package com.ifedorov.cfbf.tree;

import java.util.Objects;

public class RedBlackTree<T extends Comparable<T>, N extends Node<N, T>> {

    private final InsertHandler<T, N> insertHandler;
    private final DeleteHandler<T, N> deleteHandler;
    private N root;

    public RedBlackTree(NodeFactory<N, T> nodeFactory) {
        this.insertHandler = new InsertHandler<T, N>(this, nodeFactory);
        this.deleteHandler = new DeleteHandler<T, N>(this);
    }

    public void delete(N node) {
        this.deleteHandler.delete(node);
    }

    public N insert(T value) {
        return insertHandler.insert(value);
    }

    public N findNode(T value) {
        if(root == null) {
            return null;
        } else {
            N nextNode = root;
            while(nextNode != null) {
                if(nextNode.value().equals(value)) {
                    return nextNode;
                } else {
                    if(value.compareTo(nextNode.value()) > 0) {
                        nextNode = nextNode.rightChild();
                    } else {
                        nextNode = nextNode.leftChild();
                    }
                }
            }
            return null;
        }
    }

    public N root() {
        return root;
    }

    public boolean hasRoot() {
        return root != null;
    }

    public boolean isRoot(N node) {
        return root == node;
    }

    
    public void root(N node) {
        this.root = node;
        if(root != null) {
            this.root.parent(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RedBlackTree<?, ?> that = (RedBlackTree<?, ?>) o;
        return Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
