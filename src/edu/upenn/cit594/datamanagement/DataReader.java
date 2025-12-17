package edu.upenn.cit594.datamanagement;

import edu.upenn.cit594.logging.Logger;
import edu.upenn.cit594.util.FileValidator;

import java.io.IOException;

public abstract class DataReader {

    public enum DataType {
        COVID,
        PROPERTY,
        POPULATION
    }

    protected String filename;

    protected DataType dataType;

    public static DataReader determineFileType(String filename, DataType dataType) throws IOException {

        if (filename == null || dataType == null){
            throw new IllegalArgumentException("Missing file or data type.");
        }

        // validate that the file can be opened and read
        FileValidator.validateFile(filename, String.valueOf(dataType));

        String filenameLower = filename.toLowerCase();
        if (filenameLower.endsWith(".csv")) {
            Logger.getInstance().log(filename);
            return new CSVReader(filename, dataType);
        } else if (filenameLower.endsWith(".json")) {
            if (dataType != DataType.COVID){
                throw new IllegalArgumentException("Invalid file format.");
            }
            Logger.getInstance().log(filename);
            return new JSONReader(filename, dataType);
        } else {
            throw new IllegalArgumentException("Invalid file format.");
        }
    }

    public static DataType getDataType(String type) {
        if (type.equals("covid")){
            return DataType.COVID;
        }else if(type.equals("population")){
            return DataType.POPULATION;
        }else if(type.equals("properties")) {
            return DataType.PROPERTY;
        }
        return null;
    }

    public abstract void readFile() throws IOException;

    // return 0 when data is malformed so that they aren't accounted for in processor
    protected int parseIntValue(String value){
        if (value.isEmpty()){
            return 0;
        }else{
            try{
                return Integer.parseInt(value.trim());
            }catch (NumberFormatException e){
                return 0;
            }
        }
    }


}
