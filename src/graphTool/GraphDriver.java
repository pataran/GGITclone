package graphTool;

import java.util.HashMap;
import java.util.Scanner;

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
        System.out.println("Select a command to execute:");
        System.out.println("1 - Create Observation");
        System.out.println("2 - Create Knowledge");
        System.out.println("3 - Update Observation");
        System.out.println("4 - Update Knowledge");
        System.out.println("5 - Delete Observation");
        System.out.println("6 - Delete Knowledge");
        System.out.println("7 - More Commands");
        System.out.println("0 - Exit Program");
        System.out.println("----------------------------------------------------\n\n");

        executeMainCommand(input.nextLine());
    }

    private void showMoreOptions(){

        System.out.println("------------- Graph Tool Driver ------------------\n\n");
        System.out.println("\tMORE-COMMANDS\n");
        System.out.println("Select a command to execute:");
        System.out.println("1 - Coming Soon");
        System.out.println("2 - Coming Soon");
        System.out.println("3 - Coming Soon");
        System.out.println("0 - Main Menu");
        System.out.println("----------------------------------------------------\n\n");

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
                case "2":
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

    private void initCreateNode(String label){
        System.out.println("\n...Creating a new " + label + ":\n");
        System.out.print("...Set the following property values: ");

        HashMap<String, Object> propertyToValues = new HashMap<>();

        System.out.print("[ \"id\", \"name\", \"latitude\", \"longitude\", \"description\" ]\n");

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
            dbOps.createKnowledge(propertyToValues);
        }
        else
        {
            System.out.println("Invalid Label.\n\n\n\n");
            input.next();
        }
    }

    private void HandleValueInput(String prop, HashMap<String,Object> propertyToValues){
        System.out.println("Enter a new value for \"" + prop +"\":");
        System.out.print("[\"" + prop +"\"] -> ");
        String value = input.nextLine();

        if(tryParseDouble(value))
        {
            Double dValue = Double.parseDouble(value);
            propertyToValues.put(prop, dValue);
        }
        else
        {
            propertyToValues.put(prop, value);
        }
    }

    private void initUpdateNode(String label){
        System.out.println("\n...Update an existing " + label + ":\n");
        System.out.print("...Enter the Id value for the node being updated: ");

        String idValue = input.nextLine();

        HashMap<String, Object> propertyToValues = new HashMap<>();
        HashMap<String, Object> readNode;

        if(label == Const.OBSERVATION_LABEL)
        {
            readNode = dbOps.readObservation(idValue);
            EditNodeProperties(readNode, propertyToValues);
            dbOps.updateObservation(idValue, propertyToValues);
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

    private void EditNodeProperties(HashMap<String,Object> readNode, HashMap<String,Object> propertyToValues){
        if(readNode != null)
        {
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
    }

    private void initDeleteNode(String label){
        System.out.println("\n...Update an existing " + label + ":\n");
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

    private void exitProgram(){
        running = false;
    }

    public void Drive(){
        while(running){
            showMainMenu();
        }
    }

    boolean tryParseDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}