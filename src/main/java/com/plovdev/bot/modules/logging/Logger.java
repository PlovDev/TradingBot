package com.plovdev.bot.modules.logging;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Logger {
    private OutputStream out = System.out;
    private final List<OutputStream> outs = new ArrayList<>();
    private String name = "LOGGER";

    public Logger() {

    }
    public Logger(String name) {
        this.name = name;
    }
    public Logger(OutputStream stream) {
        out = stream;
    }
    public Logger(String name, OutputStream stream) {
        this.name = name;
        out = stream;
    }

    public synchronized void addOut(OutputStream stream) {
        outs.add(stream);
    }
    public synchronized void setName(String nname) {
        name = nname;
    }
    public synchronized void setOut(OutputStream stream) {
        out = stream;
    }

    public List<OutputStream> getOuts() {
        return outs;
    }
    public String getName() {
        return name;
    }
    public OutputStream getOut() {
        return out;
    }

    public void info(String msg, Object ... objects) {
        log(msg, "INFO", Colors.Green.toString(), objects);
    }
    public void warn(String msg, Object ... objects) {
        log(msg, "WARN", Colors.Yellow.toString(), objects);
    }
    public void error(String msg, Object ... objects) {
        log(msg, "ERROR", Colors.Red.toString(), objects);
    }
    public void blue(String msg, Object ... objects) {
        log(msg, "", Colors.Blue.toString(), objects);
    }

    public void log(String text, String lvl, String color, Object ... objects) {
        try {
            String postfix = "\u001B[0m";

            if (out instanceof FileOutputStream) {
                postfix = "";
                color = "";
            }
            if (text.contains("{}")) {
                text = text.replace("{}", "%s");
                text = String.format(text, objects);
            }

            String outString = color + text + postfix + "\n";
            writeLog(out, outString);


            for (OutputStream os : outs) {
                if (os instanceof FileOutputStream) {
                    postfix = "";
                    color = "";
                }
                String outStr = color + text + postfix + "\n";
                writeLog(os, outStr);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    private synchronized void writeLog(OutputStream stream, String log) throws Exception {
        stream.write(log.getBytes(Charset.defaultCharset()));
        stream.flush();
    }

    public void close() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                out.close();
                for (OutputStream o : outs) o.close();
            } catch (IOException e) {
                error("Error " + e.getMessage());
            }
        }));
    }
}
