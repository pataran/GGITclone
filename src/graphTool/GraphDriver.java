package graphTool;

import java.util.HashMap;
import java.util.Scanner;
import org.neo4j.graphdb.Label;

public class GraphDriver{

    private boolean running;

    private Scanner input;
    private DbOps dbOps;

    public GraphDriver(){
        input = new Scanner(System.in);
        dbOps = new DbOps();
        running = true;
    }

    private void showMainMenu(){

        System.out.println("------------- Graph Tool Driver ------------------\n\n");
        System.out.println("\tMAIN-MENU\n");
        System.out.println("1 - Create Observation");
        System.out.println("2 - Create Knowledge");
        System.out.println("3 - Update Observation");
        System.out.println("4 - Update Knowledge");
        System.out.println("5 - Delete Observation");
        System.out.println("6 - Delete Knowledge");
        System.out.println("7 - More Commands");
        System.out.println("0 - Exit Program");
        System.out.println("----------------------------------------------------\n");
        System.out.println("Select a command to execute:");

        executeMainCommand(input.nextLine());
    }

    private void showMoreOptions(){

        System.out.println("------------- Graph Tool Driver ------------------\n\n");
        System.out.println("\tMORE-COMMANDS\n");
        System.out.println("1 - List all Observations");
        System.out.println("2 - List all Knowledges");
        System.out.println("3 - Coming Soon");
        System.out.println("0 - Main Menu");
        System.out.println("----------------------------------------------------\n");
        System.out.println("Select a command to execute:");

        executeMoreCommand(input.nextLine());
    }

