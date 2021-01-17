package org.openhab.binding.awattar.internal;

public class aWATTarBestpriceConfiguration {

    public int rangeStart;
    public int rangeDuration;
    public int length;
    public boolean consecutive;

    public String toString() {
        return String.format("{ s: %d, d: %d, l: %d, c: %b )", rangeStart, rangeDuration, length, consecutive);
    }
}
