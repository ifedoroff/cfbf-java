package com.ifedorov.cfbf;

import com.ifedorov.cfbf.tree.*;
import com.ifedorov.cfbf.tree.Node.Color;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedBlackTreeTest {

    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
                new Student("John", 3),
                new Student("Mark", 4)
        );

        acceptAllEmployee(students, e -> System.out.println(e.name));
        acceptAllEmployee(students, e -> {
            e.gpa *= 1.5;
        });
        acceptAllEmployee(students, e -> System.out.println(e.name + ": " + e.gpa));
    }

    public static void acceptAllEmployee(List<Student> student, Consumer<Student> printer) {
        for (Student e : student) {
            printer.accept(e);
        }
    }

    static class Student<N extends String> {
        public N name;
        public double gpa;

        Student(N name, double g) {
            this.name = name;
            this.gpa = g;
        }
    }

    private final static NodeFactory<Node, Integer> nodeFactory = new NodeFactory<Node, Integer>() {
        @Override
        public Node create(Integer value, Color color) {
            return new Node(value, color);
        }
    };

    @Test
    public void testInsertRoot() {
        RedBlackTree tree = new RedBlackTree<>(nodeFactory);
        tree.insert(1);
        assertEquals(Color.BLACK, tree.root().color());
    }

    @Test
    public void testInsertMostlyLeft() {
        RedBlackTree tree = new RedBlackTree<>(nodeFactory);
        try {
            tree.insert(10);
            assertEquals(Color.BLACK, tree.root().color());

            tree.insert(20);
            assertEquals(Color.RED, tree.root().rightChild().color());
            assertEquals(20, tree.root().rightChild().value());

            tree.insert(30);
            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(20, tree.root().value());
            assertEquals(Color.RED, tree.root().rightChild().color());
            assertEquals(30, tree.root().rightChild().value());
            assertEquals(Color.RED, tree.root().leftChild().color());
            assertEquals(10, tree.root().leftChild().value());

            tree.insert(15);
            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(20, tree.root().value());
            assertEquals(Color.BLACK, tree.root().rightChild().color());
            assertEquals(30, tree.root().rightChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().color());
            assertEquals(10, tree.root().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().rightChild().color());
            assertEquals(15, tree.root().leftChild().rightChild().value());

            tree.insert(18);
            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(20, tree.root().value());
            assertEquals(Color.BLACK, tree.root().rightChild().color());
            assertEquals(30, tree.root().rightChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().color());
            assertEquals(15, tree.root().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().color());
            assertEquals(10, tree.root().leftChild().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().rightChild().color());
            assertEquals(18, tree.root().leftChild().rightChild().value());

            tree.insert(9);
            assertEquals(Color.RED, tree.root().leftChild().color());
            assertEquals(15, tree.root().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().leftChild().color());
            assertEquals(10, tree.root().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().color());
            assertEquals(18, tree.root().leftChild().rightChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().leftChild().color());
            assertEquals(9, tree.root().leftChild().leftChild().leftChild().value());

            tree.insert(8);
            assertEquals(Color.BLACK, tree.root().leftChild().leftChild().color());
            assertEquals(9, tree.root().leftChild().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().rightChild().color());
            assertEquals(10, tree.root().leftChild().leftChild().rightChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().leftChild().color());
            assertEquals(8, tree.root().leftChild().leftChild().leftChild().value());

            tree.insert(7);

            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(15, tree.root().value());
            assertEquals(Color.RED, tree.root().rightChild().color());
            assertEquals(20, tree.root().rightChild().value());
            assertEquals(Color.RED, tree.root().leftChild().color());
            assertEquals(9, tree.root().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().color());
            assertEquals(10, tree.root().leftChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().leftChild().color());
            assertEquals(8, tree.root().leftChild().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().leftChild().color());
            assertEquals(7, tree.root().leftChild().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().rightChild().color());
            assertEquals(30, tree.root().rightChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().leftChild().color());
            assertEquals(18, tree.root().rightChild().leftChild().value());

            tree.insert(6);
            tree.insert(5);
            tree.insert(4);
            tree.insert(3);

            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(15, tree.root().value());
            assertEquals(Color.BLACK, tree.root().leftChild().color());
            assertEquals(7, tree.root().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().color());
            assertEquals(5, tree.root().leftChild().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().rightChild().color());
            assertEquals(9, tree.root().leftChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().leftChild().leftChild().color());
            assertEquals(4, tree.root().leftChild().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().leftChild().rightChild().color());
            assertEquals(6, tree.root().leftChild().leftChild().rightChild().value());
            assertEquals(Color.RED, tree.root().leftChild().leftChild().leftChild().leftChild().color());
            assertEquals(3, tree.root().leftChild().leftChild().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().leftChild().color());
            assertEquals(8, tree.root().leftChild().rightChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().rightChild().color());
            assertEquals(10, tree.root().leftChild().rightChild().rightChild().value());

        } catch (Error e) {
            throw e;
        }
    }

    @Test
    public void testInsertMostlyRight() {
        RedBlackTree tree = new RedBlackTree<>(nodeFactory);
        try {
            tree.insert(1);
            tree.insert(2);
            tree.insert(3);
            tree.insert(4);
            tree.insert(5);
            tree.insert(6);
            tree.insert(7);
            tree.insert(8);
            tree.insert(9);
            tree.insert(10);

            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(4, tree.root().value());
            assertEquals(Color.BLACK, tree.root().leftChild().color());
            assertEquals(2, tree.root().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().color());
            assertEquals(3, tree.root().leftChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().color());
            assertEquals(6, tree.root().rightChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().leftChild().color());
            assertEquals(5, tree.root().rightChild().leftChild().value());
            assertEquals(Color.RED, tree.root().rightChild().rightChild().color());
            assertEquals(8, tree.root().rightChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().rightChild().leftChild().color());
            assertEquals(7, tree.root().rightChild().rightChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().rightChild().rightChild().color());
            assertEquals(9, tree.root().rightChild().rightChild().rightChild().value());
            assertEquals(Color.RED, tree.root().rightChild().rightChild().rightChild().rightChild().color());
            assertEquals(10, tree.root().rightChild().rightChild().rightChild().rightChild().value());
        } catch (Error e) {
            throw e;
        }
    }

    @Test
    public void testChaoticInsert() {
        RedBlackTree tree = new RedBlackTree<>(nodeFactory);
        try {
            tree.insert(1);
            assertEquals(
                    TreeBuilder.empty(nodeFactory).rootNode(1, (treeLevel -> {})).build(),
                    tree
                    );
            tree.insert(10);
            tree.insert(2);
            assertEquals(
                    TreeBuilder.empty(nodeFactory).rootNode(2, treeLevel -> {
                        treeLevel.left(1, Color.RED, treeLevel1 -> {});
                        treeLevel.right(10, Color.RED, treeLevel1 -> {});
                    }).build(),
                    tree
            );
            tree.insert(9);
            tree.insert(3);
            assertEquals(
                    TreeBuilder.empty(nodeFactory).rootNode(2, treeLevel -> {
                        treeLevel.left(1, Color.BLACK, treeLevel1 -> {});
                        treeLevel.right(9, Color.BLACK, treeLevel1 -> {
                            treeLevel1.left(3, Color.RED, levelBuilder -> {});
                            treeLevel1.right(10, Color.RED, levelBuilder -> {});
                        });
                    }).build(),
                    tree
            );


            tree.insert(8);
            tree.insert(4);
            tree.insert(7);

            assertEquals(
                    TreeBuilder.empty(nodeFactory).rootNode(4, treeLevel -> {
                        treeLevel.left(2, Color.RED, treeLevel1 -> {
                            treeLevel1.left(1, Color.BLACK, levelBuilder -> {});
                            treeLevel1.right(3, Color.BLACK, levelBuilder -> {});
                        });
                        treeLevel.right(9, Color.RED, treeLevel1 -> {
                            treeLevel1.left(8, Color.BLACK, levelBuilder -> {
                                levelBuilder.left(7, Color.RED, levelBuilder1 -> {});
                            });
                            treeLevel1.right(10, Color.BLACK, levelBuilder -> {});
                        });
                    }).build(),
                    tree
            );

            tree.insert(5);
            tree.insert(6);


        } catch (Error|Exception e) {
            RedBlackTreePrinter.printNode(tree.root());
            throw e;
        }
    }

    @Test
    public void testDelete() {
        RedBlackTree tree = new RedBlackTree<>(nodeFactory);
        try {
            IntStream.range(1, 9).forEach(tree::insert);
            tree.delete(tree.findNode(8));
            assertEquals(Color.BLACK, tree.root().color());
            assertEquals(4, tree.root().value());
            assertEquals(Color.RED, tree.root().leftChild().color());
            assertEquals(2, tree.root().leftChild().value());
            assertEquals(Color.RED, tree.root().rightChild().color());
            assertEquals(6, tree.root().rightChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().leftChild().color());
            assertEquals(5, tree.root().rightChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().rightChild().color());
            assertEquals(7, tree.root().rightChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().color());
            assertEquals(1, tree.root().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().color());
            assertEquals(3, tree.root().leftChild().rightChild().value());

            tree.delete(tree.findNode(7));

            assertEquals(Color.BLACK, tree.root().rightChild().color());
            assertEquals(6, tree.root().rightChild().value());
            assertEquals(Color.RED, tree.root().rightChild().leftChild().color());
            assertEquals(5, tree.root().rightChild().leftChild().value());

            tree.delete(tree.findNode(2));

            assertEquals(Color.BLACK, tree.root().leftChild().color());
            assertEquals(1, tree.root().leftChild().value());
            assertEquals(Color.RED, tree.root().leftChild().rightChild().color());
            assertEquals(3, tree.root().leftChild().rightChild().value());
        } catch (Error|Exception e) {
            RedBlackTreePrinter.printNode(tree.root());
            throw e;
        }
    }

    @Test
    public void testEquals() {
        NodeFactory<Node, Integer> nodeFactory = new NodeFactory<Node, Integer>() {
            @Override
            public Node create(Integer value, Color color) {
                return new Node(value, Color.RED);
            }
        };
        RedBlackTree tree1 = new RedBlackTree<>(nodeFactory);
        RedBlackTree tree2 = new RedBlackTree<>(nodeFactory);

        tree1.insert(1);
        tree1.insert(2);
        tree1.insert(3);
        tree2.insert(1);
        tree2.insert(2);
        tree2.insert(3);

        assertEquals(tree1, tree2);
    }

    @Test
    public void testDeleteChaotic() {
        RedBlackTree tree = new RedBlackTree<>(nodeFactory);
        try {
            tree.insert(1);
            tree.insert(10);
            tree.insert(2);
            tree.insert(9);
            tree.insert(3);
            tree.insert(8);
            tree.insert(4);
            tree.insert(7);
            tree.insert(5);
            tree.insert(6);

            tree.delete(tree.findNode(9));

            assertEquals(Color.BLACK, tree.root().rightChild().color());
            assertEquals(8, tree.root().rightChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().color());
            assertEquals(2, tree.root().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().leftChild().color());
            assertEquals(1, tree.root().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().leftChild().rightChild().color());
            assertEquals(3, tree.root().leftChild().rightChild().value());
            assertEquals(Color.RED, tree.root().rightChild().leftChild().color());
            assertEquals(6, tree.root().rightChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().rightChild().color());
            assertEquals(10, tree.root().rightChild().rightChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().leftChild().leftChild().color());
            assertEquals(5, tree.root().rightChild().leftChild().leftChild().value());
            assertEquals(Color.BLACK, tree.root().rightChild().leftChild().rightChild().color());
            assertEquals(7, tree.root().rightChild().leftChild().rightChild().value());

            tree.delete(tree.findNode(2));

            RedBlackTree sample = TreeBuilder.empty(nodeFactory).rootNode(6, levelBuilder -> {
                levelBuilder.left(4, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(1, Color.BLACK, levelBuilder2 -> {
                        levelBuilder2.right(3, Color.RED);
                    });
                    levelBuilder1.right(5, Color.BLACK);
                });
                levelBuilder.right(8, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(7, Color.BLACK);
                    levelBuilder1.right(10, Color.BLACK);
                });
            }).build();
            assertEquals(sample, tree);

            tree.delete(tree.findNode(5));

            sample = TreeBuilder.empty(nodeFactory).rootNode(6, levelBuilder -> {
                levelBuilder.left(3, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(1, Color.BLACK);
                    levelBuilder1.right(4, Color.BLACK);
                });
                levelBuilder.right(8, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(7, Color.BLACK);
                    levelBuilder1.right(10, Color.BLACK);
                });
            }).build();
            assertEquals(sample, tree);

            tree.delete(tree.findNode(6));

            sample = TreeBuilder.empty(nodeFactory).rootNode(4, levelBuilder -> {
                levelBuilder.left(3, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(1, Color.RED);
                });
                levelBuilder.right(8, Color.RED, levelBuilder1 -> {
                    levelBuilder1.left(7, Color.BLACK);
                    levelBuilder1.right(10, Color.BLACK);
                });
            }).build();
            assertEquals(sample, tree);

            tree.delete(tree.findNode(10));

            sample = TreeBuilder.empty(nodeFactory).rootNode(4, levelBuilder -> {
                levelBuilder.left(3, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(1, Color.RED);
                });
                levelBuilder.right(8, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(7, Color.RED);
                });
            }).build();
            assertEquals(sample, tree);

            tree.delete(tree.findNode(4));

            sample = TreeBuilder.empty(nodeFactory).rootNode(3, levelBuilder -> {
                levelBuilder.left(1, Color.BLACK);
                levelBuilder.right(8, Color.BLACK, levelBuilder1 -> {
                    levelBuilder1.left(7, Color.RED);
                });
            }).build();
            assertEquals(sample, tree);


            tree.delete(tree.findNode(8));
            tree.delete(tree.findNode(3));
            tree.delete(tree.findNode(1));

            sample = TreeBuilder.empty(nodeFactory).rootNode(7).build();
            assertEquals(sample, tree);

            tree.delete(tree.findNode(7));
            sample = TreeBuilder.empty(nodeFactory).build();
            assertEquals(sample, tree);
        } catch (Error|Exception e) {
            RedBlackTreePrinter.printNode(tree.root());
            throw e;
        }
    }
}