    private void executeMainCommand(String args){
        try {
            String input = args;
            switch (input) {
                case "1":
                    initCreateNode(Const.OBSERVATION_LABEL);
                    break;
                case "2":
                    initCreateNode(Const.KNOWLEDGE_LABEL);
                    break;
                case "3":
                    initUpdateNode(Const.OBSERVATION_LABEL);
                    break;
                case "4":
                    initUpdateNode(Const.KNOWLEDGE_LABEL);
                    break;
                case "5":
                    initDeleteNode(Const.OBSERVATION_LABEL);
                    break;
                case "6":
                    initDeleteNode(Const.KNOWLEDGE_LABEL);
                    break;
                case "7":
                    showMoreOptions();
                    break;
                case "0":
                    exitProgram();
                    break;
                default:
                    System.out.println("The user must enter a command");
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("Operation does not currently exist. We'll get to it eventually.");
        }
    }

    private void executeMoreCommand(String args){
        try {
            String input = args;
            switch (input) {
                case "1":
                    initReadAllNodes(Const.OBSERVATION_LABEL);
                    break;
                case "2":
                    initReadAllNodes(Const.KNOWLEDGE_LABEL);
                    break;
                case "3":
                    throw new UnsupportedOperationException("The \"Coming Soon\" command is not currently supported. We'll get to it one day");
                case "0":
                    showMainMenu();
                    break;
                default:
                    System.out.println("The user must enter a command");
            }
        } catch (UnsupportedOperationException e) {
            System.out.println(e);
        }
    }

    private void initCreateNode(Label label){
        System.out.println("\n...Creating a new " + label.name() + ":\n");
        System.out.print("...Set the following property values: ");
        System.out.print("[ \"id\", \"name\", \"latitude\", \"longitude\", \"description\" ]\n");

        HashMap<String, Object> propertyToValues = new HashMap<>();

        for(String prop : Const.NODE_PROPERTIES)
        {
            HandleValueInput(prop, propertyToValues);
        }

        if(label == Const.OBSERVATION_LABEL)
        {
            dbOps.createObservation(propertyToValues);
        }
        else if(label == Const.KNOWLEDGE_LABEL)
        {
            System.out.println("...Choose available [Observation] to link new [Knowledge] to:");

            boolean valid = false;
            String obsId = new String();

            while(!valid)
            {
                HashMap<String, HashMap<String, Object>> allNodes = dbOps.readAllObservations();
                for (String key : allNodes.keySet())
                {
                    System.out.println("--( { \"" + Const.UUID + "\" : " + key + "} )--");
                }
                System.out.println("------------------------------------------------------");
                System.out.println("Enter id of [Observation]:");

                obsId = input.nextLine();
                if(allNodes.keySet().contains(obsId))
                {
                    valid = true;
                }
                else
                {
                    System.out.println("Invalid [Observation] id.\n\n\n\n");
                    input.next();
                }
            }
            dbOps.createKnowledge(obsId, propertyToValues);
        }
        else
        {
            System.out.println("Invalid Label.\n\n\n\n");
            input.next();
        }
    }

    private void initUpdateNode(Label label){
        System.out.println("\n...Update an existing " + label.name() + ":\n");
        System.out.print("...Enter the Id value for the node being updated: ");

        String idValue = input.nextLine();

        HashMap<String, Object> propertyToValues = new HashMap<>();
        HashMap<String, Object> readNode;

        if(label == Const.OBSERVATION_LABEL)
        {
            readNode = dbOps.readObservation(idValue);
            if(readNode != null)
            {
                EditNodeProperties(readNode, propertyToValues);
                dbOps.updateObservation(idValue, propertyToValues);
            }
            else
            {
                System.out.println("Invalid [Observation] id.\n\n\n\n");
                input.next();
            }
        }
        else if(label == Const.KNOWLEDGE_LABEL)
        {
            readNode = dbOps.readKnowledge(idValue);
            EditNodeProperties(readNode, propertyToValues);
            dbOps.updateKnowledge(idValue, propertyToValues);
        }
        else
        {
            System.out.println("Invalid Label.\n\n\n\n");
            input.next();
        }
    }

    private void initDeleteNode(Label label){
        System.out.println("\n...Update an existing " + label.name() + ":\n");
        System.out.print("...Enter the Id value for the node being updated: ");

        String idValue = input.nextLine();

        if(label == Const.OBSERVATION_LABEL)
        {
            dbOps.deleteObservation(idValue);
        }
        else if(label == Const.KNOWLEDGE_LABEL)
        {
            dbOps.deleteKnowledge(idValue);
        }
        else
        {
            System.out.println("Invalid Label.\n\n\n\n");
            input.next();
        }
    }

    private void initReadAllNodes(Label label){
        HashMap<String, HashMap<String, Object>> nodes;
        if(label == Const.OBSERVATION_LABEL)
        {
            nodes = dbOps.readAllObservations();
            System.out.println("OBSERVATIONS-----");
            for(String nodeId : nodes.keySet()){
                System.out.println("[{ id: \"" + nodeId + "\"}] -");
            }
        }
        else if(label == Const.KNOWLEDGE_LABEL)
        {
            nodes = dbOps.readAllKnowledges();
            System.out.println("KNOWLEDGES-----");
            for(String nodeId : nodes.keySet()){
                System.out.println("[{ id: \"" + nodeId + "\"}] -");
            }
        }
        else {
            System.out.println("Invalid label ....\n");
            input.next();
        }
    }

    private void exitProgram(){
        running = false;
    }

    public void Drive(){
        while(running){
            showMainMenu();
        }
    }

    boolean TryParseDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void EditNodeProperties(HashMap<String,Object> readNode, HashMap<String,Object> propertyToValues){
        System.out.println("\nRetrieved node with properties: [ \"id\", \"name\", \"latitude\", \"longitude\", \"description\" ]");

        String chooseProp = new String();

        while (chooseProp != "done")
        {
            for (String prop : readNode.keySet())
            {
                System.out.println("[\"" + prop + "\"] <- {" + readNode.get(prop) + "}");
            }
            System.out.println("--------------------------------------------------------");
            System.out.print("Enter property to edit: (Type DONE to when finished)");

            boolean valid = false;
            while (!valid)
            {
                chooseProp = input.nextLine();

                switch (chooseProp.toLowerCase())
                {
                    case Const.UUID:
                        HandleValueInput(Const.UUID, propertyToValues);
                        valid = true;
                        break;
                    case Const.NAME:
                        HandleValueInput(Const.NAME, propertyToValues);
                        valid = true;
                        break;
                    case Const.LATITUDE:
                        HandleValueInput(Const.LATITUDE, propertyToValues);
                        valid = true;
                        break;
                    case Const.LONGITUDE:
                        HandleValueInput(Const.LONGITUDE, propertyToValues);
                        valid = true;
                        break;
                    case Const.DESCRIPTION:
                        HandleValueInput(Const.DESCRIPTION, propertyToValues);
                        valid = true;
                        break;
                    case "done":
                        System.out.print("...Finishing property edit.");
                        valid = true;
                        break;
                    default:
                        System.out.println("Invalid property. Choose a valid node property:");
                }
            }
        }
    }

    private void HandleValueInput(String prop, HashMap<String,Object> propertyToValues){
        System.out.println("Enter a new value for \"" + prop +"\":");
        System.out.print("[\"" + prop +"\"] -> ");
        String value = input.nextLine();

        if(TryParseDouble(value))
        {
            Double dValue = Double.parseDouble(value);
            propertyToValues.put(prop, dValue);
        }
        else
        {
            propertyToValues.put(prop, value);
        }
    }

    public static void main(String[] args){
        GraphDriver driver = new GraphDriver();
        driver.Drive();
    }
}
