package com.ifedorov.cfbf.tree;

import java.lang.reflect.InvocationTargetException;

public class TreeBuilder<T extends Comparable<T>, N extends Node<N, T>> {

    private final NodeFactory<N, T> nodeFactory;
    private RedBlackTree<T, N> tree;

    public TreeBuilder(NodeFactory<N, T> nodeFactory, RedBlackTree<T, N> tree) {
        this.nodeFactory = nodeFactory;
        this.tree = tree;
    }

    public TreeBuilder(NodeFactory<N, T> nodeFactory) {
        this(nodeFactory, new RedBlackTree<>(nodeFactory));
    }

    public static <T extends Comparable<T>, N extends Node<N, T>> TreeBuilder<T, N> empty(NodeFactory<N, T> nodeFactory) {
        return new TreeBuilder(nodeFactory);
    }

    public TreeBuilder<T, N> rootNode(T value) {
        return rootNode(value, levelBuilder -> {});
    }

    public TreeBuilder<T, N> rootNode(T value, NodeBuilder<T, N> levelBuilder) {
        N node = nodeFactory.create(value, Node.Color.BLACK);
        tree.root(node);
        levelBuilder.accept(new TreeLevel(node, nodeFactory));
        return this;
    }

    public RedBlackTree<T, N> build() {
        return tree;
    }


    public static class TreeLevel<T extends Comparable<T>, N extends Node<N, T>> {
        private N parent;
        private final NodeFactory<N, T> nodeFactory;

        public TreeLevel(N parent, NodeFactory<N, T> nodeFactory) {
            this.parent = parent;
            this.nodeFactory = nodeFactory;
        }

        public void left(T value, Node.Color color, NodeBuilder<T, N> levelBuilder) {
            N node = nodeFactory.create(value, color);
            parent.leftChild(node);
            levelBuilder.accept(new TreeLevel<T, N>(node, nodeFactory));
        }

        public void left(T value, Node.Color color) {
            left(value, color, levelBuilder -> {});
        }

        public void right(T value, Node.Color color, NodeBuilder<T, N> levelBuilder) {
            N node = nodeFactory.create(value, color);
            parent.rightChild(node);
            levelBuilder.accept(new TreeLevel<T, N>(node, nodeFactory));
        }

        public void right(T value, Node.Color color) {
            right(value, color, levelBuilder -> {});
        }
    }
    public interface NodeBuilder<T extends Comparable<T>, N extends Node<N, T>> {
        void accept(TreeLevel<T, N> levelBuilder);
    }
}
