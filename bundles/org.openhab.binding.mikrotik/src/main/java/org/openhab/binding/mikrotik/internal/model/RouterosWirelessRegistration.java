package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosWirelessRegistration extends RouterosRegistrationBase {
    public RouterosWirelessRegistration(Map<String, String> props) {
        super(props);
    }

    public String getUptime() {
        return propMap.get("uptime");
    }
}
