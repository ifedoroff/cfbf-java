package com.ifedorov.cfbf.tree;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Node<N extends Node, T extends Comparable<T>> implements Comparable<Node<N, T>>{

    public enum Color {
        RED, BLACK
    }

    public Node(T value, Color color) {
        this.color = color;
        Preconditions.checkNotNull(value, "Null values are not allowed");
        this.value = value;
    }
    private T value;
    private N leftChild;
    private N rightChild;
    private N parent;
    private Color color;

    @Override
    public int compareTo(Node<N, T> o) {
        return this.value.compareTo(o.value());
    }

    public N leftChild() {
        return leftChild;
    }

    public void leftChild(N value) {
        this.leftChild = value;
        if(this.leftChild != null) {
            this.leftChild.parent(this);
        }
    }

    public T value() {
        return value;
    }

    public void rightChild(N value) {
        this.rightChild = value;
        if(this.rightChild != null) {
            this.rightChild.parent(this);
        }
    }

    public N rightChild() {
        return rightChild;
    }


    public N parent() {
        return parent;
    }


    public void parent(N parent) {
        this.parent = parent;
    }


    public boolean hasChildren() {
        return leftChild != null || rightChild != null;
    }

    public boolean hasTwoChildren() {
        return leftChild != null && rightChild != null;
    }

    public boolean isLeftChild(N node) {
        return leftChild == node;
    }

    public boolean isRightChild(N node) {
        return rightChild == node;
    }

    public void deleteChild(N node) {
        if(node == this.leftChild) {
            this.leftChild = null;
        } else if(node == this.rightChild) {
            this.rightChild = null;
        }
    }

    public void substituteNode(N node, N substitute) {
        if(node == this.rightChild) {
            this.rightChild = substitute;
            this.rightChild.parent(this);
        } else if(node == this.leftChild) {
            this.leftChild = substitute;
            this.leftChild.parent(this);
        }
    }

    public N inOrderPredecessor() {
        if(this.leftChild == null) {
            return null;
        } else {
            List<N> allChildren = this.leftChild.getChildrenRecursive();
            allChildren.add(this.leftChild);
            Collections.sort(allChildren);
            return allChildren.get(allChildren.size() - 1);
        }
    }

    public List<N> getChildrenRecursive() {
        List<N> allChildren = Lists.newArrayList();
        if(this.leftChild != null) {
            allChildren.addAll(this.leftChild.getChildrenRecursive());
            allChildren.add(this.leftChild);
        }
        if(this.rightChild != null) {
            allChildren.addAll(this.rightChild.getChildrenRecursive());
            allChildren.add(this.rightChild);
        }
        return allChildren;
    }

    public Color color() {
        return color;
    }

    public void color(Color color) {
        this.color = color;
    }

    public void invertColor() {
        this.color = this.color == Color.BLACK ? Color.RED : Color.BLACK;
    }

    public N uncle() {
        N parent = this.parent();
        N grandParent = (N) parent.parent();
        if(parent != null && grandParent != null) {
            if (grandParent.isLeftChild(parent)) {
                return (N) grandParent.rightChild();
            } else {
                return (N) grandParent.leftChild();
            }
        }
        return null;
    }

    public N grandParent() {
        N grandParent = null;
        if(this.parent() != null) {
            grandParent = (N) this.parent().parent();
        }
        return grandParent;
    }

    public N sibling() {
        if(parent() == null) {
            return null;
        } else if(parent().isLeftChild(this)){
            return (N) parent().rightChild();
        } else {
            return (N) parent().leftChild();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?, ?> node = (Node<?, ?>) o;
        return Objects.equals(value, node.value) &&
                Objects.equals(leftChild, node.leftChild) &&
                Objects.equals(rightChild, node.rightChild) &&
                color == node.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, leftChild, rightChild, color);
    }
}
