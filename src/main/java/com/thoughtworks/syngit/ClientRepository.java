package com.thoughtworks.syngit;

import com.thoughtworks.syngit.git.GitFacade;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRepository {

    private static final String EMPTY = "";

    private static final Diff EMPTY_DIFF = new Diff(EMPTY, EMPTY, new ArrayList<File>());

    private GitFacade git;

    private Diff lastSent = EMPTY_DIFF;

    public ClientRepository(File gitDirectory) {
        this.git = new GitFacade(gitDirectory);
    }

    public Diff getDiff() {
        Diff diff = new Diff(git.getCachedDiff(), git.getDiff(), git.getUntrackedFiles());

        if (diff.equals(lastSent)) return EMPTY_DIFF;

        lastSent = diff;
        return diff;
    }
}
