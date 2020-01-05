package com.ifedorov.cfbf.alloc;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FATTest {

    @Mock FATtoDIFATFacade faTtoDIFATFacade;

    @Test
    void shouldSetNumberOfFATSectorsOnHeader() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(rootView, header);
        FAT fat = new FAT(sectors, header, faTtoDIFATFacade);
        fat.registerSector(0, null);
        fat.registerSector(1, 0);
        assertEquals(1, header.getNumberOfFatSectors());
        IntStream.range(0, 128).forEach(i -> fat.registerSector(i + 2, i + 1));
        assertEquals(2, header.getNumberOfFatSectors());
    }

    @Test
    void testRegisterSectorsBeyondOneFatSector() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(DataView.empty(), header);
        List<Integer> sectorPositions = IntStream.range(0, 129).map(val -> sectors.allocate().getPosition()).boxed().collect(Collectors.toList());
        FAT allocationTable = new FAT(sectors, header, faTtoDIFATFacade);
        allocationTable.registerSector(sectorPositions.get(0), null);
        for (int i = 1; i <sectorPositions.size(); i++) {
            allocationTable.registerSector(sectorPositions.get(i), sectorPositions.get(i - 1));
        }
//        verify(fatToDIFATFacade, times(1)).registerFatSectorInDIFAT(129);
//        verify(fatToDIFATFacade, times(1)).registerFatSectorInDIFAT(130);
        Sector fatSector = sectors.sector(129);
        for (int i = 0; i < sectorPositions.size() - 1; i++) {
            assertEquals(sectorPositions.get(i + 1), Utils.toInt(fatSector.subView(i * 4, (i + 1) * 4).getData()));
        }
        assertEquals(128, Utils.toInt(fatSector.subView(508).getData()));
        Sector secondFatSector = sectors.sector(130);
        assertArrayEquals(Utils.ENDOFCHAIN_MARK, secondFatSector.subView(0, 4).getData());
        assertArrayEquals(Utils.FATSECT_MARK, secondFatSector.subView(4, 8).getData());
        assertArrayEquals(Utils.FATSECT_MARK, secondFatSector.subView(8, 12).getData());
    }

    @Test
    void testRegisterSectorsInOneFatSector() {
        DataView rootView = DataView.empty();
        Header header = Header.empty(rootView.allocate(Header.HEADER_LENGTH));
        Sectors sectors = new Sectors(DataView.empty(), header);
        List<Integer> sectorPositions = IntStream.range(0, 128).map(val -> sectors.allocate().getPosition()).boxed().collect(Collectors.toList());
        FAT allocationTable = new FAT(sectors, header, faTtoDIFATFacade);
        allocationTable.registerSector(sectorPositions.get(0), null);
        for (int i = 1; i <sectorPositions.size(); i++) {
            allocationTable.registerSector(sectorPositions.get(i), sectorPositions.get(i - 1));
        }
//        verify(fatToDIFATFacade, times(1)).registerFatSectorInDIFAT(128);
//        verify(fatToDIFATFacade, times(1)).registerFatSectorInDIFAT(129);
//        verify(fatToDIFATFacade, times(2)).registerFatSectorInDIFAT(anyInt());
        Sector fatSector = sectors.sector(128);
        for (int i = 0; i < sectorPositions.size() - 2; i++) {
            assertEquals(sectorPositions.get(i + 1), Utils.toInt(fatSector.subView(i * 4, (i + 1) * 4).getData()));
        }
        assertArrayEquals(fatSector.subView(508).getData(), Utils.ENDOFCHAIN_MARK);
        Sector secondFATSector = sectors.sector(129);
        assertArrayEquals(Utils.FATSECT_MARK, secondFATSector.subView(0, 4).getData());
        assertArrayEquals(Utils.FATSECT_MARK, secondFATSector.subView(4, 8).getData());
    }

}
