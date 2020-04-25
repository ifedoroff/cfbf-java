package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;
import com.ifedorov.cfbf.tree.Node;

import java.util.Optional;

public class RootStorageDirectoryEntry extends StorageDirectoryEntry {

    public static final String NAME = "Root Entry";
    public static final int ID = 0;

    public RootStorageDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view) {
        super(id, directoryEntryChain, view);
    }

    @Override
    public Optional<DirectoryEntry> getChild() {
        if(getChildPosition() == ID) {
            throw new IllegalStateException("Root Entry child cannot have ID == 0");
        }
        return super.getChild();
    }

    @Override
    protected void setRightSibling(DirectoryEntry rightSibling) {
        if(rightSibling != null) {
            throw new IllegalStateException("Root Storage cannot have siblings");
        }
    }

    @Override
    protected void setLeftSibling(DirectoryEntry leftSibling) {
        if(leftSibling != null) {
            throw new IllegalStateException("Root Storage cannot have siblings");
        }
    }

    @Override
    public void setDirectoryEntryName(String name) {
        if(!"Root Entry".equals(name)) {
            throw new IllegalStateException("Name of Root Storage directory entry is always '" + NAME + "'");
        }
        super.setDirectoryEntryName(name);
    }

    public static class Builder extends DirectoryEntry.Builder<RootStorageDirectoryEntry> {

        public Builder(int id, DirectoryEntryChain directoryEntryChain, DataView view) {
            super(id, directoryEntryChain, view);
            super.objectType(ObjectType.RootStorage);
            super.color(ColorFlag.BLACK);
            super.name(NAME);
        }

        @Override
        public DirectoryEntry.Builder<RootStorageDirectoryEntry> name(String name) {
            throw new UnsupportedOperationException("already set in constructor");
        }

        @Override
        public DirectoryEntry.Builder<RootStorageDirectoryEntry> color(ColorFlag colorFlag) {
            throw new UnsupportedOperationException("already set in constructor");
        }

        @Override
        public DirectoryEntry.Builder<RootStorageDirectoryEntry> objectType(ObjectType type) {
            throw new UnsupportedOperationException("already set in constructor");
        }

        @Override
        public DirectoryEntry.Builder<RootStorageDirectoryEntry> leftSibling(DirectoryEntry entry) {
            if(entry != null) {
                throw new UnsupportedOperationException("Root Storage cannot have siblings");
            } else {
                super.leftSibling(null);
                return this;
            }
        }

        @Override
        public DirectoryEntry.Builder<RootStorageDirectoryEntry> rightSibling(DirectoryEntry entry) {
            if(entry != null) {
                throw new UnsupportedOperationException("Root Storage cannot have siblings");
            } else {
                super.rightSibling(null);
                return this;
            }
        }

        @Override
        public RootStorageDirectoryEntry build() {
            return new RootStorageDirectoryEntry(id, directoryEntryChain, view);
        }

    }
}
