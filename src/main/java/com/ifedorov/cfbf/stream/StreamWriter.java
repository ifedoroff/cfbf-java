package com.ifedorov.cfbf.stream;

import java.io.OutputStream;

public interface StreamWriter {
    int write(byte[] data);
    void writeAt(int startingSector, int position, byte[] data);
    int append(int startingSector, int currentSize, byte[] data);

    void copyTo(int startingLocation, OutputStream os);
}
