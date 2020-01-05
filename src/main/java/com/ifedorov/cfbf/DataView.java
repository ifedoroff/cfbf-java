package com.ifedorov.cfbf;

import com.google.common.base.Verify;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataView {
    DataView writeAt(int position, byte[] bytes);
    int getSize();
    byte[] getData();
    DataView subView(int start, int end);
    DataView subView(int start);
    DataView allocate(int length);
    DataView fill(byte[] filler);
    default boolean isEmpty() {
        return getSize() == 0;
    };
    byte[] readAt(int position, int length);
//    default byte[] readAt(int position, int length) {
//        return ArrayUtils.subarray(getData(), position, position + length);
//    };

    static DataView empty() {
        return new ChunkedDataView(Header.SECTOR_SHIFT_VERSION_3_INT);
    }

    static DataView from(InputStream is) {
        try {
            return DataView.from(ByteStreams.toByteArray(is));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read from InputStream", e);
        }
    }

    static DataView from(byte[] data) {
        ChunkedDataView dataView = new ChunkedDataView(Header.SECTOR_SHIFT_VERSION_3_INT, data);
        return dataView;
    }

    class ChunkedDataView implements DataView {
        private int chunkSize;
        private List<DataView> chunks = Lists.newArrayList();

        private ChunkedDataView(int chunkSize) {
            this.chunkSize = chunkSize;
        }
        private ChunkedDataView(int chunkSize, byte[] data) {
            this(chunkSize);
            Verify.verify(data.length % chunkSize == 0);
            int dataLength = data.length;
            SimpleDataView rawView = new SimpleDataView(data);
            for (int i = 0; i < dataLength; i+=512) {
                chunks.add(new ReferencingSubView(rawView, i, i + 512));
            }
        }

        private ChunkedDataView(int chunkSize, List<DataView> chunks) {
            this.chunkSize = chunkSize;
            this.chunks.addAll(chunks);
        }

        @Override
        public DataView writeAt(int position, byte[] bytes) {
            return chunks.get(position / 512).writeAt(position%512, bytes);
        }

        @Override
        public int getSize() {
            return chunks.size() * chunkSize;
        }

        @Override
        public byte[] getData() {
            byte[] result = new byte[getSize()];
            for (int i = 0; i < chunks.size(); i++) {
                DataView chunk = chunks.get(i);
                System.arraycopy(chunk.getData(), 0, result, i * chunkSize, chunkSize);
            }
            return result;
        }

        @Override
        public DataView subView(int start, int end) {
            Verify.verify(start/chunkSize == (end - 1)/chunkSize, "Can only get subview enclosed by one chunk. Actual values: " + start + " - " + end);
            Verify.verify(start != end, "Cannot get subview of size 0");
            DataView chunk = chunks.get(start / chunkSize);
            if(end % chunkSize == 0) {
                return chunk.subView(start % chunkSize);
            } else {
                return chunk.subView(start % chunkSize, end % chunkSize);
            }
        }

        @Override
        public DataView subView(int start) {
            throw new UnsupportedOperationException();
//            Verify.verify(start % chunkSize == 0, "Cannot create subview that partitions chunk into two");
//            return new ChunkedDataView(chunkSize, Lists.newArrayList(chunks.subList(start/chunkSize, chunks.size())));
        }

        @Override
        public DataView allocate(int length) {
            Verify.verify(length == chunkSize);
            SimpleDataView view = new SimpleDataView(new byte[length]);
            chunks.add(view);
            return view;
        }

        @Override
        public DataView fill(byte[] filler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] readAt(int position, int length) {
            throw new UnsupportedOperationException();
        }
    }

    class SimpleDataView implements DataView {

        private byte[] data;

        public SimpleDataView(byte[] data) {
            this.data = data;
        }

        public DataView writeAt(int position, byte[] bytes) {
            if(data == null) {
                throw new IndexOutOfBoundsException(String.format("%s + %s > %s", bytes.length, 0, 0));
            }
            if(position + bytes.length > data.length) {
                throw new IndexOutOfBoundsException(String.format("%s + %s > %s", bytes.length, position, data.length));
            }
            System.arraycopy(bytes, 0, data, position, bytes.length);
            return this;
        }

        public int getSize() {
            return data.length;
        }

        public byte[] getData() {
            return data;
        }

        public DataView subView(int start, int end) {
            int dataStart = 0;
            int dataEnd = data == null ? 0 : data.length;
            if(end < start) {
                throw new IndexOutOfBoundsException(String.format("end < start (%s < %s)", end, start));
            }
            if(start < dataStart) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view start: %s", start, dataStart));
            }
            if(end > dataEnd) {
                throw new IndexOutOfBoundsException(String.format("subView end: %s, view end: %s", end, dataEnd));
            }
            if(start > dataEnd) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view end: %s", start, dataEnd));
            }
            if(end < dataStart) {
                throw new IndexOutOfBoundsException(String.format("subView end: %s, view start: %s", end, dataStart));
            }
            return new ReferencingSubView(this, start, end);
        }

        @Override
        public DataView subView(int start) {
            int dataStart = 0;
            int dataEnd = data == null ? 0 : data.length;
            if(start < dataStart) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view start: %s", start, dataStart));
            }
            if(start > dataEnd) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view end: %s", start, dataEnd));
            }
            return new ReferencingSubView(this, start, data.length);
        }

        public DataView allocate(int length) {
            throw new UnsupportedOperationException();
//            if(length != chunkSize) {
//                throw new IllegalArgumentException("Allocation should be performed in chunks of " + chunkSize + ". Requested allocation is " + length);
//            }
//            if(data == null) {
//                data = new byte[length];
//            } else {
//                byte[] newData = new byte[data.length + length];
//                System.arraycopy(data, 0, newData, 0, data.length);
//                this.data = newData;
//            }
//            return new ReferencingSubView(this, data.length - length, data.length);
        }

        @Override
        public DataView fill(byte[] filler) {
            Utils.fill(data, filler);
            return this;
        }

        @Override
        public byte[] readAt(int position, int length) {
            return ArrayUtils.subarray(data, position, position + length);
        }
    }

    static class ReferencingSubView implements DataView {

        private final int capacity;
        private final int start;
        private final int end;
        private DataView delegate;

        public ReferencingSubView(DataView delegate, int start, int end) {
            this.delegate = delegate;
            this.capacity = end - start;
            this.start = start;
            this.end = end;
        }

        @Override
        public DataView writeAt(int position, byte[] bytes) {
            return delegate.writeAt(this.start + position, bytes);
        }

        @Override
        public int getSize() {
            return capacity;
        }

        @Override
        public byte[] getData() {
            return delegate.readAt(start, end - start);
        }

        @Override
        public DataView subView(int start, int end) {
            if(end < start) {
                throw new IndexOutOfBoundsException(String.format("end < start (%s < %s)", end, start));
            }
            if(start < 0) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view start: %s", start, 0));
            }
            if(end > this.capacity) {
                throw new IndexOutOfBoundsException(String.format("subView end: %s, view end: %s", end, this.capacity));
            }
            if(start > this.capacity) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view end: %s", start, this.capacity));
            }
            if(end < 0) {
                throw new IndexOutOfBoundsException(String.format("subView end: %s, view start: %s", end, 0));
            }
            return new ReferencingSubView(delegate, this.start + start, this.start + end);
        }

        @Override
        public DataView subView(int start) {
            int dataStart = 0;
            int dataEnd = capacity;
            if(start < dataStart) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view start: %s", start, dataStart));
            }
            if(start > dataEnd) {
                throw new IndexOutOfBoundsException(String.format("subView start: %s, view end: %s", start, dataEnd));
            }
            return new ReferencingSubView(delegate, this.start + start, end);
        }

        @Override
        public DataView allocate(int length) {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataView fill(byte[] filler) {
            Verify.verify(getSize() % filler.length == 0);
            int step = filler.length;
            for (int i = 0; i < getSize(); i+=step) {
                this.writeAt(i, filler);
            }
            return this;
        }

        @Override
        public byte[] readAt(int position, int length) {
            if(start + position >= end) {
                throw new IllegalArgumentException(String.format("Starting position cannot be greater then subview 'end'. (starting position: %s < view end: %s)", position, end));
            }
            if(start + position + length >= end) {
                throw new IllegalArgumentException(String.format("Operation exceeds view limits. (read end position < view end: %s; )", position + length, end));
            }
            return delegate.readAt(start + position, length);
        }
    }
}
