package com.ifedorov.cfbf.tree;

public interface NodeFactory<N extends Node, T extends Comparable<T>> {
    N create(T value, Node.Color color);
}
