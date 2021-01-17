package org.openhab.binding.awattar.internal;

public abstract class aWATTarBestPriceResult {

    private long start;
    private long end;

    public aWATTarBestPriceResult() {
    }

    public long getStart() {
        return start;
    }

    public void updateStart(long start) {
        if (this.start == 0 || this.start > start) {
            this.start = start;
        }
    }

    public long getEnd() {
        return end;
    }

    public void updateEnd(long end) {
        if (this.end == 0 || this.end < end) {
            this.end = end;
        }
    }

    public abstract boolean isActive();

    public abstract String getHours();
}
