package com.ifedorov.cfbf;

import com.ifedorov.cfbf.stream.StreamRW;

public class StreamDirectoryEntry extends DirectoryEntry {
    public StreamDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view, StreamRW streamReader) {
        super(id, directoryEntryChain, view, streamReader);
    }

    public StreamDirectoryEntry(int id, String name, ColorFlag colorFlag, DirectoryEntryChain directoryEntryChain, DataView view, StreamRW streamReader) {
        super(id, name, colorFlag, ObjectType.Stream, directoryEntryChain, view, streamReader);
    }
}
