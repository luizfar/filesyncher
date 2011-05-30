package com.thoughtworks.syngit.git;

import com.thoughtworks.syngit.util.CommandExecutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitFacade {

    private Repository repository;

    private Git git;

    private CommandExecutor bash = new CommandExecutor();

    public GitFacade(File gitDirectory) {
        RepositoryBuilder builder = new RepositoryBuilder();
        try {
            repository = builder.setGitDir(gitDirectory).readEnvironment().findGitDir().build();
            git = new Git(repository);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clean() {
        bash.executeIn(repository.getWorkTree(), "git reset --hard HEAD", "git clean -fd");
    }

    public void applyCachedPatch(String cachedPatch) {
        applyPatch(cachedPatch);
        bash.executeIn(repository.getWorkTree(), "git add .");
    }

    public void applyPatch(String patch) {
        File patchFile = createTempPatchWith(patch);
        bash.executeIn(repository.getWorkTree(), "git apply " + patchFile.getAbsolutePath());
    }

    public String getCachedDiff() {
        return bash.executeIn(repository.getWorkTree(), "git diff --cached");
    }

    public String getDiff() {
        return bash.executeIn(repository.getWorkTree(), "git diff");
    }

    public List<File> getUntrackedFiles() {
        try {
            Status status = git.status().call();

            List<File> modifiedFiles = new ArrayList<File>();
            for (String untrackedFileName : status.getUntracked()) {
                modifiedFiles.add(new File(repository.getWorkTree(), untrackedFileName));
            }

            return modifiedFiles;
        } catch (IOException e) {
            throw new GitAccessException(e);
        }
    }

    private File createTempPatchWith(String content) {
        try {
            File patch = File.createTempFile("syngit", "diff");
            FileWriter writer = new FileWriter(patch);
            writer.append(content);
            writer.flush();
            writer.close();

            return patch;
        } catch (IOException ioe) {
            throw new GitAccessException(ioe);
        }
    }
}
