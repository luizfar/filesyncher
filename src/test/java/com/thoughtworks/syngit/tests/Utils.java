package com.thoughtworks.syngit.tests;

import com.thoughtworks.syngit.util.CommandExecutor;

import java.io.File;
import java.io.FileWriter;

public class Utils {

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
        new CommandExecutor().executeIn(dir, commands);
    }
}
