package com.thoughtworks.syngit.util;

import java.io.*;

public class CommandExecutor {

    public String executeIn(File dir, String... commands) {
        try {
            File wd = new File("/bin");
            Process process = Runtime.getRuntime().exec("/bin/bash", null, wd);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(process.getOutputStream())), true);

            out.println("cd " + dir.getAbsolutePath());
            for (String command : commands) {
                out.println(command);
            }
            out.println("exit");

            process.waitFor();
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            in.close();
            out.close();
            process.destroy();

            return stringBuilder.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
