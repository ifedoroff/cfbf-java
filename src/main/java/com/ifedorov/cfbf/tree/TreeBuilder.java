package com.ifedorov.cfbf.tree;

public class TreeBuilder<T extends Comparable<T>, N extends Node<N, T>> {

    private final NodeFactory<N, T> nodeFactory;
    private RedBlackTree<T, N> tree;
    public TreeBuilder(NodeFactory<N, T> nodeFactory) {
        this.tree = new RedBlackTree<>(nodeFactory);
        this.nodeFactory = nodeFactory;
    }

    public static TreeBuilder empty(NodeFactory nodeFactory) {
        return new TreeBuilder(nodeFactory);
    }

    public TreeBuilder rootNode(T value) {
        return rootNode(value, levelBuilder -> {});
    }

    public TreeBuilder rootNode(T value, NodeBuilder levelBuilder) {
        N node = (N) new Node(value, Node.Color.BLACK);
        tree.root(node);
        levelBuilder.accept(new TreeLevel(node, nodeFactory));
        return this;
    }

    public RedBlackTree build() {
        return tree;
    }


    public static class TreeLevel<T extends Comparable<T>, N extends Node<N, T>> {
        private N parent;
        private final NodeFactory<N, T> nodeFactory;

        public TreeLevel(N parent, NodeFactory<N, T> nodeFactory) {
            this.parent = parent;
            this.nodeFactory = nodeFactory;
        }

        public void left(T value, Node.Color color, NodeBuilder levelBuilder) {
            N node = (N) new Node(value, color);
            parent.leftChild(node);
            levelBuilder.accept(new TreeLevel(node, nodeFactory));
        }

        public void left(T value, Node.Color color) {
            left(value, color, levelBuilder -> {});
        }

        public void right(T value, Node.Color color, NodeBuilder levelBuilder) {
            N node = (N) new Node(value, color);
            parent.rightChild(node);
            levelBuilder.accept(new TreeLevel(node, nodeFactory));
        }

        public void right(T value, Node.Color color) {
            right(value, color, levelBuilder -> {});
        }
    }
    public interface NodeBuilder {
        void accept(TreeLevel levelBuilder);
    }
}
