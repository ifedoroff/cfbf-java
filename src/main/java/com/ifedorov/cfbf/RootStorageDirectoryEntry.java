package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;
import com.ifedorov.cfbf.tree.Node;

import java.util.Optional;

public class RootStorageDirectoryEntry extends StorageDirectoryEntry {

    public static final String NAME = "Root Entry";
    public static final int ID = 0;

    public RootStorageDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view) {
        super(id, NAME, ColorFlag.BLACK, ObjectType.RootStorage, directoryEntryChain, view);
        this.getChild().ifPresent(child -> {
            tree.root(new DirectoryEntryNode(child, Node.Color.BLACK));
        });
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
}
