package com.ifedorov.cfbf.stream;

public interface StreamWriter {
    int write(byte[] data);
    void writeAt(int startingSector, int position, byte[] data);
    int append(int startingSector, int currentSize, byte[] data);
}
