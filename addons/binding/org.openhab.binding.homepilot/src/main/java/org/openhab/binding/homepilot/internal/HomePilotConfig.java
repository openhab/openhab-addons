package org.openhab.binding.homepilot.internal;

public class HomePilotConfig {

    private String address = null;

    public String getAddress() {
        if (address == null) {
            throw new IllegalStateException("The address for the hompilot bridge is not set correctly. "
                    + "Do you have setup a .things file with e.g.: 'homepilot:bridge:default [ address=\"YOUR_HOMEPILOT_IP\" ]'?");
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
