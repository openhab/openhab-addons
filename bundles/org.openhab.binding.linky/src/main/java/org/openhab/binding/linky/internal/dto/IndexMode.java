package org.openhab.binding.linky.internal.dto;

public enum IndexMode {
    None(-1, 0),
    Supplier(0, 10),
    Distributor(1, 4);

    private final int idx;
    private final int size;

    IndexMode(int idx, int size) {
        this.idx = idx;
        this.size = size;
    }

    public int getIdx() {
        return idx;
    }

    public int getSize() {
        return size;
    }
}
