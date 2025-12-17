package edu.upenn.cit594.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileValidator {

    // validates that the given file can be opened and read
    public static void validateFile(String filename, String type) throws IOException {

            File file = new File(filename);

            if (!file.exists()){
                throw new FileNotFoundException(type + " file does not exist.");
            }
            if (!file.canRead()){
                throw new IOException(type + " file cannot be read.");
            }

    }
}
