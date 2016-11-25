package org.openhab.binding.regoheatpump.internal.protocol;

public class ErrorLine {
    private final int error;
    private final String timestamp;
    private final int value1;
    private final int value2;
    private final int value3;
    private final int value4;

    public ErrorLine(int error, String timestamp, int v1, int v2, int v3, int v4) {
        this.error = error;
        this.timestamp = timestamp;
        value1 = v1;
        value2 = v2;
        value3 = v3;
        value4 = v4;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s", errorSource(), timestamp);
    }

    public String timestamp() {
        return timestamp;
    }

    public int value1() {
        return value1;
    }

    public int value2() {
        return value2;
    }

    public int value3() {
        return value3;
    }

    public int value4() {
        return value4;
    }

    public String errorSource() {
        switch (error) {
            case 0:
                return "Sensor radiator return (GT1)";
            case 1:
                return "Outdoor sensor (GT2)";
            case 2:
                return "Sensor hot water (GT3)";
            case 3:
                return "Mixing valve sensor (GT4)";
            case 4:
                return "Room sensor (GT5)";
            case 5:
                return "Sensor compressor (GT6)";
            case 6:
                return "Sensor heat tran fluid out (GT8)";
            case 7:
                return "Sensor heat tran fluid in (GT9)";
            case 8:
                return "Sensor cold tran fluid in (GT10)";
            case 9:
                return "Sensor cold tran fluid in (GT11)";
            case 10:
                return "Compresor circuit switch";
            case 11:
                return "Electrical cassette";
            case 12:
                return "HTF C=pump switch (MB2)";
            case 13:
                return "Low pressure switch (LP)";
            case 14:
                return "High pressure switch (HP)";
            case 15:
                return "High return HP (GT9)";
            case 16:
                return "HTF out max (GT8)";
            case 17:
                return "HTF in under limit (GT10)";
            case 18:
                return "HTF out under limit (GT11)";
            case 19:
                return "Compressor superheat (GT6)";
            case 20:
                return "3-phase incorrect order";
            case 21:
                return "Power failure";
            case 22:
                return "High delta GT8/GT9";
            default:
                return String.format("Unknown (%d)", error);
        }
    }
}
