package com.ifedorov.cfbf.tree;

public class DeleteHandler<T extends Comparable<T>, N extends Node<N, T>> extends UpdateHandler<T, N> {
    private RedBlackTree<T, N> tree;

    public DeleteHandler(RedBlackTree<T, N> tree) {
        super(tree);
        this.tree = tree;
    }

    public void delete(N node) {
        if(!node.hasChildren()) {
            if(tree.isRoot(node)) {
                tree.root(null);
            } else {
                if (node.color() == Node.Color.BLACK) {
                    N sibling = node.sibling();
                    node.parent().deleteChild(node);
                    recover(sibling);
                } else {
                    node.parent().deleteChild(node);
                }
            }
        } else if(node.hasTwoChildren()) {
            N substituteWith = node.inOrderPredecessor();
            swap(node, substituteWith);
            delete(node);
        } else {
            N substituteWith = null;
            if(node.rightChild() == null) {
                substituteWith = node.leftChild();
            } else {
                substituteWith = node.rightChild();
            }
            if(tree.isRoot(node)) {
                tree.root(substituteWith);
                substituteWith.color(Node.Color.BLACK);
            } else if(substituteWith.color() == Node.Color.RED || node.color() == Node.Color.RED) {
                node.parent().substituteNode(node, substituteWith);
                substituteWith.color(Node.Color.BLACK);
            } else {
                N sibling = node.sibling();
                node.parent().substituteNode(node, substituteWith);
                recover(sibling);
            }
        }
    }

    private void recover(N sibling) {
        Node.Color siblingColor = sibling.color();
        Node siblingsLeftChild = sibling.leftChild();
        Node siblingsRightChild = sibling.rightChild();
        Node.Color siblingLeftChildColor = siblingsLeftChild == null ? Node.Color.BLACK : siblingsLeftChild.color();
        Node.Color siblingRightChildColor = siblingsRightChild == null ? Node.Color.BLACK : siblingsRightChild.color();
        boolean isSiblingLeftChild = sibling.parent().isLeftChild(sibling);
        if(siblingColor == Node.Color.BLACK) {
            if(siblingLeftChildColor == Node.Color.RED || siblingRightChildColor == Node.Color.RED) {
                if(sibling.parent().isLeftChild(sibling)) {
                    if(siblingLeftChildColor == Node.Color.RED) {
                        rightRotate(sibling.parent(), sibling);
                        siblingsLeftChild.color(Node.Color.BLACK);
                    } else {
                        leftRotate(sibling, sibling.rightChild());
                        rightRotate(sibling.grandParent(), sibling.parent());
                        sibling.color(Node.Color.BLACK);
                    }
                } else {
                    if(siblingRightChildColor == Node.Color.RED) {
                        leftRotate(sibling.parent(), sibling);
                        siblingsRightChild.color(Node.Color.BLACK);
                    } else {
                        rightRotate(sibling, sibling.leftChild());
                        leftRotate(sibling.grandParent(), sibling.parent());
                        sibling.color(Node.Color.BLACK);
                    }
                }
            } else {
                sibling.color(Node.Color.RED);
                N parent = sibling.parent();
                if(parent.color() == Node.Color.BLACK && !tree.isRoot(parent)) {
                    recover(parent.sibling());
                } else {
                    parent.color(Node.Color.BLACK);
                }
            }
        } else {
            N parent = sibling.parent();
            N newSibling = null;
            sibling.color(Node.Color.BLACK);
            parent.color(Node.Color.RED);
            if(isSiblingLeftChild) {
                newSibling = sibling.rightChild();
                rightRotate(sibling.parent(), sibling);
            } else {
                newSibling = sibling.leftChild();
                leftRotate(sibling.parent(), sibling);
            }
            recover(newSibling);
        }
    }

    private void swap(N node1, N node2) {
        if(node1.parent() == node2) {
            swapChildParent(node2, node1);
        } else if(node2.parent() == node1) {
            swapChildParent(node1, node2);
        } else {
            N node1Parent = node1.parent();
            N node1LeftChild = node1.leftChild();
            N node1RightChild = node1.rightChild();
            N node2Parent = node2.parent();
            N node2LeftChild = node2.leftChild();
            N node2RightChild = node2.rightChild();
            Node.Color node1Color = node1.color();
            Node.Color node2Color = node2.color();
            node1.leftChild(node2LeftChild);
            node1.rightChild(node2RightChild);
            node2.leftChild(node1LeftChild);
            node2.rightChild(node1RightChild);
            node1.color(node2Color);
            node2.color(node1Color);
            if(node1Parent == null) {
                tree.root(node2);
            } else {
                node1Parent.substituteNode(node1, node2);
            }
            if(node2Parent == null) {
                tree.root(node1);
            } else {
                node2Parent.substituteNode(node2, node1);
            }
        }
    }

    private void swapChildParent(N parent, N child) {
        Node.Color parentColor = parent.color();
        Node.Color childColor = child.color();
        N leftGrandChild = child.leftChild();
        N rightGrandChild = child.rightChild();
        N grandParent = parent.parent();
        if(grandParent == null) {
            tree.root(child);
        } else if(grandParent.isLeftChild(parent)) {
            grandParent.leftChild(child);
        } else {
            grandParent.rightChild(child);
        }
        if(parent.isLeftChild(child)) {
            N rightChild = parent.rightChild();
            child.leftChild(parent);
            child.rightChild(rightChild);
        } else {
            N leftChild = parent.leftChild();
            child.rightChild(parent);
            child.leftChild(leftChild);
        }
        child.color(parentColor);
        parent.leftChild(leftGrandChild);
        parent.rightChild(rightGrandChild);
        parent.color(childColor);
    }
}
