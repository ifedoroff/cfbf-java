package com.ifedorov.cfbf;

import com.google.common.collect.Streams;
import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.ArrayUtils;
import sun.misc.IOUtils;

import javax.xml.crypto.Data;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface DataView {
    DataView writeAt(int position, byte[] bytes);
    int getSize();
    byte[] getData();
    DataView subView(int start, int end);
    DataView subView(int start);
    DataView allocate(int length);
    static DataView empty() {
        return new SimpleDataView();
    }

    static DataView from(InputStream is) {
        try {
            return DataView.from(ByteStreams.toByteArray(is));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read from InputStream", e);
        }
    }

    static DataView from(byte[] data) {
        SimpleDataView dataView = new SimpleDataView();
        dataView.data = data;
        return dataView;
    }
    class SimpleDataView implements DataView {

        private byte[] data;

        private SimpleDataView() {

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
            return new SubView(start, end);
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
            return new SubView(start, data.length);
        }

        public DataView allocate(int length) {
            if(data == null) {
                data = new byte[length];
            } else {
                byte[] newData = new byte[data.length + length];
                System.arraycopy(data, 0, newData, 0, data.length);
                this.data = newData;
            }
            return new SubView(data.length, data.length + length);
        }

        private class SubView implements DataView {

            private final int capacity;
            private final int start;
            private final int end;

            public SubView(int start, int end) {
                this.capacity = end - start;
                this.start = start;
                this.end = end;
            }

            @Override
            public DataView writeAt(int position, byte[] bytes) {
                return SimpleDataView.this.writeAt(this.start + position, bytes);
            }

            @Override
            public int getSize() {
                return capacity;
            }

            @Override
            public byte[] getData() {
                return ArrayUtils.subarray(data, start, end);
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
                return new SubView(this.start + start, this.start + end);
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
                return new SubView(this.start + start, end);
            }

            @Override
            public DataView allocate(int length) {
                throw new UnsupportedOperationException();
            }
        }
    }
}
