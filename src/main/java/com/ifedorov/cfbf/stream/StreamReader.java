package com.ifedorov.cfbf.stream;

public interface StreamReader {

    byte[] read(int startingSector, int length);
}
