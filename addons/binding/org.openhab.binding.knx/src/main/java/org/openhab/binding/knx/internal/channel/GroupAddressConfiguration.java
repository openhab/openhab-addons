package org.openhab.binding.knx.internal.channel;

public class GroupAddressConfiguration {

    private final String ga;
    private final boolean read;

    public GroupAddressConfiguration(String ga, boolean read) {
        super();
        this.ga = ga;
        this.read = read;
    }

    public String getGA() {
        return ga;
    }

    public boolean isRead() {
        return read;
    }

}
