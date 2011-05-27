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

    public List<File> findChanges(File gitDirectory) throws IOException {
        RepositoryBuilder builder = new RepositoryBuilder();
        Repository repository = builder.setGitDir(gitDirectory).readEnvironment().findGitDir().build();
        Git git = new Git(repository);
        Status status = git.status().call();

        List<File> modifiedFiles = new ArrayList<File>();
        for (String modifiedFileName : status.getModified()) {
            modifiedFiles.add(new File(repository.getWorkTree(), modifiedFileName));
        }

        return modifiedFiles;
    }
}
