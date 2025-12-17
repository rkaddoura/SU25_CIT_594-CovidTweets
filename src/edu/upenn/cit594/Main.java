package edu.upenn.cit594;

import edu.upenn.cit594.datamanagement.DataReader;
import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.processor.Processor;
import edu.upenn.cit594.ui.UserInterface;
import edu.upenn.cit594.util.DataShare;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws IOException {

        // validate input
        Map<String, String> inputs = new HashMap<>();

        if (args.length <= 4){

            Set<String> validNames = Set.of("covid", "properties", "population", "log");
            String inputRegex = "^--(?<name>.+?)=(?<value>.+)$";
            Pattern pattern = Pattern.compile(inputRegex);

            for (String input : args) {
                Matcher matcher = pattern.matcher(input);
                if (!matcher.matches()) {
                    System.out.println("Invalid format: " + input);
                    return;
                }

                String name = matcher.group("name");
                String value = matcher.group("value");

                if (!validNames.contains(name)){
                    System.out.print("Invalid argument name: " + input);
                    return;
                }

                if (inputs.containsKey(name)) {
                    System.out.println("Duplicate argument: " + input);
                    return;
                }

                inputs.put(name, value);
            }
        }else{
            System.out.println("Invalid number of arguments.");
            return;
        }

        // create logger & log command line arguments
        Logger logger = Logger.getInstance();

        if (inputs.containsKey("log")){
            try{
                logger.setOutput(inputs.get("log"));
            }catch(RuntimeException e){
                System.out.println(e.getMessage());
                // log to system.err if file cant be opened
                logger.log(argsToString(args));
                return;
            }
        }
        logger.log(argsToString(args));

        // initialize readers and read files
        if (!initializeReaders(inputs)){
            DataShare.getInstance().reset();
            logger.close();
            return;
        }


        Processor processor = new Processor();
        UserInterface ui = new UserInterface(processor);

        ui.start();

        // close and reset files when program is done
        DataShare.getInstance().reset();
        logger.close();

    }

    private static boolean initializeReaders(Map<String, String> inputs){
        // create readers for given files
        for (String input: inputs.keySet()){
            if (input.equals("log")){
                continue;
            }

            try{
                DataReader reader = DataReader.determineFileType(inputs.get(input), DataReader.getDataType(input));
                reader.readFile();
            }catch (IllegalArgumentException | IOException e){
                System.out.print(e.getMessage());
                return false;
            }
        }
        return true;
    }

    private static String argsToString(String[] args){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++){
            sb.append(args[i]);
            if (i != args.length - 1){
                sb.append(" ");
            }
        }
        return sb.toString();
    }


}
