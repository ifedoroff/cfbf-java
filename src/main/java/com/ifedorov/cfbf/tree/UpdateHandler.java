package com.ifedorov.cfbf.tree;

public class UpdateHandler<T extends Comparable<T>, N extends Node<N, T>> {

    private RedBlackTree<T, N> tree;

    public UpdateHandler(RedBlackTree<T, N> tree) {
        this.tree = tree;
    }

    protected void rightRotate(N subTreeRoot, N pivot) {
        N parent = subTreeRoot.parent();
        if(parent == null) {
            subTreeRoot.leftChild(pivot.rightChild());
            pivot.rightChild(subTreeRoot);
            tree.root(pivot);
        } else {
            boolean isLeftSubTree = parent.isLeftChild(subTreeRoot);
            subTreeRoot.leftChild(pivot.rightChild());
            pivot.rightChild(subTreeRoot);
            if(isLeftSubTree) {
                parent.leftChild(pivot);
            } else {
                parent.rightChild(pivot);
            }
        }
        swapColor(subTreeRoot, pivot);
    }

    protected void leftRotate(N subTreeRoot, N pivot) {
        N parent = subTreeRoot.parent();
        if(parent == null) {
            subTreeRoot.rightChild(pivot.leftChild());
            pivot.leftChild(subTreeRoot);
            tree.root(pivot);
        } else {
            boolean isLeftSubTree = parent.isLeftChild(subTreeRoot);
            subTreeRoot.rightChild(pivot.leftChild());
            pivot.leftChild(subTreeRoot);
            if(isLeftSubTree) {
                parent.leftChild(pivot);
            } else {
                parent.rightChild(pivot);
            }
        }
        swapColor(subTreeRoot, pivot);
    }

    private void swapColor(N node1, N node2) {
        Node.Color node1Color = node1.color();
        node1.color(node2.color());
        node2.color(node1Color);
    }
}
