package com.ifedorov.cfbf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.ifedorov.cfbf.Header.HEADER_LENGTH;
import static com.ifedorov.cfbf.HeaderTest.dummyHeader;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SectorsTest.SectorsDataResolver.class)
@ExtendWith(MockitoExtension.class)
public class SectorsTest {

    @Mock Header header;

    @BeforeEach
    void init() {
        when(header.getSectorShift()).thenReturn(HEADER_LENGTH);
    }

    @Test
    void testNumberOfSectors(@SectorsDataResolver.ByNumberOfSectors(numberOfSectors = 5) byte[] data) {
        Sectors sectors = new Sectors(DataView.from(data), header);
        assertNotNull(sectors.sector(0));
        assertNotNull(sectors.sector(1));
        assertNotNull(sectors.sector(2));
        assertNotNull(sectors.sector(3));
        assertThrows(IndexOutOfBoundsException.class, () -> sectors.sector(4));
    }

    @Test
    void testAllocateFirstSector() {
        DataView backedDataView = DataView.empty();
        Sectors sectors = new Sectors(backedDataView, header);
        Sector allocated = sectors.allocate();
        assertNotNull(allocated);
        assertArrayEquals(Utils.initializedWith(HEADER_LENGTH, Utils.FREESECT_MARK_OR_NOSTREAM), allocated.getData());
        assertEquals(HEADER_LENGTH, backedDataView.getSize());
    }

    @Test
    void testAllocateFAT() {
        DataView backedDataView = DataView.from(Utils.initializedWith(HEADER_LENGTH, 5));
        Sectors sectors = new Sectors(backedDataView, header);
        Sector allocated = sectors.allocate();
        assertNotNull(allocated);
        assertArrayEquals(Utils.initializedWith(HEADER_LENGTH, Utils.FREESECT_MARK_OR_NOSTREAM), allocated.getData());
        assertEquals(1024, backedDataView.getSize());
    }

    @Test
    void testAllocateDIFAT() {
        DataView backedDataView = DataView.from(Utils.initializedWith(HEADER_LENGTH, 5));
        Sectors sectors = new Sectors(backedDataView, header);
        Sector allocated = sectors.allocateDIFAT();
        assertNotNull(allocated);
        byte[] sample = Utils.initializedWith(HEADER_LENGTH, Utils.FREESECT_MARK_OR_NOSTREAM);
        System.arraycopy(Utils.ENDOFCHAIN_MARK, 0, sample, 508, 4);
        assertArrayEquals(sample, allocated.getData());
        assertEquals(1024, backedDataView.getSize());
    }

    public static class SectorsDataResolver implements ParameterResolver {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getAnnotationsByType(ByNumberOfSectors.class).length > 0;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            ByNumberOfSectors[] byNumberOfSectors = parameterContext.getParameter().getAnnotationsByType(ByNumberOfSectors.class);
            return createData(byNumberOfSectors[0].numberOfSectors(), byNumberOfSectors[0].sectorSize());
        }

        private byte[] createData(int sectors, int sectorSize) {
            byte[] data = new byte[sectors * sectorSize];
            System.arraycopy(dummyHeader(), 0, data, 0, HEADER_LENGTH);
            return data;
        }

        @Target(ElementType.PARAMETER)
        @Retention(RetentionPolicy.RUNTIME)
        public @interface ByNumberOfSectors {
            int numberOfSectors() default 1;
            int sectorSize() default HEADER_LENGTH;
        }
    }
}