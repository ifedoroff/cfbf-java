package com.ifedorov.cfbf.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConditionalStreamReaderTest {

    @Mock RegularStreamReader regularStreamReader;
    @Mock MiniStreamReader miniStreamReader;
    int threshold = 4096;

    @BeforeEach
    void init() {
        when(regularStreamReader.read(anyInt(), anyInt())).thenReturn(new byte[0]);
        when(miniStreamReader.read(anyInt(), anyInt())).thenReturn(new byte[0]);
    }

    @Test
    void testConditionalGetData() {
        ConditionalStreamReader conditionalStreamReader = new ConditionalStreamReader(regularStreamReader, miniStreamReader, 4096);
        conditionalStreamReader.read(0, 4096);
        verify(regularStreamReader, times(1)).read(0, 4096);
        conditionalStreamReader.read(1, 4095);
        verify(miniStreamReader, times(1)).read(1, 4095);
    }
}