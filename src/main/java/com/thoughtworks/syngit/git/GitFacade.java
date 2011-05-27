package com.thoughtworks.syngit.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitFacade {

    private Repository repository;

    private Git git;

    public GitFacade(File gitDirectory) {
        RepositoryBuilder builder = new RepositoryBuilder();
        try {
            repository = builder.setGitDir(gitDirectory).readEnvironment().findGitDir().build();
            git = new Git(repository);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> findChanges() throws IOException {
        Status status = git.status().call();

        List<File> modifiedFiles = new ArrayList<File>();
        for (String modifiedFileName : status.getModified()) {
            modifiedFiles.add(new File(repository.getWorkTree(), modifiedFileName));
        }

        return modifiedFiles;
    }
}
