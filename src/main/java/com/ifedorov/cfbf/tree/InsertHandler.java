package com.ifedorov.cfbf.tree;

public class InsertHandler<T extends Comparable<T>, N extends Node<N, T>> extends UpdateHandler<T, N> {
    private RedBlackTree<T, N> tree;
    private NodeFactory<N, T> nodeFactory;

    public InsertHandler(RedBlackTree<T, N> tree, NodeFactory<N, T> nodeFactory) {
        super(tree);
        this.tree = tree;
        this.nodeFactory = nodeFactory;
    }

    public N insert(T value) {
        N node = simpleInsert(value);
        if(!tree.isRoot(node) && !tree.isRoot(node.parent())) {
            recolorAndRotateIfNeeded(node);
        }
        return node;
    }

    public N simpleInsert(T value) {
        if(!tree.hasRoot()) {
            N node = nodeFactory.create(value, Node.Color.BLACK);
            tree.root(node);
            return node;
        } else {
            N currentNode = tree.root();
            while(currentNode != null) {
                if(currentNode.value().compareTo(value) == 0) {
                    throw new IllegalArgumentException("Equal values are not supported: " + value);
                } else {
                    Node<N, T> nextNode = null;
                    if(value.compareTo(currentNode.value()) < 0) {
                        if(currentNode.leftChild() == null) {
                            N node = nodeFactory.create(value, Node.Color.RED);
                            currentNode.leftChild(node);
                            return node;
                        } else {
                            currentNode = currentNode.leftChild();
                        }
                    } else {
                        if(currentNode.rightChild() == null) {
                            N node = nodeFactory.create(value, Node.Color.RED);
                            currentNode.rightChild(node);
                            return node;
                        } else {
                            currentNode = currentNode.rightChild();
                        }
                    }
                }
            }
            throw new IllegalStateException("Unexpected behaviour -- cannot find node location in the tree");
        }
    }

    private void recolorAndRotateIfNeeded(N node) {
        N grandChild = node;
        N parent = null;
        while(grandChild != null &&
                grandChild.color() == Node.Color.RED &&
                (parent = grandChild.parent()) != null &&
                parent.color() == Node.Color.RED) {
            N uncle = grandChild.uncle();
            Node.Color uncleColor = uncle == null ? Node.Color.BLACK : uncle.color();
            switch (uncleColor) {
                case BLACK:
                    rotateAndRecolorIfBlackScenario(grandChild);
                    break;
                case RED:
                    recolorIfRedScenario(grandChild);
                    break;
                default:
                    throw new IllegalStateException("Should not pass here");
            }
            grandChild = grandChild.grandParent();
        }
    }

    protected void rotateSubtree(N grandParent, N parent, N grandChild) {
        if(grandParent.isLeftChild(parent) && parent.isLeftChild(grandChild)) {
            rightRotate(grandParent, parent);
        } else if(grandParent.isLeftChild(parent) && parent.isRightChild(grandChild)) {
            leftRotate(parent, grandChild);
            rightRotate(grandParent, grandChild);
            grandChild.color(Node.Color.BLACK);
            grandParent.color(Node.Color.RED);
            assert parent.color() == Node.Color.RED;
        } else if(grandParent.isRightChild(parent) && parent.isRightChild(grandChild)) {
            leftRotate(grandParent, parent);
        } else {
            rightRotate(parent, grandChild);
            leftRotate(grandParent, grandChild);
            grandChild.color(Node.Color.BLACK);
            grandParent.color(Node.Color.RED);
            assert parent.color() == Node.Color.RED;
        }
    }

    private void recolorAfterRotate(N pivot) {
        pivot.color(Node.Color.BLACK);
        N leftChild = pivot.leftChild();
        if(leftChild != null) {
            leftChild.color(Node.Color.RED);
        }
        N rightChild = pivot.rightChild();
        if(rightChild != null) {
            rightChild.color(Node.Color.RED);
        }
    }

    private void recolorIfRedScenario(N grandChild) {
        N uncle = grandChild.uncle();
        if(uncle != null) {
            uncle.color(Node.Color.BLACK);
        }
        N parent = grandChild.parent();
        if(parent != null) {
            parent.color(Node.Color.BLACK);
        }
        N grandParent = grandChild.grandParent();
        if(grandParent != null) {
            if(tree.isRoot(grandParent)) {
                grandParent.color(Node.Color.BLACK);
            } else {
                grandParent.color(Node.Color.RED);
            }
        }
    }

    private void rotateAndRecolorIfBlackScenario(N grandChild) {
        N parent = grandChild.parent();
        N grandParent = grandChild.grandParent();
        rotateSubtree(grandParent, parent, grandChild);
    }
}
