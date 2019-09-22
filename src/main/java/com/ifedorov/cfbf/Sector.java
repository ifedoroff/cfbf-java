package com.ifedorov.cfbf;

public interface Sector extends DataView{
    int getPosition();
    public static Sector from(DataView view, int position) {
        return new SimpleSector(view, position);
    }
    class SimpleSector implements Sector {
        private final DataView view;
        private final int position;

        private SimpleSector(DataView view, int position) {
            this.view = view;
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public DataView writeAt(int position, byte[] bytes) {
            return view.writeAt(position, bytes);
        }

        @Override
        public int getSize() {
            return view.getSize();
        }

        @Override
        public byte[] getData() {
            return view.getData();
        }

        @Override
        public DataView subView(int start, int end) {
            return view.subView(start, end);
        }

        @Override
        public DataView subView(int start) {
            return view.subView(start);
        }

        @Override
        public DataView allocate(int length) {
            return view.allocate(length);
        }

        public static DataView empty() {
            return DataView.empty();
        }

        public static DataView from(byte[] data) {
            return DataView.from(data);
        }
    }
}
