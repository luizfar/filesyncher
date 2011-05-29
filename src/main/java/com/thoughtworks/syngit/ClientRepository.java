package com.thoughtworks.syngit;

import com.thoughtworks.syngit.git.GitFacade;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientRepository {

    private GitFacade git;

    private Map<String, Long> lastModificationByPath = new HashMap<String, Long>();

    public ClientRepository(GitFacade git) {
        this.git = git;
    }

    public ClientRepository(File gitRepository) {
        this.git = new GitFacade(gitRepository);
    }

    public List<File> findNewlyChangedFiles() {
        List<File> gitChanges = git.findChanges();
        List<File> newChanges = new ArrayList<File>();
        for (File gitChangedFile : gitChanges) {
            Long lastModification = lastModificationByPath.get(gitChangedFile.getAbsolutePath());
            if (lastModification == null || gitChangedFile.lastModified() > lastModification) {
                lastModificationByPath.put(gitChangedFile.getAbsolutePath(), gitChangedFile.lastModified());
                newChanges.add(gitChangedFile);
            }
        }
        return newChanges;
    }
}
