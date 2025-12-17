package edu.upenn.cit594.datamanagement;
import edu.upenn.cit594.util.CovidRecord;
import edu.upenn.cit594.util.DataShare;
import edu.upenn.cit594.util.PropertyRecord;

import java.io.*;
import java.util.*;

public class CSVReader extends DataReader {

    private final List<PropertyRecord> propertyRecords = new ArrayList<>();
    private final List<CovidRecord> covidRecords = new ArrayList<>();
    private final Map<Integer, Integer> populationMap = new HashMap<>();

    private final String filename;
    private final DataType dataType;

    protected CSVReader(String filename, DataType dataType) {
        this.filename = filename;
        this.dataType = dataType;
    }

    private enum STATES{
        START,
        NO_QUOTE,
        QUOTE,
        INNER_QUOTE
    }

    @Override
    public void readFile() throws IOException {

        List<String> headers = null;
        boolean firstLine = true;

        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();


        try(BufferedReader reader = new BufferedReader(new FileReader(this.filename))){
            // set up reading of first character
            STATES state = STATES.START;
            int character = reader.read();

            // check for EOF
            while (character != -1) {
                switch (state) {

                    // for first character
                    case START:
                        switch (character) {

                            // if line starts with quote
                            case '"':
                                state = STATES.QUOTE;
                                break;

                            // add empty string if comma
                            case ',':
                                row.add("");
                                break;

                            // add empty string if new line
                            case '\n':
                                row.add("");
                                if (firstLine){
                                    headers = row;
                                    firstLine = false;
                                }else{
                                    processRow(row, headers);
                                }
                                row = new ArrayList<>();
                                break;

                                // need CRLF to end
                            case '\r':
                                character = reader.read();

                                // check if the next character is LF for a proper ending
                                if (character == '\n') {
                                    row.add("");
                                    if (firstLine){
                                        headers = row;
                                        firstLine = false;
                                    }else{
                                        processRow(row, headers);
                                    }
                                    row = new ArrayList<>();
                                } else {
                                    throw new CSVFormatException("Need LF after CR");
                                }
                                break;

                                // add anything else
                            default:
                                // convert from numerical value and add to string builder
                                field.append((char) character);
                                state = STATES.NO_QUOTE;
                                break;

                        }
                        break;

                    // when first character is not a quote
                    case NO_QUOTE:
                        switch (character) {
                            // quote illegal
                            case '"':
                                throw new CSVFormatException("Inner quote not valid here.");

                                // check for end of field
                            case ',':
                                row.add(field.toString());
                                field = new StringBuilder();
                                state = STATES.START;
                                break;

                            // if end of line, add fields and exit
                            case '\n':
                                row.add(field.toString());
                                if (firstLine){
                                    headers = row;
                                    firstLine = false;
                                }else{
                                    processRow(row, headers);
                                }
                                row = new ArrayList<>();
                                field = new StringBuilder();
                                state = STATES.START;
                                break;

                                // need CRLF to end
                            case '\r':
                                character = reader.read();

                                // check if the next character is LF for a proper ending
                                if (character == '\n') {
                                    row.add(field.toString());
                                    if (firstLine){
                                        headers = row;
                                        firstLine = false;
                                    }else{
                                        processRow(row, headers);
                                    }
                                    row = new ArrayList<>();
                                    field = new StringBuilder();
                                    state = STATES.START;
                                } else {
                                    throw new CSVFormatException("Need LF after CR");
                                }
                                break;

                                // add all other characters
                            default:
                                // convert from numerical value and add to string builder
                                field.append((char) character);
                                break;

                        }
                        break;

                    // when first character is a quote
                    case QUOTE:
                        switch (character) {
                            // check type of next quote encountered
                            case '"':
                                state = STATES.INNER_QUOTE;
                                break;

                            // add all other characters
                            default:
                                // convert from numerical value and add to string builder
                                field.append((char) character);
                                break;
                        }
                        break;

                    // determine if quote was escaped or ending
                    case INNER_QUOTE:
                        switch (character) {
                            // escape quote, go back to outer quote
                            case '"':
                                field.append('"');
                                state = STATES.QUOTE;
                                break;

                            // ending quote found, go to next field
                            case ',':
                                row.add(field.toString());
                                field = new StringBuilder();
                                state = STATES.START;
                                break;

                            // if end of line, add fields and exit
                            case '\n':
                                row.add(field.toString());
                                if (firstLine){
                                    headers = row;
                                    firstLine = false;
                                }else{
                                    processRow(row, headers);
                                }
                                row = new ArrayList<>();
                                field = new StringBuilder();
                                state = STATES.START;
                                break;

                                // need CRLF to end
                            case '\r':
                                character = reader.read();

                                // check if the next character is LF for a proper ending
                                if (character == '\n') {
                                    row.add(field.toString());
                                    if (firstLine){
                                        headers = row;
                                        firstLine = false;
                                    }else{
                                        processRow(row, headers);
                                    }
                                    row = new ArrayList<>();
                                    field = new StringBuilder();
                                    state = STATES.START;
                                } else {
                                    throw new CSVFormatException("Need LF after CR");
                                }
                                break;

                            default:
                                throw new CSVFormatException("Character after closing quote");
                        }
                        break;
                }

                // get next character and loop
                character = reader.read();
            }

            // check state after EOF was reached
            switch(state){

                case START:
                    if (!row.isEmpty() && !firstLine){
                        processRow(row, headers);
                    }
                    break;

                case NO_QUOTE, INNER_QUOTE:
                    // add final field when EOF reached
                    row.add(field.toString());
                    if (!firstLine){
                        processRow(row, headers);
                    }
                    break;

                case QUOTE:
                    // illegal unbalanced quotes
                    throw new CSVFormatException("EOF reached inside open quotes");

            }
        }catch(CSVFormatException e){
            throw new IOException(e.getMessage());
        }

        setData(this.dataType);
    }

