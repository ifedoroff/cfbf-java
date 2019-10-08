package com.ifedorov.cfbf;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.alloc.AllocationTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationTableTest {

    @Mock Sectors sectors;

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
        when(sectors.sector(0)).thenReturn(Sector.from(DataView.from(firstSector), 0));
        when(sectors.sector(1)).thenReturn(Sector.from(DataView.from(secondSector), 1));
        AllocationTable allocationTable = new AllocationTable(
                sectors,
                Lists.newArrayList(0, 1),
                16);
        assertEquals(4, allocationTable.buildChain(0).size());
        assertTrue(Iterables.elementsEqual(Lists.newArrayList(0,1,2,5), allocationTable.buildChain(0)));
        assertEquals(3, allocationTable.buildChain(3).size());
        assertTrue(Iterables.elementsEqual(Lists.newArrayList(3,4,6), allocationTable.buildChain(3)));
    }
}