package org.openhab.binding.jellyfin.internal.discovery;

class JellyfinServerInfo {
    private final String name;
    private final String id;
    private final String address;
    private final int port;
    private final String discoveredIpAddress; // The IP address from which the response was received

    public JellyfinServerInfo(String name, String id, String address, int port, String discoveredIpAddress) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.port = port;
        this.discoveredIpAddress = discoveredIpAddress;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getDiscoveredIpAddress() {
        return discoveredIpAddress;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", ID: " + id + ", Address: " + address + ", Port: " + port + ", Discovered IP: "
                + discoveredIpAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        JellyfinServerInfo that = (JellyfinServerInfo) o;
        return id.equals(that.id); // Servers are unique by their ID
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
