package com.ifedorov.cfbf;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public class RedBlackTree {

    private Node root;

    public void delete(DirectoryEntry value) {
        RedBlackTree.Node node = findNode(value);
        if(node != null) {
            delete(node);
        }
    }

    protected void delete(Node node) {
        if(!node.hasChildren()) {
            node.parent().deleteChild(node);
        } else if(node.hasTwoChildren()) {
            RedBlackTree.Node substituteWith = node.inOrderSuccessor();
            delete(substituteWith.value());
            node.value(substituteWith.value());
        } else {
            if(node.right() == null) {
                node.parent().substituteNode(node, node.left());
            } else {
                node.parent().substituteNode(node, node.right());
            }
        }
    }

    
    public RedBlackTree.Node insert(DirectoryEntry value) {
        if(root == null) {
            return root = createRoot(value);
        } else {
            RedBlackTree.Node currentNode = root;
            while(currentNode != null) {
                if(currentNode.value().compareTo(value) == 0) {
                    throw new IllegalArgumentException("Equal values are not supported");
                } else {
                    RedBlackTree.Node nextNode = null;
                    if(value.compareTo(currentNode.value()) < 0) {
                        if(currentNode.left() == null) {
                            return currentNode.left(value);
                        } else {
                            currentNode = currentNode.left();
                        }
                    } else {
                        if(currentNode.right() == null) {
                            return currentNode.right(value);
                        } else {
                            currentNode = currentNode.right();
                        }
                    }
                }
            }
            throw new IllegalStateException("Unexpected behaviour -- cannot find node location in the tree");
        }
    }

    public RedBlackTree.Node findNode(DirectoryEntry value) {
        if(root == null) {
            return null;
        } else {
            RedBlackTree.Node nextNode = root;
            while(nextNode != null) {
                if(nextNode.value().equals(value)) {
                    return nextNode;
                } else {
                    if(value.compareTo(nextNode.value()) > 0) {
                        nextNode = nextNode.right();
                    } else {
                        nextNode = nextNode.left();
                    }
                }
            }
            return null;
        }
    }

    
    public RedBlackTree.Node root() {
        return root;
    }

    
    public void root(Node node) {
        this.root = node;
        this.root.parent(null);
    }

    protected RedBlackTree.Node createRoot(DirectoryEntry value) {
        return new Node(value);
    }

    protected void rotateSubtree(Node root, Node child, Node grandChild) {
        if(root.isLeftChild(child) && child.isLeftChild(grandChild)) {
            rightRotate(root, child);
        } else if(root.isLeftChild(child) && child.isRightChild(grandChild)) {
            leftRotate(child, grandChild);
            rightRotate(root, grandChild);
        } else if(root.isRightChild(child) && child.isRightChild(grandChild)) {
            leftRotate(root, child);
        } else {
            rightRotate(child, grandChild);
            leftRotate(root, grandChild);
        }
    }

    protected void rightRotate(Node subTreeRoot, Node pivot) {
        Node parent = subTreeRoot.parent();
        if(parent == null) {
            subTreeRoot.left(pivot.right());
            pivot.right(subTreeRoot);
            root(pivot);
        } else {
            boolean isLeftSubTree = parent.isLeftChild(subTreeRoot);
            subTreeRoot.left(pivot.right());
            pivot.right(subTreeRoot);
            if(isLeftSubTree) {
                parent.left(pivot);
            } else {
                parent.right(pivot);
            }
        }
    }

    protected void leftRotate(Node subTreeRoot, Node pivot) {
        Node parent = subTreeRoot.parent();
        if(parent == null) {
            subTreeRoot.right(pivot.left());
            pivot.left(subTreeRoot);
            root(pivot);
        } else {
            boolean isLeftSubTree = parent.isLeftChild(subTreeRoot);
            subTreeRoot.right(pivot.left());
            pivot.left(subTreeRoot);
            if(isLeftSubTree) {
                parent.left(pivot);
            } else {
                parent.right(pivot);
            }
        }
    }

    public static class Node implements Comparable<Node>{

        public Node(DirectoryEntry value) {
            Preconditions.checkNotNull(value, "Null values are not allowed");
            this.value = value;
        }
        private DirectoryEntry value;
        private Node left;
        private Node right;
        private Node parent;

        
        public int compareTo(Node o) {
            return this.value.compareTo(o.value());
        }

        
        public Node left() {
            return left;
        }

        
        public Node left(DirectoryEntry value) {
            Node node = new Node(value);
            left(node);
            return node;
        }

        
        public void left(Node value) {
            this.left = (Node) value;
            if(this.left != null) {
                this.left.parent(this);
            }
        }

        public DirectoryEntry value() {
            return value;
        }

        
        public void value(DirectoryEntry value) {
            this.value = value;
        }

        
        public Node right(DirectoryEntry value) {
            Node node = new Node(value);
            right(node);
            return node;
        }

        
        public void right(Node value) {
            this.right = (Node) value;
            if(this.right != null) {
                this.right.parent(this);
            }
        }
        
        public Node right() {
            return right;
        }

        
        public Node parent() {
            return parent;
        }

        
        public void parent(Node parent) {
            this.parent = (Node) parent;
        }

        
        public boolean hasChildren() {
            return left != null || right != null;
        }

        public boolean hasTwoChildren() {
            return left != null && right != null;
        }

        public boolean isLeftChild(Node node) {
            return left == node;
        }

        public boolean isRightChild(Node node) {
            return right == node;
        }

        public void deleteChild(Node node) {
            if(node == this.left) {
                this.left = null;
            } else if(node == this.right) {
                this.right = null;
            }
        }

        public void substituteNode(Node node, Node substitute) {
            if(node == this.right) {
                this.right = substitute;
            } else if(node == this.left) {
                this.left = substitute;
            }
        }

        public Node inOrderSuccessor() {
            if(this.right == null) {
                return null;
            } else {
                List<Node> allChildren = this.right.getChildrenRecursive();
                allChildren.add(this.right);
                Collections.sort(allChildren);
                return allChildren.get(0);
            }
        }

        public List<Node> getChildrenRecursive() {
            List<Node> allChildren = Lists.newArrayList();
            if(this.left != null) {
                allChildren.addAll(this.left.getChildrenRecursive());
                allChildren.add(this.left);
            }
            if(this.right != null) {
                allChildren.addAll(this.right.getChildrenRecursive());
                allChildren.add(this.right);
            }
            return allChildren;
        }

        public DirectoryEntry.ColorFlag color() {
            return value.getColorFlag();
        }

        public void color(DirectoryEntry.ColorFlag color) {
            value.setColorFlag(color);
        }

        public Node uncle() {
            Node parent = this.parent();
            Node grandParent = parent.parent();
            if(parent != null && grandParent != null) {
                if (grandParent.isLeftChild(parent)) {
                    return (Node) grandParent.right();
                } else {
                    return (Node) grandParent.left();
                }
            }
            return null;
        }

        public Node grandParent() {
            Node greatParent = null;
            if(this.parent() != null) {
                greatParent = (Node) this.parent().parent();
            }
            return greatParent;
        }

        public Node sibling() {
            if(parent() == null) {
                return null;
            } else if(parent().isLeftChild(this)){
                return parent().right();
            } else {
                return parent().left();
            }
        }
    }

}
