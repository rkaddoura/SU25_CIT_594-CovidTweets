package edu.upenn.cit594.ui;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.processor.LivableAreaMetric;
import edu.upenn.cit594.processor.MarketValueMetric;
import edu.upenn.cit594.processor.Processor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserInterface {

    protected Processor processor;
    protected Scanner in;
    Logger logger = Logger.getInstance();

    public UserInterface(Processor processor){
        this.processor = processor;
        in = new Scanner(System.in);
    }

    public void start(){

        while (true){
            printMenu();
            String input = getInput("");
            int choice;
            try{
                choice = Integer.parseInt(input);
                if (choice < 0 || choice > 7){
                    System.out.println("Invalid input. Try again.\n");
                    continue;
                }
            }catch (NumberFormatException e){
                System.out.println("Invalid input. Try again.\n");
                continue;
            }

            Set<Integer> availableActions = processor.getAvailableActions();
            if (!availableActions.contains(choice)){
                System.out.println("Data is not available for this option.\n");
                continue;
            }

            switch (choice){
                case 0:
                    return;
                case 1:
                    showAvailableActions();
                    continue;

                case 2:
                    showTotalPop();
                    continue;

                case 3:
                    showTotalVaccineOnDate();
                    continue;

                case 4:
                    showAvgPropertyValueInZIP();
                    continue;

                case 5:
                    showAvgLivableAreaInZIP();
                    continue;

                case 6:
                    showTotalMarketVal();
                    continue;

                case 7:
                    showPercentVaccinatedGivenMarketVal();
                    continue;

                default:
                    System.out.println("Invalid input. Try again:");
            }
        }
    }

    private void printMenu(){
        System.out.print("""
                0. Exit the program.
                1. Show the available actions.
                2. Show the total population of all ZIP Codes.
                3. Show the total vaccinations per capita for each ZIP Code for the specified date.
                4. Show the average market value for properties in a specified ZIP Code.
                5. Show the average total livable area for properties in a specified ZIP Code.
                6. Show the total market value of properties, per capita, for a specified ZIPCode.
                7. Show the percentage of the population fully vaccinated, for a specified market value range.
                """);
    }

    protected void showAvailableActions(){
        Set<Integer> availableActions = processor.getAvailableActions();
        System.out.println();
        System.out.println("BEGIN OUTPUT");
        for (Integer action : availableActions){
            System.out.println(action);
        }
        System.out.println("END OUTPUT");
        System.out.println();
    }

    protected void showTotalPop(){
        int totalPop = processor.calculateTotalPop();

        if (totalPop == -1){
            System.out.println("Error: Population data is not available.");
        }else{
            printLine(String.valueOf(totalPop));
        }
    }

    protected void showTotalVaccineOnDate(){

        String vacType = getVaccinationTypeFromUser();
        String date = getDateFromUser();

        Map<Integer, Double> vacPerCap = processor.getVaccinationsPerCapita(vacType, date);

        if (vacPerCap.isEmpty()){
            printLine("0");
        }else{
            System.out.println();
            System.out.println("BEGIN OUTPUT");
            for (Integer zip: vacPerCap.keySet()){
                String out = String.format("%05d %.4f", zip, vacPerCap.get(zip));
                System.out.println(out);
            }
            System.out.println("END OUTPUT");
            System.out.println();
        }
    }

    protected void showAvgPropertyValueInZIP(){

        int zip = getZIPCodeFromUser();

        int avg = processor.calculateAvgInZIP(zip, new MarketValueMetric());

        printLine(String.valueOf(avg));
    }

    protected void showAvgLivableAreaInZIP(){

        int zip = getZIPCodeFromUser();

        int avg = processor.calculateAvgInZIP(zip, new LivableAreaMetric());

        printLine(String.valueOf(avg));

    }

    protected void showTotalMarketVal(){
        int zip = getZIPCodeFromUser();

        int totalMV = processor.calculateTotalMarketValuePerCapInZIP(zip);

        printLine(String.valueOf(totalMV));

    }


    protected void showPercentVaccinatedGivenMarketVal(){

        int upperMV;
        int lowerMV;

        // get upper limit of market value
        while(true){
            String upper = getInput("Enter the upper limit for market value:");

            try{
                upperMV = Integer.parseInt(upper);
                break;
            }catch (NumberFormatException e){
                System.out.println("Invalid value. Please try again.");
            }
        }

        // get lower limit of market value
        while(true){
            String lower = getInput("Enter the lower limit for market value:");

            try{
                lowerMV = Integer.parseInt(lower);
                if (lowerMV > upperMV){
                    System.out.println("Lower bound is greater that upper bound. Please try again.");
                    continue;
                }
                break;
            }catch (NumberFormatException e){
                System.out.println("Invalid value. Please try again.");
            }
        }

        float value = processor.calculatePercentVaccinatedGivenMarketVal(upperMV, lowerMV);
        String out = String.format("%.2f%%", value);
        printLine(out);
    }

    protected int getZIPCodeFromUser(){
        String zipRegex = "^\\d{5}$";
        Pattern zipPattern = Pattern.compile(zipRegex);

        String zip;
        while(true){
            zip = getInput("Enter a 5 digit ZIP Code:");

            Matcher zipMatcher = zipPattern.matcher(zip);
            if (zipMatcher.matches()){
                break;
            }
            System.out.println("Invalid ZIP Code. Please try again.");
        }
        return Integer.parseInt(zip);
    }

    protected String getVaccinationTypeFromUser(){
        String vacType;

        while(true){
            vacType = getInput("Enter vaccination type (partial or full): ").toLowerCase();

            if (vacType.equals("partial") || vacType.equals("full")){
                break;
            }
            System.out.println("Invalid input. Please enter 'partial' or 'full'.");
        }

        return vacType;
    }

    protected String getDateFromUser(){
        String date;
        String dateRegex = "^\\d{4}-\\d{2}-\\d{2}$";
        Pattern datePattern = Pattern.compile(dateRegex);

        while(true){
            date = getInput("Enter a date in the following format: YYYY-MM-DD");
            Matcher timeMatcher = datePattern.matcher(date);
            if (timeMatcher.matches()){
                break;
            }
            System.out.println("Invalid date. Please try again.");
        }
        return date;
    }

    private String getInput(String prompt){
        if (!prompt.isEmpty()){
            System.out.println(prompt);
        }
        System.out.print("> ");
        System.out.flush();
        String input = in.nextLine().trim();
        logger.log(input);

        return input;
    }

    private void printLine(String string){
        System.out.println();
        System.out.println("BEGIN OUTPUT");
        System.out.println(string);
        System.out.println("END OUTPUT");
        System.out.println();
    }


}
