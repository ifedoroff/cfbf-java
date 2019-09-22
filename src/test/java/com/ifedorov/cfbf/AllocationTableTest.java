package com.ifedorov.cfbf;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AllocationTableTest {

    @Test
    void testBuildChain() {
        byte[] firstSector = new byte[16];
        System.arraycopy(Utils.toBytes(1, 4), 0, firstSector, 0, 4);
        System.arraycopy(Utils.toBytes(2, 4), 0, firstSector, 4, 4);
        System.arraycopy(Utils.toBytes(5, 4), 0, firstSector, 8, 4);
        System.arraycopy(Utils.toBytes(4, 4), 0, firstSector, 12, 4);
        byte[] secondSector = new byte[16];
        System.arraycopy(Utils.toBytes(6, 4), 0, secondSector, 0, 4);
        System.arraycopy(Utils.ENDOFCHAIN_MARK, 0, secondSector, 4, 4);
        System.arraycopy(Utils.ENDOFCHAIN_MARK, 0, secondSector, 8, 4);
        System.arraycopy(Utils.ENDOFCHAIN_MARK, 0, secondSector, 12, 4);
        AllocationTable allocationTable = new AllocationTable(
                Lists.newArrayList(Sector.from(DataView.from(firstSector), 0), Sector.from(DataView.from(secondSector), 1)),
                16);
        assertEquals(4, allocationTable.buildChain(0).size());
        assertTrue(Iterables.elementsEqual(Lists.newArrayList(0,1,2,5), allocationTable.buildChain(0)));
        assertEquals(3, allocationTable.buildChain(3).size());
        assertTrue(Iterables.elementsEqual(Lists.newArrayList(3,4,6), allocationTable.buildChain(3)));
    }
}