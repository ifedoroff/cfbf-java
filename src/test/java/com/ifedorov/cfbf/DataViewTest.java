package com.ifedorov.cfbf;

import com.google.common.base.VerifyException;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DataViewTest {

    @Test
    void testOverflow() {
        assertThrows(IndexOutOfBoundsException.class, () -> DataView.empty().writeAt(0, new byte[1]));
        assertThrows(IndexOutOfBoundsException.class, () -> DataView.empty().allocate(512).writeAt(510, new byte[6]));
    }

    @Test
    void testSize() {
        DataView dataView = DataView.empty();
        assertThrows(VerifyException.class, () -> dataView.allocate(100).getSize());
        assertEquals(512, dataView.allocate(512).getSize());
        assertEquals(512, dataView.allocate(512).getSize());
        assertEquals(1024, dataView.getSize());
    }

    @Test
    void testSubView() {
        DataView dataView = new DataView.SimpleDataView(new byte[100]);
        byte[] firstPart = new byte[50];
        Arrays.fill(firstPart, (byte)1);
        dataView.writeAt(0, firstPart);
        byte[] secondPart = new byte[50];
        Arrays.fill(secondPart, (byte)2);
        dataView.writeAt(50, secondPart);
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(0, 101));
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(0, 50).subView(1, 51));
        assertArrayEquals(firstPart, dataView.subView(0, 50).getData());
        assertArrayEquals(secondPart, dataView.subView(50, 100).getData());
        assertThrows(UnsupportedOperationException.class, () -> dataView.subView(0, 10).allocate(10));
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(101));
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(0, 10).subView(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> dataView.subView(0, 10).subView(11));
        assertEquals(10, dataView.subView(50, 100).subView(0, 10).getSize());
    }

    @Test
    void testVariableSizeChunkedDataView() {
        DataView.VariableSizeChunkedDataView dataView = new DataView.VariableSizeChunkedDataView(
                Lists.newArrayList(
                        new DataView.SimpleDataView(new byte[]{0,1,2}),
                        new DataView.SimpleDataView(new byte[]{3,4,5,6,7}),
                        new DataView.SimpleDataView(new byte[]{8,9})
                )
        );
        assertArrayEquals(new byte[]{0,1,2,3,4,5,6,7,8,9}, dataView.getData());
        assertEquals(10, dataView.getSize());
        assertArrayEquals(new byte[]{0,1,2}, dataView.subView(0, 3).getData());
        assertArrayEquals(new byte[]{3,4,5,6,7,8,9}, dataView.subView(3).getData());
        assertArrayEquals(new byte[]{3,4,5}, dataView.subView(3, 6).getData());
        assertArrayEquals(new byte[]{3,4,5,6,7}, dataView.subView(3, 8).getData());
        assertArrayEquals(new byte[]{3,4,5,6,7, 8}, dataView.subView(3, 9).getData());
        dataView.writeAt(5, new byte[]{15});
        assertArrayEquals(new byte[]{15}, dataView.readAt(5, 1));
        dataView.writeAt(5, new byte[]{13,13,13,13,13});
        assertArrayEquals(new byte[]{13,13,13,13,13}, dataView.readAt(5, 5));
        dataView.fill(new byte[]{30});
        assertArrayEquals(Utils.initializedWith(10, 30), dataView.getData());
    }

}
