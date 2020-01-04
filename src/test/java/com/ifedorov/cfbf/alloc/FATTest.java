package com.ifedorov.cfbf.alloc;

import com.ifedorov.cfbf.DataView;
import com.ifedorov.cfbf.Header;
import com.ifedorov.cfbf.Sectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}
