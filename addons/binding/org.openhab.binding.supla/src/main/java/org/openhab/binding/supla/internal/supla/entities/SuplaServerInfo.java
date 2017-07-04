package org.openhab.binding.supla.internal.supla.entities;

public final class SuplaServerInfo {
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SuplaServerInfo)) return false;

        SuplaServerInfo that = (SuplaServerInfo) o;

        return address != null ? address.equals(that.address) : that.address == null;
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "SuplaServerInfo{" +
                "address='" + address + '\'' +
                '}';
    }
}
