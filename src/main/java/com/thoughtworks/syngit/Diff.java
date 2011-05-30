package com.thoughtworks.syngit;

import java.io.File;
import java.util.List;

public class Diff {

    private String cachedPatch;

    private String patch;

    private List<File> newFiles;

    public Diff(String cachedPatch, String patch, List<File> newFiles) {
        this.cachedPatch = cachedPatch;
        this.patch = patch;
        this.newFiles = newFiles;
    }

    public String getCachedPatch() {
        return cachedPatch;
    }

    public String getPatch() {
        return patch;
    }

    public List<File> getNewFiles() {
        return newFiles;
    }

    public boolean hasChanges() {
        return !getCachedPatch().trim().isEmpty() || !getPatch().trim().isEmpty() || !getNewFiles().isEmpty();
    }

    @Override
    public int hashCode() {
        return cachedPatch.hashCode() + patch.hashCode() + newFiles.hashCode() * 31;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Diff)) {
            return false;
        }
        Diff otherDiff = (Diff) other;
        return otherDiff.cachedPatch.equals(cachedPatch) &&
                otherDiff.patch.equals(patch) &&
                otherDiff.newFiles.equals(newFiles);
    }
}
