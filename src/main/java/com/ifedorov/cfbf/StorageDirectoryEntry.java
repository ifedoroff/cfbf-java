package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;
import com.ifedorov.cfbf.tree.Node;
import com.ifedorov.cfbf.tree.NodeFactory;
import com.ifedorov.cfbf.tree.RedBlackTree;

public class StorageDirectoryEntry extends DirectoryEntry {


    public static final NodeFactory<DirectoryEntryNode, DirectoryEntry> NODE_FACTORY = new NodeFactory<DirectoryEntryNode, DirectoryEntry>() {
        @Override
        public DirectoryEntryNode create(DirectoryEntry value, Node.Color color) {
            return new DirectoryEntryNode(value, color);
        }
    };
    private final RedBlackDirectoryEntryTree tree = new RedBlackDirectoryEntryTree(NODE_FACTORY);

    public StorageDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view, StreamRW streamReader) {
        super(id, directoryEntryChain, view, streamReader);
    }

    public StorageDirectoryEntry(int id, String name, ColorFlag colorFlag, ObjectType objectType, DirectoryEntryChain directoryEntryChain, DataView view, StreamRW streamReader) {
        super(id, name, colorFlag, objectType, directoryEntryChain, view, streamReader);
    }

    public StorageDirectoryEntry(int id, String name, ColorFlag colorFlag, DirectoryEntryChain directoryEntryChain, DataView view, StreamRW streamReader) {
        super(id, name, colorFlag, ObjectType.Storage, directoryEntryChain, view, streamReader);
    }

    public class RedBlackDirectoryEntryTree extends RedBlackTree<DirectoryEntry, DirectoryEntryNode> {

        public RedBlackDirectoryEntryTree(NodeFactory<DirectoryEntryNode, DirectoryEntry> nodeFactory) {
            super(nodeFactory);
        }

        @Override
        public void root(DirectoryEntryNode node) {
            super.root(node);
            StorageDirectoryEntry.this.setChild(node.value());
        }
    }

    private void setChild(DirectoryEntry entry) {
        view.subView(FLAG_POSITION.CHILD, FLAG_POSITION.CHILD + 4).writeAt(0, Utils.toBytes(entry.getId(), 4));
    }

    public void addChild(DirectoryEntry entry) {
        tree.insert(entry);
    }

    public static class DirectoryEntryNode extends Node<DirectoryEntryNode, DirectoryEntry> {

        public DirectoryEntryNode(DirectoryEntry value, Color color) {
            super(value, color);
            color(color);
        }

        @Override
        public void leftChild(DirectoryEntryNode leftChild) {
            super.leftChild(leftChild);
            value().setLeftSibling(leftChild == null ? null : leftChild.value());
        }

        @Override
        public void rightChild(DirectoryEntryNode rightChild) {
            super.rightChild(rightChild);
            value().setRightSibling(rightChild == null ? null : rightChild.value());
        }

        @Override
        public void deleteChild(DirectoryEntryNode node) {
            if(isLeftChild(node)) {
                value().setLeftSibling(null);
            } else if(isRightChild(node)) {
                value().setRightSibling(null);
            }
            super.deleteChild(node);
        }

        @Override
        public void substituteNode(DirectoryEntryNode node, DirectoryEntryNode substitute) {
            if(isRightChild(node)) {
                value().setRightSibling(substitute.value());
            } else if(isLeftChild(node)) {
                value().setLeftSibling(substitute.value());
            }
            super.substituteNode(node, substitute);
        }

        @Override
        public void color(Color color) {
            super.color(color);
            value().setColorFlag(ColorFlag.fromNodeColor(color));
        }

        @Override
        public void invertColor() {
            super.invertColor();
            value().invertColor();
        }
    }
}
