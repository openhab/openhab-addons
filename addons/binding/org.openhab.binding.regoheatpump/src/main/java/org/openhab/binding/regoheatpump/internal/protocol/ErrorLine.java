package org.openhab.binding.regoheatpump.internal.protocol;

import java.util.Calendar;

public class ErrorLine {
    private final byte error;
    private final String timestamp;

    public ErrorLine(byte error, String timestamp) {
        this.error = error;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%d @ %s", error, timestamp);
    }

    public int error() {
        return error;
    }

    public String timestampAsString() {
        return timestamp;
    }

    public Calendar timestamp() {
        Calendar cal = Calendar.getInstance();

        int year = Integer.parseInt(timestamp.substring(0, 2)) + 1000;
        if (year < 1950) {
            year += 1000;
        }
        int month = Integer.parseInt(timestamp.substring(2, 4));
        int day = Integer.parseInt(timestamp.substring(4, 6));
        int hour = Integer.parseInt(timestamp.substring(7, 9));
        int min = Integer.parseInt(timestamp.substring(10, 12));
        int sec = Integer.parseInt(timestamp.substring(13, 15));
        cal.set(year, month - 1, day, hour, min, sec);

        return cal;
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
