package com.ifedorov.cfbf.stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConditionalStreamRWTest {

    @Mock
    RegularStreamRW regularStreamRW;
    @Mock
    MiniStreamRW miniStreamRW;
    int threshold = 4096;

    @BeforeEach
    void init() {
        when(regularStreamRW.read(anyInt(), anyInt())).thenReturn(new byte[0]);
        when(miniStreamRW.read(anyInt(), anyInt())).thenReturn(new byte[0]);
    }

    @Test
    void testConditionalGetData() {
        ConditionalStreamRW conditionalStreamRW = new ConditionalStreamRW(regularStreamRW, miniStreamRW, 4096);
        conditionalStreamRW.read(0, 4096);
        verify(regularStreamRW, times(1)).read(0, 4096);
        conditionalStreamRW.read(1, 4095);
        verify(miniStreamRW, times(1)).read(1, 4095);
    }
}