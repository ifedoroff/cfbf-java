package com.ifedorov.cfbf.stream;

public interface StreamReader {

    byte[] read(int startingSector, int length);
    byte[] read(int startingSector, int fromIncl, int toExcl);
}
