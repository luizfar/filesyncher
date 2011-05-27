package com.thoughtworks.syngit.tests;

import java.io.*;

public class TestUtils {

    public static File createFileWithContent(File directory, String name, String content) throws Exception {
        File file = new File(directory, name);
        FileWriter writer = new FileWriter(file);
        writer.append(content);
        writer.flush();
        writer.close();

        return file;
    }

    public static File createTempGitRepository(String name) throws Exception {
        File gitRepositoryFolder = createTempFolder(name);
        execCommands(gitRepositoryFolder, "git init");
        return gitRepositoryFolder;
    }

    public static File createTempFolder(String name) throws Exception {
        File folder = File.createTempFile(name, "");
        folder.delete();
        folder.mkdir();
        return folder;
    }

    public static void execCommands(File dir, String... commands) throws Exception {
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
        in.close();
        out.close();
        process.destroy();
    }
}
