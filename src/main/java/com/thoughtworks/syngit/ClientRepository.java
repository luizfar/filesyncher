package com.thoughtworks.syngit;

import com.thoughtworks.syngit.git.GitFacade;

import java.io.File;

public class ClientRepository {

    private GitFacade git;

    public ClientRepository(GitFacade git) {
        this.git = git;
    }

    public ClientRepository(File gitDirectory) {
        this.git = new GitFacade(gitDirectory);
    }

    public Diff getDiff() {
        return new Diff(git.getCachedDiff(), git.getDiff(), git.getUntrackedFiles());
    }
}
