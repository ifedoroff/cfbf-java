package com.ifedorov.cfbf;

import com.google.common.base.Strings;
import com.google.common.base.Verify;
import com.ifedorov.cfbf.stream.StreamRW;
import com.ifedorov.cfbf.stream.StreamReader;
import com.ifedorov.cfbf.tree.Node;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class DirectoryEntry implements Comparable<DirectoryEntry>{

    public static final int ENTRY_LENGTH = 128;
    public static final int ENTRY_NAME_MAXIMUM_LENGTH_UTF16_STRING = 31;
    public static final int ENTRY_NAME_MAXIMUM_LENGTH = 64;
    public static final byte[] UTF16_TERMINATING_BYTES = new byte[]{0, 0};
    protected DataView view;
    private ObjectType objectType;
    private ColorFlag colorFlag;
    private int id;
    protected DirectoryEntryChain directoryEntryChain;

    public interface FLAG_POSITION {

        int DIRECTORY_ENTRY_NAME = 0;
        int DIRECTORY_ENTRY_NAME_LENGTH = 64;
        int OBJECT_TYPE = 66;
        int COLOR_FLAG = 67;
        int LEFT_SIBLING = 68;
        int RIGHT_SIBLING = 72;
        int CHILD = 76;
        int CLSID = 80;
        int STATE_BITS = 96;
        int CREATION_TIME = 100;
        int MODIFY_TIME = 108;
        int STARTING_SECTOR_LOCATION = 116;
        int STREAM_SIZE = 120;
    }
    protected DirectoryEntry(int id, DirectoryEntryChain directoryEntryChain, DataView view) {
        this.id = id;
        this.directoryEntryChain = directoryEntryChain;
        this.view = view;
        Verify.verify(view.getSize() == ENTRY_LENGTH);
        int nameLength = Utils.toInt(view.subView(FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH + 2).getData());
        Verify.verify(nameLength >= 0 && nameLength <= ENTRY_NAME_MAXIMUM_LENGTH);
        objectType = ObjectType.fromCode(view.subView(FLAG_POSITION.OBJECT_TYPE, FLAG_POSITION.OBJECT_TYPE +1).getData()[0]);
        colorFlag = ColorFlag.fromCode(view.subView(FLAG_POSITION.COLOR_FLAG, FLAG_POSITION.COLOR_FLAG +1).getData()[0]);
    }

    protected DirectoryEntry(int id, String name, ColorFlag colorFlag, ObjectType objectType, DirectoryEntryChain directoryEntryChain, DataView view) {
        this.id = id;
        this.directoryEntryChain = directoryEntryChain;
        this.view = view;
        setObjectType(objectType);
        setColorFlag(colorFlag);
        int nameLength = name.length() * 2 + 2;
        Verify.verify(nameLength >= 0 && nameLength <= ENTRY_NAME_MAXIMUM_LENGTH);
        setDirectoryEntryName(name);
        setLeftSibling(null);
        setRightSibling(null);
        view.subView(FLAG_POSITION.STREAM_SIZE, FLAG_POSITION.STREAM_SIZE + 8).writeAt(0, Utils.toBytes(0, 8));
        setStreamStartingSector(Utils.ENDOFCHAIN_MARK_INT);
    }

    @Override
    public int compareTo(DirectoryEntry o) {
        int result = Integer.compare(this.getDirectoryEntryName().length(), o.getDirectoryEntryName().length());
        if(result == 0) {
            result = this.getDirectoryEntryName().toUpperCase().compareTo(o.getDirectoryEntryName().toUpperCase());
        }
        return result;
    }

    protected void setRightSibling(DirectoryEntry rightSibling) {
        if(rightSibling == null) {
            view.subView(FLAG_POSITION.RIGHT_SIBLING, FLAG_POSITION.RIGHT_SIBLING + 4).writeAt(0, Utils.FREESECT_MARK_OR_NOSTREAM);
        } else {
            view.subView(FLAG_POSITION.RIGHT_SIBLING, FLAG_POSITION.RIGHT_SIBLING + 4).writeAt(0, Utils.toBytes(rightSibling.getId(), 4));
        }
    }

    protected void setLeftSibling(DirectoryEntry leftSibling) {
        if(leftSibling == null) {
            view.subView(FLAG_POSITION.LEFT_SIBLING, FLAG_POSITION.LEFT_SIBLING + 4).writeAt(0, Utils.FREESECT_MARK_OR_NOSTREAM);
        } else {
            view.subView(FLAG_POSITION.LEFT_SIBLING, FLAG_POSITION.LEFT_SIBLING + 4).writeAt(0, Utils.toBytes(leftSibling.getId(), 4));
        }
    }

    public void setDirectoryEntryName(String name) {
        if(Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("Directory Entry name should be non-null and non-empty string");
        }
        if(name.length() > ENTRY_NAME_MAXIMUM_LENGTH_UTF16_STRING) {
            throw new IllegalArgumentException("Directory Entry name may contain 31 UTF-16 at most + NULL terminated character");
        }
        view.subView(FLAG_POSITION.DIRECTORY_ENTRY_NAME, FLAG_POSITION.DIRECTORY_ENTRY_NAME + ENTRY_NAME_MAXIMUM_LENGTH).writeAt(0, Utils.addTrailingZeros(Utils.toUTF16Bytes(name), ENTRY_NAME_MAXIMUM_LENGTH));
        int lengthInBytesIncludingTerminatorSymbol = name.length();
        view.subView(FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH + 2).writeAt(0, Utils.toBytes(lengthInBytesIncludingTerminatorSymbol * 2 + 2, 2));
    }

    public int getId() {
        return id;
    }

    public String getDirectoryEntryName() {
        return Utils.toUTF8WithNoTrailingZeros(view.subView(FLAG_POSITION.DIRECTORY_ENTRY_NAME, FLAG_POSITION.DIRECTORY_ENTRY_NAME + ENTRY_NAME_MAXIMUM_LENGTH).getData());
    }

    public int getDirectoryEntryNameLength() {
        return Utils.toInt(view.subView(DirectoryEntry.FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH, DirectoryEntry.FLAG_POSITION.DIRECTORY_ENTRY_NAME_LENGTH + 2).getData());
    }

    public int getDirectoryEntryNameLengthUTF8() {
        return (getDirectoryEntryNameLength() - 2)/2;
    }

    public Optional<DirectoryEntry> getChild() {
        int childPosition = getChildPosition();
        return Utils.isFreeSectOrNoStream(childPosition) ? Optional.empty() : Optional.ofNullable(directoryEntryChain.getEntryById(childPosition));
    }

    protected int getChildPosition() {
        return Utils.toInt(view.subView(FLAG_POSITION.CHILD, FLAG_POSITION.CHILD + 4).getData());
    }

    private void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
        view.subView(FLAG_POSITION.OBJECT_TYPE, FLAG_POSITION.OBJECT_TYPE +1).writeAt(0, new byte[]{(byte) objectType.code()});
    }

    public Optional<DirectoryEntry> getLeftSibling() {
        int leftSiblingPosition = getLeftSiblingPosition();
        return Utils.isFreeSectOrNoStream(leftSiblingPosition) || Utils.isEndOfChain(leftSiblingPosition) ? Optional.empty() : Optional.of(directoryEntryChain.getEntryById(leftSiblingPosition));
    }

    private int getLeftSiblingPosition() {
        return Utils.toInt(view.subView(FLAG_POSITION.LEFT_SIBLING, FLAG_POSITION.LEFT_SIBLING + 4).getData());
    }

    public Optional<DirectoryEntry> getRightSibling() {
        int rightSiblingPosition = getRightSiblingPosition();
        return Utils.isFreeSectOrNoStream(rightSiblingPosition) ? Optional.empty() : Optional.ofNullable(directoryEntryChain.getEntryById(rightSiblingPosition));
    }

    private int getRightSiblingPosition() {
        return Utils.toInt(view.subView(FLAG_POSITION.RIGHT_SIBLING, FLAG_POSITION.RIGHT_SIBLING + 4).getData());
    }

    public int getStreamStartingSector() {
        return Utils.toInt(view.subView(FLAG_POSITION.STARTING_SECTOR_LOCATION, FLAG_POSITION.STARTING_SECTOR_LOCATION + 4).getData());
    }

    public void setStreamStartingSector(int startingSector) {
        view.subView(FLAG_POSITION.STARTING_SECTOR_LOCATION, FLAG_POSITION.STARTING_SECTOR_LOCATION + 4).writeAt(0, Utils.toBytes(startingSector, 4));
    }

    public void traverse(Consumer<DirectoryEntry> action) {
        action.accept(this);
        getLeftSibling().ifPresent((leftSibling) -> leftSibling.traverse(action));
        getRightSibling().ifPresent((rightSibling) -> rightSibling.traverse(action));
        getChild().ifPresent(child -> child.traverse(action));
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public ColorFlag getColorFlag() {
        return colorFlag;
    }

    public void setColorFlag(ColorFlag colorFlag) {
        this.colorFlag = colorFlag;
        view.subView(FLAG_POSITION.COLOR_FLAG, FLAG_POSITION.COLOR_FLAG +1).writeAt(0, new byte[]{(byte) colorFlag.code()});
    }

    public void invertColor() {
        this.colorFlag = this.colorFlag == ColorFlag.BLACK ? ColorFlag.RED : ColorFlag.BLACK;
    }

    public enum ColorFlag {
        RED(0), BLACK(1);

        private int code;

        private ColorFlag(int code) {

            this.code = code;
        }

        public static ColorFlag fromCode(int code) {
            for (ColorFlag value : ColorFlag.values()) {
                if(value.code == code) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown ColorFlag: " + code);
        }

        public int code() {
            return code;
        }

        public static ColorFlag fromNodeColor(Node.Color color) {
            return color == Node.Color.BLACK ? BLACK : RED;
        }

        public Node.Color toNodeColor() {
            return this == BLACK ? Node.Color.BLACK : Node.Color.RED;
        }

    }

    public enum ObjectType {
        Storage(1), Stream(2), RootStorage(5), Unknown(0);

        private int code;

        private ObjectType(int code) {

            this.code = code;
        }

        public static ObjectType fromCode(int code) {
            for (ObjectType value : ObjectType.values()) {
                if(value.code == code) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown ObjectType: " + code);
        }

        public int code() {
            return code;
        }
    }
}
