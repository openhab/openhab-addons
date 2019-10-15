package org.openhab.binding.teleinfo.internal.reader.common;

public interface FrameHcOption {

    public enum Hhphc {
        A,
        C,
        D,
        E,
        Y
    }

    int getHchc();

    void setHchc(int hchc);

    int getHchp();

    void setHchp(int hchp);

    Hhphc getHhphc();

    void setHhphc(Hhphc hhphc);

}