    private void processRow(List<String> row, List<String> headers){

        switch (this.dataType)  {

            case COVID:
                int zipIndex = headers.indexOf("zip_code");
                int partially_vaccinated = headers.indexOf("partially_vaccinated");
                int fully_vaccinated = headers.indexOf("fully_vaccinated");
                int etlTimestamp = headers.indexOf("etl_timestamp");

                // ignore any records where the ZIP Code is not 5 digits
                String covZIP = row.get(zipIndex).trim();
                if (covZIP.length() != 5 || !covZIP.matches("\\d{5}")){
                    return;
                }

                // ignore rows with timestamps in incorrect format
                String timeStamp = row.get(etlTimestamp).trim();
                String timeRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$";
                if (!timeStamp.matches(timeRegex)){
                    return;
                }

                CovidRecord cr = new CovidRecord();
                cr.setZipCode(Integer.parseInt(covZIP));
                cr.setEtlTimestamp(timeStamp);
                cr.setPartiallyVaccinated(parseIntValue(row.get(partially_vaccinated)));
                cr.setFullyVaccinated(parseIntValue(row.get(fully_vaccinated)));

                covidRecords.add(cr);
                break;


            case PROPERTY:

                int propZipIndex = headers.indexOf("zip_code");
                int marketValueIndex = headers.indexOf("market_value");
                int totalLivableAreaIndex = headers.indexOf("total_livable_area");

                // ignore records with malformed zipcodes
                String propZIP = row.get(propZipIndex).trim();
                String propZipRegex = "^\\d{5}(-?\\d{0,4})?$";
                if (!propZIP.matches(propZipRegex)){
                    return;
                }

                PropertyRecord pr = new PropertyRecord();
                pr.setZipCode(Integer.parseInt(propZIP.substring(0,5))); // only first 5 digits of zipcode
                pr.setMarketValue(row.get(marketValueIndex));
                pr.setTotalLivableArea(row.get(totalLivableAreaIndex));

                propertyRecords.add(pr);

                break;

            case POPULATION:

                int popZipIndex = headers.indexOf("zip_code");
                int populationIndex = headers.indexOf("population");

                String popZipRegex = "^\\d{5}$";
                // ignore records with malformed zipcodes
                String popZIP = row.get(popZipIndex).trim();
                if (!popZIP.matches(popZipRegex)){
                    return;
                }

                int zip = Integer.parseInt(popZIP);
                int popInt;
                try{
                    popInt = Integer.parseInt(row.get(populationIndex));
                }catch(NumberFormatException e){
                    popInt = 0;
                }

                populationMap.put(zip, popInt);
                break;
        }
    }

    private void setData(DataType dataType){
        DataShare ds = DataShare.getInstance();

        switch(dataType){
            case COVID:
                ds.setCovidData(covidRecords);
                break;

            case PROPERTY:
                ds.setPropertyData(propertyRecords);
                break;

            case POPULATION:
                ds.setPopulationData(populationMap);
                break;
        }

    }

}
