package com.efimchick.ifmo.io.filetree;

import java.nio.file.Path;

public class FileInfo {
    Path path;
    int nestingLevel;
    boolean isLastInList;
    public FileInfo(Path path, int nestingLevel) {
        this.path = path;
        this.nestingLevel = nestingLevel;
    }

    public boolean isLastInList() {
        return this.isLastInList;
    }

    public boolean isNested() {
        return this.nestingLevel > 0;
    }

    public Path path() {
        return this.path;
    }

    public int getNesting() {
        return this.nestingLevel;
    }

    public void makeLastInList() {
        if(!this.isLastInList)
            this.isLastInList = true;
    }
}
