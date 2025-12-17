package edu.upenn.cit594.logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Singleton logger used to log events. Log format is: "currentTime logMessage"
 */
public class Logger {

    private PrintWriter out = new PrintWriter(System.err);
    private boolean printWithSysErr = true;
    private static Logger instance;
    private Queue<String> queue = new LinkedList<>();

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null){
            instance = new Logger();
        }

        return instance;
    }

    public void setOutput(String filename) {

        // close file if one already exists
        if (!printWithSysErr && out != null){
            out.close();
        }
        try{
            out = new PrintWriter(new FileWriter(filename, true));
            printWithSysErr = false;

            while (!this.queue.isEmpty()) {
                out.println(System.currentTimeMillis() + " " + this.queue.poll());
            }
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("Cannot open logger file.");
        }
    }


    public void log(String message) {
        if (out == null) {
            this.queue.add(message);
            return;
        }
        out.println(System.currentTimeMillis() + " " + message);
        out.flush();

    }

    public void close() {
        if (!printWithSysErr && out != null) {
            out.close();
        }
        out = null;
    }



}
