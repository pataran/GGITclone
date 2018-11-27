package VersionControl;

import graphTool.Const;
import graphTool.DbUtils;
import org.neo4j.graphdb.*;

import java.util.*;

/**
 *
 * Created by cNorsworthy 9/27/18
 *
 * This class contains methods and helper methods for merging
 * two graphs together.
 *
 */

public class Merge {

    /**
     *
     * This is used in merging within a branch when someone pulls from master.
     *
     * We are merging two graph databases by adding their nodes and
     * relationships into another graph database. This naive merge
     * assumes no conflicts. Edges are NOT weighted. It is considered a
     * naive merge because if a node is present in one graph but not the other,
     * it will just include this node, even though this node may have been
     * deleted in the other graph, and should not be included.
     *
     * @param graph1
     * @param graph2
     * @param mergedGraph
     * @return DbOps mergedGraph
     */
    //TODO: test
    //TODO: handle deleted relationships
    //TODO: handle different data in naive merge
    //TODO: refactor, maybe instead of merged graph have a new directory to create a new merged graph in the method
    public static DbUtils mergeNaively(DbUtils graph1, DbUtils graph2, DbUtils mergedGraph){

        //Search through graph1 and graph2 (breadth first search) and get all nodes and what each node is connected to
        try (ResourceIterator<Node> graph1AllNodesIterator = graph1.getAllNodesIterator();
             ResourceIterator<Node> graph2AllNodesIterator = graph2.getAllNodesIterator()) {

            /* Put the nodes in the merged graph */
            while (graph1AllNodesIterator.hasNext() || graph2AllNodesIterator.hasNext()) {
                //assume the nodes do not exist unless shown otherwise
                Node nextNode1 = null;
                Node nextNode2 = null;
                try{
                    nextNode1 = graph1AllNodesIterator.next();
                    //Preemptively add the first node from graph 1.
                    if(nextNode1 != null){
                        Label label1 = graph1.getNodeLabel(nextNode1);
                        HashMap<String, Object> props1 = graph1.readNodeProperties(nextNode1);
                        if(mergedGraph.getNodeByID(props1.get(Const.UUID)) == null){
                            mergedGraph.putNodeInGraph(label1, props1);
                        }

                    }
                } catch(Exception ignored){}

                try{
                    nextNode2 = graph2AllNodesIterator.next();
                } catch (Exception ignored){}

                //Only add the next node from graph 2 if it doesn't have the same id as the next node from graph 1.
                //So you won't add the same node twice.
                if(nextNode1 != null && nextNode2 != null){
                    //If the ids of the next nodes aren't equal, put in the next node from the second graph
                    if (!graph1.getNodeID(nextNode1).equals(graph2.getNodeID(nextNode2))) {
                        Label label2 = graph2.getNodeLabel(nextNode2);
                        HashMap<String, Object> props2 = graph2.readNodeProperties(nextNode2);
                        if(mergedGraph.getNodeByID(props2.get(Const.UUID)) == null) {
                            mergedGraph.putNodeInGraph(label2, props2);
                        }
                    }
                } else if (nextNode2 != null){
                    Label label2 = graph2.getNodeLabel(nextNode2);
                    HashMap<String, Object> props2 = graph2.readNodeProperties(nextNode2);
                    if(mergedGraph.getNodeByID(props2.get(Const.UUID)) == null) {
                        mergedGraph.putNodeInGraph(label2, props2);
                    }
                }
            }
        }
        /* Put the relationships in the merged graph */
        ArrayList<String> allMergedGraphIds = mergedGraph.getAllIDs();

        for(Object mergedGraphId : allMergedGraphIds){
            String currentMergedGraphId = mergedGraphId.toString();
            try {
                /*Get the appropriate node from either graph 1, graph 2, or both*/
                Node graph1Node = graph1.getNodeByID(currentMergedGraphId);
                Node graph2Node = graph2.getNodeByID(currentMergedGraphId);
                if ((graph1Node != null) && (graph2Node != null)) {
                    //put in relationships for both
                    connectSameRelationshipsInMergedGraph(graph1, mergedGraph, graph1Node);
                    connectSameRelationshipsInMergedGraph(graph2, mergedGraph, graph2Node);
                    //connectAllRelationships
                } else if ((graph1Node == null) && (graph2Node != null)) {
                    //put in relationship for node2 only
                    connectSameRelationshipsInMergedGraph(graph2, mergedGraph, graph2Node);
                } else if (graph1Node != null) {
                    //put in relationship for node1 only
                    connectSameRelationshipsInMergedGraph(graph1, mergedGraph, graph1Node);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Test and handle merge conflicts with both graphs concerning the data in the nodes
        testAndHandleDataMergeConflict(graph1, graph2, mergedGraph);

        return mergedGraph;
    }

    /**
     *
     * This is used for merging two separate branches together into a new branch.
     *
     * This graph merging algorithm handles merge conflicts using the
     * MergeConflictException class. It looks at the common ancestor of the
     * two graph databases, seeing what has been deleted. It handles
     * merge conflicts based on this information.
     *
     * @param graph1
     * @param graph2
     * @param commonAncestorGraph
     * @param mergedGraph
     * @return DbOps mergedGraph
     */

    public static DbUtils mergeWithPossibleConflicts(DbUtils graph1, DbUtils graph2,
                                                   DbUtils commonAncestorGraph,
                                                   DbUtils mergedGraph){

        //See what has changed with graphs 1 and 2
        ArrayList<String> graph1Deletions = getAllDeletedNodesIds(commonAncestorGraph, graph1);
        ArrayList<String> graph2Deletions = getAllDeletedNodesIds(commonAncestorGraph, graph2);
        //A set will not allow duplicates
        Set<String> totalDeletions = new HashSet<>();
        totalDeletions.addAll(graph1Deletions);
        totalDeletions.addAll(graph2Deletions);

        //Merge the two graphs together naively
        mergedGraph = mergeNaively(graph1, graph2, mergedGraph);

        //Test and handle merge conflicts with deleted nodes
        testAndHandleNodeMergeConflict(mergedGraph, totalDeletions);

        return mergedGraph;
    }

    private static void connectSameRelationshipsInMergedGraph(DbUtils originalGraph, DbUtils mergedGraph, Node startNode){

        String startNodeId = originalGraph.getNodeID(startNode);
        Relationship relationship;

        Iterator<Relationship> relationshipIterator = originalGraph.getRelationshipIterator(startNode);
        while (relationshipIterator.hasNext()){

            relationship = relationshipIterator.next();
            RelationshipType relationshipType = originalGraph.getRelationshipType(relationship);

            //Get both nodes in this relationship
            Node[] relationshipNodes = originalGraph.getRelationshipNodes(relationship);

            //(the second element in the array)
            Node endNode = relationshipNodes[1];
            //get id of the end node in the relationship
            String endNodeId = originalGraph.getNodeID(endNode);

            //To prevent relationships between a node and itself, and to prevent adding the same relationship twice.
            if(!startNodeId.equals(endNodeId)){
                //get appropriate nodes in merged graph
                Node node1 = mergedGraph.getNodeByID(startNodeId);
                Node node2 = mergedGraph.getNodeByID(endNodeId);

                if (node1 != null) {
                    Relationship preExistingRelationship = mergedGraph.getRelationshipBetween(node1, endNodeId);
                    if(preExistingRelationship == null){ //to make sure you aren't adding the same relationship twice
                        //connect the appropriate relationship
                        mergedGraph.createRelationshipBetween(node1, node2, relationshipType);
                    }
                }
            }
        }
    }

    private static ArrayList<String> getAllDeletedNodesIds(DbUtils ancestorGraph, DbUtils descendantGraph){

        ArrayList<String> allDeletedNodesIds = new ArrayList<>();

        //Get all node ID's from the ancestor graph and descendant graph
        ArrayList<String> allAncestorGraphIds = ancestorGraph.getAllIDs();
        ArrayList<String> allDescendantGraphIds = descendantGraph.getAllIDs();

        //Loop over every id in the ancestor graph, which contains a number of
        //nodes greater than or equal to the number of nodes in its descendant.
        for(String ancestorID: allAncestorGraphIds){
            //If the descendant graph does not contain this ancestor node's ID, it was deleted
            if(!allDescendantGraphIds.contains(ancestorID)){
                allDeletedNodesIds.add(ancestorID);
            }
        }
        return allDeletedNodesIds;
    }

    private static void testAndHandleNodeMergeConflict(DbUtils mergedGraph, Set<String> totalDeletions){
        //loop over every node in the merged graph
        ResourceIterator<Node> mergedGraphAllNodesIterator = mergedGraph.getAllNodesIterator();
        while(mergedGraphAllNodesIterator.hasNext()){
            Node nextNode = mergedGraphAllNodesIterator.next();
            String mergedGraphNodeID = mergedGraph.getNodeID(nextNode);
            //check every ID in the array of all deleted ID's
            for(String deletedId : totalDeletions){
                //if the merged graph contains a node that should have been deleted, handle this conflict
                if(deletedId.equals(mergedGraphNodeID)){
                    handleNodeMergeConflict(mergedGraph, mergedGraphNodeID);
                }
            }
        }
    }

    private static void handleNodeMergeConflict(DbUtils mergedGraph, String conflictingID){
        System.out.println("Resurfaced deleted node with id: "
                + conflictingID + ". Do you want to keep this node and its non-conflicting children? (Y/N)");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next().toLowerCase();

        Node conflictingNode = mergedGraph.getNodeByID(conflictingID);
        Label conflictingNodeLabel = mergedGraph.getNodeLabel(conflictingNode);
        if(input.equals("y")){
            System.out.println("Keeping node " + conflictingID + " and its non-conflicting children.");
        } else if (input.equals("n")){
            if(conflictingNodeLabel.equals(Const.ROOT_LABEL)){
                System.out.println("You cannot delete the root node.");
            } else {
                System.out.println("Deleting node " + conflictingID + " and its children.");
                mergedGraph.deleteNode(conflictingNodeLabel, conflictingID);
            }
        }
    }

    private static void handleDataMergeConflict(DbUtils mergedGraph, String conflictingId, String key,
                                                String graph1Property, String graph2Property){
        System.out.println("Conflicting data on node with id: "
                + conflictingId + "of data type " + key + ". Which data would you rather keep?");
        System.out.println("A. Original graph data: " + graph1Property);
        System.out.println("B. Merged graph data: " + graph2Property);
        Scanner scanner = new Scanner(System.in);
        String input = scanner.next().toLowerCase();

        Node conflictingNode = mergedGraph.getNodeByID(conflictingId);
        if(input.equals("a")){
            mergedGraph.setProperty(conflictingNode, key, graph1Property);
        } else if (input.equals("b")){
            mergedGraph.setProperty(conflictingNode, key, graph2Property);
        }
    }

    private static void testAndHandleDataMergeConflict(DbUtils graph1, DbUtils graph2, DbUtils mergedGraph){
        //Currently, this method assumes all nodes have the same amount of properties

        //loop over every node in the merged graph, we only care to compare data in nodes in that graph
        ResourceIterator<Node> mergedGraphAllNodesIterator = mergedGraph.getAllNodesIterator();
        while(mergedGraphAllNodesIterator.hasNext()){
            Node currentMergedGraphNode = mergedGraphAllNodesIterator.next();
            String currentMergedGraphNodeId = mergedGraph.getNodeID(currentMergedGraphNode);

            //Iterate over all five keys to get their corresponding properties
            //These properties except for the UUID are the data for comparison
            Iterator<String> propertyKeysItr = mergedGraph.getPropertyKeysIterator(currentMergedGraphNode);
            while(propertyKeysItr.hasNext()){
                String propertyKey = propertyKeysItr.next();
                if(propertyKey.equals(Const.UUID)){
                    continue;
                }

                try {
                    //Get the same node in each graph to compare data
                    Node currentGraph1Node = graph1.getNodeByID(currentMergedGraphNodeId);
                    Node currentGraph2Node = graph2.getNodeByID(currentMergedGraphNodeId);
                    //Get the two graph properties
                    String graph1Property = graph1.getPropertyAsString(currentGraph1Node, propertyKey);
                    String graph2Property = graph2.getPropertyAsString(currentGraph2Node, propertyKey);

                    //If the two strings aren't equal, you have a conflict that needs resolving
                    if(currentGraph1Node != null && currentGraph2Node != null){
                        if(!graph1Property.equals(graph2Property)){
                            handleDataMergeConflict(mergedGraph, currentMergedGraphNodeId, propertyKey,
                                    graph1Property, graph2Property);
                        }
                    }
                } catch(Exception ignored){}
            }
        }
    }
}