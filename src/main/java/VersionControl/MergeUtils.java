package VersionControl;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class MergeUtils {
    //A class that used to be in db utils, contains what the merge methods need

    GraphDatabaseService graphDb;

    private void connectDatabase(String pathName)
    {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(pathName));
        registerShutdownHook(graphDb);  //Used to shut down database if JVM is closed
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDb.shutdown();
            }
        } );
    }


    public enum RelTypes implements RelationshipType
    {
        KNOWS
    }

    public void createNode(String nodeType,String prop, String propVal, int num)
    {
        try ( Transaction tx = graphDb.beginTx() )
        {
            Label label = Label.label(nodeType);

            // Create some users
            for ( int id = 1; id <= num; id++ )
            {
                Node userNode = graphDb.createNode( label );
                userNode.setProperty( prop, propVal );
            }
            System.out.println( nodeType +  " created" );
            tx.success();
        }
    }

    void putNodeInGraph(String id){
        try(Transaction tx = graphDb.beginTx()){
            Node newNode = graphDb.createNode();
            //String id = getNodeID(graph, node);
            newNode.setProperty("ID", id);

            tx.success();
        }
    }

    static void putNodeInGraphStatic(GraphDatabaseService graph, String id){
        try(Transaction tx = graph.beginTx()){
            Node newNode = graph.createNode();
            //String id = getNodeID(graph, node);
            newNode.setProperty("ID", id);

            tx.success();
        }
    }

    static RelationshipType getRelationshipType(GraphDatabaseService graph, Node node, Relationship relationship){
        RelationshipType relType;
        try(Transaction tx = graph.beginTx()){
            relType = relationship.getType();
            tx.success();
        }
        return relType;
    }

    static void connectNodeInGraphByRelationship(GraphDatabaseService graph, Node node, Iterator<Relationship> relsIter){
        try(Transaction tx = graph.beginTx()){
            Relationship relationship;
            while (relsIter.hasNext()){
                relationship = relsIter.next();
                RelationshipType relType = getRelationshipType(graph, node, relationship);
                Node[] relNodes = relationship.getNodes();
                for(Node relNode : relNodes){
                    node.createRelationshipTo(relNode, relType);
                }
            }
            tx.success();
        }
    }

    void createRelationshipBetween(Node node1, Node node2, RelationshipType relType){
        try(Transaction tx = graphDb.beginTx()){
            Relationship rel = node1.createRelationshipTo(node2, relType);
            rel.setProperty("T", "test");
            tx.success();
        }
    }

    static void createRelationshipBetweenStatic(GraphDatabaseService graph, Node node1, Node node2, RelationshipType relType){
        try(Transaction tx = graph.beginTx()){
            Relationship rel = node1.createRelationshipTo(node2, relType);
            //rel.setProperty("T", "test");
            tx.success();
        }
    }

    public static Iterator<Relationship> getRelationshipIterator(GraphDatabaseService graph, Node node){
        Iterator<Relationship> relsIterator;
        try(Transaction tx = graph.beginTx()){
            Iterable<Relationship> rels = node.getRelationships();
            relsIterator = rels.iterator();
            tx.success();
        }
        return relsIterator;
    }

    static void printRelationships(GraphDatabaseService graph, Node node, Iterator<Relationship> relsIter){

    }

    static Node[] getRelationshipNodes(GraphDatabaseService graph, Relationship relationship){
        Node[] relationshipNodes;
        try(Transaction tx = graph.beginTx()){
            relationshipNodes = relationship.getNodes();
            tx.success();
        }
        return relationshipNodes;
    }

    static void deleteRelationship(GraphDatabaseService graph, Relationship relationship){
        try(Transaction tx = graph.beginTx()){
            relationship.delete();
            tx.success();
        }
    }

    public static String getNodeID(GraphDatabaseService graph, Node node){
        String ID;
        try(Transaction tx = graph.beginTx()){
            ID = node.getProperty("ID").toString();
            if(ID == null){
                ID = "";
            }
            tx.success();
        }
        return ID;
    }

    ArrayList<String> getAllIDs(){
        ResourceIterator<Node> nodesItr = getAllNodesIterator();
        String currentId;
        ArrayList<String> allIds = new ArrayList<>();
        try(Transaction tx = graphDb.beginTx()){
            while(nodesItr.hasNext()){
                Node node = nodesItr.next();
                currentId = node.getProperty("ID").toString();
                allIds.add(currentId);
            }
            tx.success();
        }
        return allIds;
    }

    static ArrayList<String> getAllIDsStatic(GraphDatabaseService graph){
        ResourceIterator<Node> nodesItr = getAllNodesIteratorStatic(graph);
        String currentId;
        ArrayList<String> allIds = new ArrayList<>();
        try(Transaction tx = graph.beginTx()){
            while(nodesItr.hasNext()){
                Node node = nodesItr.next();
                currentId = node.getProperty("ID").toString();
                allIds.add(currentId);
            }
            tx.success();
        }
        return allIds;
    }

    public static Node getNextNodeFromIterator(GraphDatabaseService graph, ResourceIterable<Node> iterator){
        Node nextNode;
        try(Transaction tx = graph.beginTx()){
            nextNode = iterator.iterator().next();
            tx.success();
        }
        return nextNode;
    }

    public void createDefaultNodes(String name ,int num)
    {
        try ( Transaction tx = graphDb.beginTx() )
        {
            Label label = Label.label(name);

            // Create some users
            for ( int id = 0; id < num; id++ )
            {
                Node userNode = graphDb.createNode( label );
                userNode.setProperty( "username", "user" + id + "@neo4j.org" );
            }
            System.out.println( "Users created" );
            tx.success();
        }
    }

    public void showAllNodes(String nodeName, String prop)
    {

        Label label = Label.label(nodeName);

        try ( Transaction tx = graphDb.beginTx() )
        {
            try ( ResourceIterator<Node> users = graphDb.findNodes( label ) )

            {
                ArrayList<Node> userNodes = new ArrayList<>();
                while ( users.hasNext() )
                {
                    userNodes.add( users.next() );

                }

                for ( Node node : userNodes )
                {
                    System.out.println( "The property of node " + nodeName + " is " + node.getProperty( prop ) );

                }
            }
            tx.success();
        }

    }

    public static ResourceIterator<Node> getAllNodesIteratorStatic(GraphDatabaseService graph){
        ResourceIterator<Node> allIterableNodes;
        try(Transaction tx = graph.beginTx()){
            ResourceIterable<Node> iterable = graph.getAllNodes();
            allIterableNodes = iterable.iterator();
            tx.success();
        }
        return allIterableNodes;
    }

    ResourceIterator<Node> getAllNodesIterator(){
        ResourceIterator<Node> allIterableNodes;
        try(Transaction tx = graphDb.beginTx()){
            ResourceIterable<Node> iterable = graphDb.getAllNodes();
            allIterableNodes = iterable.iterator();
            tx.success();
        }
        return allIterableNodes;
    }

    public void getNodeById(String nodeName,String prop, String id){

        Label label = Label.label(nodeName);

        try ( Transaction tx = graphDb.beginTx() )
        {
            try ( ResourceIterator<Node> users =
                          graphDb.findNodes( label, prop, id) )
            {
                ArrayList<Node> userNodes = new ArrayList<>();
                while ( users.hasNext() )
                {
                    userNodes.add( users.next() );

                }

                for ( Node node : userNodes )
                {
                    System.out.println(
                            "The username of user " + id + " is " + node.getProperty( prop ) );
                }
            }
            tx.success();
        }

    }

    static Node getNodeByID(GraphDatabaseService graph, Object value){

        ResourceIterator<Node> graphNodesIterator = MergeUtils.getAllNodesIteratorStatic(graph);
        Node currentNode;
        try(Transaction tx = graph.beginTx()){
            while(graphNodesIterator.hasNext()) {
                currentNode = graphNodesIterator.next();
                String currentKey = currentNode.getProperty("ID").toString();
                if (currentKey.equals(value)) {
                    return currentNode;
                }
            }
            tx.success();
        }
        return null;
    }

    public void deleteNodes(String nodeType,String prop, String propVal)
    {
        try ( Transaction tx = graphDb.beginTx())
        {
            Label label = Label.label( nodeType );
            ResourceIterator<Node> users = ( graphDb.findNodes( label, prop, propVal ) );

            while(users.hasNext())
            {
                Node user = users.next();
                user.delete();
            }
            tx.success();
        }
    }

    public void createRelationship(String nodeType1, String nodeType2)
    {
        Relationship relationship;
        try( Transaction tx = graphDb.beginTx())
        {
            Label label1 = Label.label(nodeType1);
            Label label2 = Label.label(nodeType2);

            ResourceIterator<Node> node = (graphDb.findNodes(label1));
            ResourceIterator<Node> node2 = (graphDb.findNodes(label2));

            while(node.hasNext()){
                Node firstNode = node.next();
                Node secondNode = node2.next();

                relationship = firstNode.createRelationshipTo( secondNode, RelTypes.KNOWS );
                relationship.setProperty( "link", "we are related " );
            }
        }
    }

    public void createRelationship(GraphDatabaseService graph, Node node1,Node node2, RelationshipType relType){
        try(Transaction tx = graph.beginTx()){

            tx.success();
        }
    }

    public void showRelationships(String nodeType1, String prop1,String nodeType2,String prop2){
        Relationship relationship;
        try( Transaction tx = graphDb.beginTx())
        {

            Label label1 = Label.label("a");
            Label label2 = Label.label("b");
            Node node1 = graphDb.createNode(label1);
            Node node2 = graphDb.createNode(label2);
            node1.setProperty("prop","Patrick hates");
            node2.setProperty("prop", "neo4j");
            relationship = node1.createRelationshipTo( node2, RelTypes.KNOWS );
            relationship.setProperty( "message", "relationships in" );

            System.out.println(node1.getProperty("prop"));
            System.out.println(relationship.getProperty("message"));
            System.out.println(node2.getProperty("prop"));
        }

    }

    public void getConnection(String pathName)
    {
        connectDatabase(pathName);
    }

}