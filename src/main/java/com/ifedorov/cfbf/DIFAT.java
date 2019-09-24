package com.ifedorov.cfbf;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DIFAT {

    private CompoundFile compoundFile;
    private Header header;
    private List<Sector> difatSectors = Lists.newArrayList();

    public DIFAT(CompoundFile compoundFile, Header header) {
        this.compoundFile = compoundFile;
        this.header = header;
        readDifatSectors();
    }

    private void readDifatSectors() {
        int firstDifatSectorLocation = header.getFirstDifatSectorLocation();
        if(!Utils.isEndOfChain(firstDifatSectorLocation)) {
            Sector lastSector = compoundFile.sector(firstDifatSectorLocation);
            difatSectors.add(lastSector);
            int nextSectorPosition = -1;
            while(!Utils.isEndOfChain(nextSectorPosition = Utils.toInt(lastSector.subView(header.getSectorShift() - 4, header.getSectorShift()).getData()))) {
                difatSectors.add(lastSector = compoundFile.sector(nextSectorPosition));
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
