package com.ifedorov.cfbf.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedBlackTreePrinter {

    public static <T extends Comparable<?>> void printNode(Node root) {
        int maxLevel = RedBlackTreePrinter.maxLevel(root);

        printNodeInternal(Collections.singletonList(root), 1, maxLevel);
    }

    private static <T extends Comparable<?>> void printNodeInternal(List<Node> nodes, int level, int maxLevel) {
        if (nodes.isEmpty() || RedBlackTreePrinter.isAllElementsNull(nodes))
            return;

        int floor = maxLevel - level;
        int endgeLines = (int) Math.pow(2, (Math.max(floor - 1, 0)));
        int firstSpaces = (int) Math.pow(2, (floor)) - 1;
        int betweenSpaces = (int) Math.pow(2, (floor + 1)) - 1;

        RedBlackTreePrinter.printWhitespaces(firstSpaces);

        List<Node> newNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node != null) {
                System.out.print(node.value() + "(" + node.color().toString().substring(0,1) + ")");
                newNodes.add(node.leftChild());
                newNodes.add(node.rightChild());
            } else {
//                    newNodes.add(null);
//                    newNodes.add(null);
                System.out.print("N(B)");
            }

            RedBlackTreePrinter.printWhitespaces(betweenSpaces);
        }
        System.out.println("");

        for (int i = 1; i <= endgeLines; i++) {
            for (int j = 0; j < nodes.size(); j++) {
                RedBlackTreePrinter.printWhitespaces(firstSpaces - i);
                if (nodes.get(j) == null) {
                    RedBlackTreePrinter.printWhitespaces(endgeLines + endgeLines + i + 1);
                    continue;
                }

                if (nodes.get(j).leftChild() != null)
                    System.out.print("/");
                else
                    RedBlackTreePrinter.printWhitespaces(1);

                RedBlackTreePrinter.printWhitespaces(i + i - 1);

                if (nodes.get(j).rightChild() != null)
                    System.out.print("\\");
                else
                    RedBlackTreePrinter.printWhitespaces(1);

                RedBlackTreePrinter.printWhitespaces(endgeLines + endgeLines - i);
            }

            System.out.println("");
        }

        printNodeInternal(newNodes, level + 1, maxLevel);
    }

    private static void printWhitespaces(int count) {
        for (int i = 0; i < count; i++)
            System.out.print(" ");
    }

    private static <T extends Comparable<?>> int maxLevel(Node node) {
        if (node == null)
            return 0;

        return Math.max(RedBlackTreePrinter.maxLevel(node.leftChild()), RedBlackTreePrinter.maxLevel(node.rightChild())) + 1;
    }

    private static <T> boolean isAllElementsNull(List<T> list) {
        for (Object object : list) {
            if (object != null)
                return false;
        }

        return true;
    }

}
