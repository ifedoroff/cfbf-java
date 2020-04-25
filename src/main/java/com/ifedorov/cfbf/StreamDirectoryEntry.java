package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.ifedorov.cfbf.stream.StreamHolder;

import java.io.OutputStream;

public class StreamDirectoryEntry extends DirectoryEntry {

    private final StreamHolder streamHolder;

    public StreamDirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view, StreamHolder streamHolder) {
        super(id, directoryEntryChain, view);
        this.streamHolder = streamHolder;
    }

    public byte[] getStreamData() {
        if(hasStreamData() && getStreamSize() > 0) {
            return streamHolder.getStreamData(getStreamStartingSector(), getStreamSize());
        } else {
            return new byte[0];
        }
    }

    public void copyTo(OutputStream os) {
        streamHolder.copyTo(getStreamStartingSector(), getStreamSize(), os);
    }

    public void setStreamData(byte[] data) {
        setStreamStartingSector(streamHolder.setStreamData(data));
        setStreamSize(data.length);
    }

    public byte[] read(int fromIncl, int toExcl) {
        return streamHolder.read(getStreamStartingSector(), getStreamSize(), fromIncl, toExcl);
    }

    public void writeAt(int position, byte[] data) {
        Verify.verify(position >= 0, "Starting position should be greater than 0: start = " + position);
        Verify.verify(position + data.length <= getStreamSize(), String.format("Cannot write beyond the end of the stream: start = %s, end = %s", position, position + data.length));
        streamHolder.writeAt(getStreamStartingSector(), getStreamSize(), position, data);
    }

    public void append(byte[] data) {
        int startingLocation = streamHolder.append(getStreamStartingSector(), getStreamSize(), data);
        setStreamStartingSector(startingLocation);
        setStreamSize(getStreamSize() + data.length);
    }

    private void setStreamSize(int length) {
        view.subView(FLAG_POSITION.STREAM_SIZE, FLAG_POSITION.STREAM_SIZE + 4).writeAt(0, Utils.toBytesLE(length, 4));
    }

    public int getStreamSize() {
        return Utils.toInt(view.subView(FLAG_POSITION.STREAM_SIZE, FLAG_POSITION.STREAM_SIZE + 4).getData());
    }

    public boolean hasStreamData() {
        return getObjectType() == ObjectType.Stream && !Utils.isEndOfChain(getStreamStartingSector());
    }

    public static class Builder extends DirectoryEntry.Builder<StreamDirectoryEntry> {

        private final StreamHolder holder;

        public Builder(int id, DirectoryEntryChain directoryEntryChain, DataView view, StreamHolder holder) {
            super(id, directoryEntryChain, view);
            this.holder = holder;
            super.objectType(ObjectType.Stream);
        }

        @Override
        public DirectoryEntry.Builder<StreamDirectoryEntry> name(String name) {
            return super.name(name);
        }

        @Override
        public DirectoryEntry.Builder<StreamDirectoryEntry> color(ColorFlag colorFlag) {
            return super.color(colorFlag);
        }

        @Override
        public DirectoryEntry.Builder<StreamDirectoryEntry> objectType(ObjectType type) {
            throw new UnsupportedOperationException("already set in constructor");
        }

        @Override
        public DirectoryEntry.Builder<StreamDirectoryEntry> leftSibling(DirectoryEntry entry) {
            return super.leftSibling(entry);
        }

        @Override
        public DirectoryEntry.Builder<StreamDirectoryEntry> rightSibling(DirectoryEntry entry) {
            return super.rightSibling(entry);
        }

        @Override
        public DirectoryEntry.Builder<StreamDirectoryEntry> child(DirectoryEntry entry) {
            if(entry != null) {
                throw new UnsupportedOperationException("Stream directory cannot have a child");
            } else {
                super.child(null);
                return this;
            }
        }

        public StreamDirectoryEntry build() {
            return new StreamDirectoryEntry(id, directoryEntryChain, view, holder);
        }
    }
}
