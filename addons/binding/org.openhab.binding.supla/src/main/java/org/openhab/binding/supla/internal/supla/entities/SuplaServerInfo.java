package org.openhab.binding.supla.internal.supla.entities;

import java.time.LocalDateTime;

public final class SuplaServerInfo {
    private String address;
    private LocalDateTime time;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaServerInfo)) return false;

        SuplaServerInfo that = (SuplaServerInfo) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        return time != null ? time.equals(that.time) : that.time == null;
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SuplaServerInfo{" +
                "address='" + address + '\'' +
                ", time=" + time +
                '}';
    }
}
