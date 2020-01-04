package com.ifedorov.cfbf.alloc;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.ifedorov.cfbf.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DIFAT {

    private Sectors sectors;
    private Header header;
    private FATtoDIFATFacade faTtoDIFATFacade;
    private LinkedList<DIFATSector> difatSectors = Lists.newLinkedList();

    public DIFAT(Sectors sectors, Header header, FATtoDIFATFacade faTtoDIFATFacade) {
        this.sectors = sectors;
        this.header = header;
        this.faTtoDIFATFacade = faTtoDIFATFacade;
        readDifatSectors();
    }

    private void readDifatSectors() {
        int firstDifatSectorLocation = header.getFirstDifatSectorLocation();
        if(!Utils.isEndOfChain(firstDifatSectorLocation)) {
            DIFATSector lastSector = new DIFATSector(sectors.sector(firstDifatSectorLocation));
            difatSectors.add(lastSector);
            int nextSectorPosition = -1;
            while(!Utils.isEndOfChain(nextSectorPosition = Utils.toInt(lastSector.subView(header.getSectorShift() - 4, header.getSectorShift()).getData()))) {
                difatSectors.add(lastSector = new DIFATSector(sectors.sector(nextSectorPosition)));
            }
        }
    }

    public List<Integer> getFatSectorChain() {
        List<Integer> result = Lists.newArrayList();
        result.addAll(header.getDifatEntries());
        result.addAll(difatSectors.stream().<Integer>flatMap(sector -> sector.getRegisteredFatSectors().stream()).mapToInt(Integer::intValue).boxed().collect(Collectors.toList()));
        return result;
    }

    public void registerFATSector(int sectorPosition) {
        if(!header.canFitMoreDifatEntries()) {
            DIFATSector difatSector;
            if(difatSectors.isEmpty()) {
                difatSector = sectors.allocateDIFAT();
                faTtoDIFATFacade.registerDifatSectorInFAT(difatSector.getPosition());
                header.registerFatSector(difatSector.getPosition());
                header.setFirstDifatSectorLocation(difatSector.getPosition());
                difatSectors.add(difatSector);
                header.setNumberOfDifatSectors(difatSectors.size());
            } else if(!difatSectors.getLast().hasFreeSpace()) {
                difatSector = sectors.allocateDIFAT();
                faTtoDIFATFacade.registerDifatSectorInFAT(difatSector.getPosition());
                difatSectors.getLast().registerNextDifatSector(difatSector.getPosition());
                difatSectors.add(difatSector);
                header.setNumberOfDifatSectors(difatSectors.size());
            } else {
                difatSector = difatSectors.getLast();
            }
            difatSector.registerFatSector(sectorPosition);
        } else {
            header.registerFatSector(sectorPosition);
        }
    }
}
