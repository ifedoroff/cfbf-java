package com.ifedorov.cfbf;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ifedorov.cfbf.tree.Node;
import com.ifedorov.cfbf.tree.NodeFactory;
import com.ifedorov.cfbf.tree.RedBlackTree;
import com.ifedorov.cfbf.tree.TreeBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ifedorov.cfbf.Utils.FREESECT_MARK_OR_NOSTREAM_INT;

public class StorageDirectoryEntry extends DirectoryEntry {


    public static final NodeFactory<DirectoryEntryNode, DirectoryEntry> NODE_FACTORY = new NodeFactory<DirectoryEntryNode, DirectoryEntry>() {
        @Override
        public DirectoryEntryNode create(DirectoryEntry value, Node.Color color) {
            return new DirectoryEntryNode(value, color);
        }
    };
    protected final RedBlackDirectoryEntryTree tree;

    public StorageDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view) {
        super(id, directoryEntryChain, view);
        tree = buildTree();
    }

    private RedBlackDirectoryEntryTree buildTree() {
        TreeBuilder treeBuilder = new TreeBuilder(NODE_FACTORY, new RedBlackDirectoryEntryTree(NODE_FACTORY));
        this.getChild().ifPresent(directoryEntry -> {
            treeBuilder.rootNode(directoryEntry, levelBuilder -> buildTreeLevel(directoryEntry, levelBuilder));
        });
        return (RedBlackDirectoryEntryTree) treeBuilder.build();
    }

    private void buildTreeLevel(DirectoryEntry parent, TreeBuilder.TreeLevel levelBuilder) {
        parent.getLeftSibling()
                .ifPresent(leftChild -> levelBuilder.left(leftChild, leftChild.getColorFlag().toNodeColor(), levelBuilder1 -> buildTreeLevel(leftChild, levelBuilder1)));
        parent.getRightSibling()
                .ifPresent(rightChild -> levelBuilder.right(rightChild, rightChild.getColorFlag().toNodeColor(), levelBuilder1 -> buildTreeLevel(rightChild, levelBuilder1)));
    }

    public class RedBlackDirectoryEntryTree extends RedBlackTree<DirectoryEntry, DirectoryEntryNode>{

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
        setChild(entry, view);
    }

    private <T extends DirectoryEntry> T addChild(DirectoryEntry entry) {
        tree.insert(entry);
        return (T) entry;
    }

    public StreamDirectoryEntry addStream(String name, byte[] data) {
        return addChild(directoryEntryChain.createStream(name, ColorFlag.RED, data));
    }

    public StorageDirectoryEntry addStorage(String name) {
        return addChild(directoryEntryChain.createStorage(name, ColorFlag.RED));
    }

    public <T extends DirectoryEntry> T findChild(Predicate<DirectoryEntry> predicate) {
        AtomicReference<DirectoryEntry> result = new AtomicReference<>();
        eachChild((directoryEntry) -> result.set(directoryEntry), predicate);
        return (T)result.get();
    }

    public List<DirectoryEntry> findChildren(Predicate<DirectoryEntry> predicate) {
        List<DirectoryEntry> children = Lists.newArrayList();
        eachChild((directoryEntry) -> {
            if(predicate.test(directoryEntry)) {
                children.add(directoryEntry);
            }
        }, Predicates.alwaysFalse());
        return children;
    }

    public Stream<DirectoryEntry> children() {
        List<DirectoryEntry> children = Lists.newArrayList();
        eachChild(directoryEntry -> {
            children.add(directoryEntry);
        });
        return children.stream();
    }

    public Stream<StorageDirectoryEntry> storages() {
        return children().filter(directoryEntry -> directoryEntry instanceof StorageDirectoryEntry).map(StorageDirectoryEntry.class::cast);
    }

    public Stream<StreamDirectoryEntry> streams() {
        return children().filter(directoryEntry -> directoryEntry instanceof StreamDirectoryEntry).map(StreamDirectoryEntry.class::cast);
    }

    public void eachChild(Consumer<DirectoryEntry> consumer) {
        eachChild(consumer, Predicates.alwaysFalse());
    }

    public void eachChild(Consumer<DirectoryEntry> consumer, Predicate<DirectoryEntry> stopPredicate) {
        Set<Integer> visitedNodes = Sets.newHashSet();
        DirectoryEntryNode currentNode = tree.root();
        if(currentNode == null) {
            return;
        }
        while(true) {
            if(currentNode != null && !visitedNodes.contains(currentNode.value().getId())) {
                visitedNodes.add(currentNode.value().getId());
                consumer.accept(currentNode.value());
                if(stopPredicate.test(currentNode.value())) {
                    break;
                }
            }
            DirectoryEntryNode leftChild = currentNode.leftChild();
            if(leftChild != null && !visitedNodes.contains(leftChild.value().getId())) {
                currentNode = currentNode.leftChild();
                continue;
            }
            DirectoryEntryNode rightChild = currentNode.rightChild();
            if(rightChild != null && !visitedNodes.contains(rightChild.value().getId())) {
                currentNode = currentNode.rightChild();
                continue;
            }
            DirectoryEntryNode parent = currentNode.parent();
            if(parent != null) {
                currentNode = currentNode.parent();
                continue;
            }
            break;
        }
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

    public static class Builder extends DirectoryEntry.Builder<StorageDirectoryEntry> {

        public Builder(int id, DirectoryEntryChain directoryEntryChain, DataView view) {
            super(id, directoryEntryChain, view);
            super.objectType(ObjectType.Storage);
        }

        @Override
        public DirectoryEntry.Builder<StorageDirectoryEntry> objectType(ObjectType type) {
            throw new UnsupportedOperationException("already set in constructor");
        }

        @Override
        public StorageDirectoryEntry build() {
            return new StorageDirectoryEntry(id, directoryEntryChain, view);
        }
    }
}
