package com.ifedorov.cfbf.alloc;

import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DIFAT {

    private Sectors sectors;
    private Header header;
    private FATtoDIFATFacade faTtoDIFATFacade;
    private List<Sector> difatSectors = Lists.newArrayList();

    public DIFAT(Sectors sectors, Header header, FATtoDIFATFacade faTtoDIFATFacade) {
        this.sectors = sectors;
        this.header = header;
        this.faTtoDIFATFacade = faTtoDIFATFacade;
        readDifatSectors();
    }

    private void readDifatSectors() {
        int firstDifatSectorLocation = header.getFirstDifatSectorLocation();
        if(!Utils.isEndOfChain(firstDifatSectorLocation)) {
            Sector lastSector = sectors.sector(firstDifatSectorLocation);
            difatSectors.add(lastSector);
            int nextSectorPosition = -1;
            while(!Utils.isEndOfChain(nextSectorPosition = Utils.toInt(lastSector.subView(header.getSectorShift() - 4, header.getSectorShift()).getData()))) {
                difatSectors.add(lastSector = sectors.sector(nextSectorPosition));
            }
        }
    }

    public List<Integer> getFatSectorChain() {
        List<Integer> result = Lists.newArrayList();
        result.addAll(header.getDifatEntries());
        result.addAll(difatSectors.stream().<Integer>flatMapToInt(sector -> {
            Stream<Integer> stream = IntStream
                    .range(0, sector.getSize() - 4)
                    .filter((val) -> val % 4 == 0)
                    .map((val) -> {
                        return Utils.toInt(sector.subView(val, val + 4).getData());
                    }).mapToObj(Integer::new);
            return StreamUtils.takeWhile(stream, (val) -> !Utils.isEndOfChain(val)).mapToInt(Integer::intValue);
        }).boxed().collect(Collectors.toList()));
        return result;
    }
}
