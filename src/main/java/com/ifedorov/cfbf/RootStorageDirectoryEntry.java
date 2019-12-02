package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;

public class RootStorageDirectoryEntry extends StorageDirectoryEntry {

    public RootStorageDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view, StreamRW streamReader) {
        super(id, "Root", ColorFlag.BLACK, ObjectType.RootStorage, directoryEntryChain, view, streamReader);
    }

    @Override
    protected void setRightSibling(DirectoryEntry rightSibling) {
        throw new IllegalStateException("Root Storage cannot have siblings");
    }

    @Override
    protected void setLeftSibling(DirectoryEntry leftSibling) {
        throw new IllegalStateException("Root Storage cannot have siblings");
    }

    @Override
    public void setDirectoryEntryName(String name) {
        if(!"Root".equals(name)) {
            throw new IllegalStateException("Name of Root Storage directory entry is always 'Root'");
        }
        super.setDirectoryEntryName(name);
    }
}
