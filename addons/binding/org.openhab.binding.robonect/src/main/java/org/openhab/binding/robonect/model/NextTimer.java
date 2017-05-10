package org.openhab.binding.robonect.model;

public class NextTimer {
    private String date;
    private String time;
    private String unix;

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getUnix() {
        return unix;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setUnix(String unix) {
        this.unix = unix;
    }
}
