package org.openhab.binding.mikrotik.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.joda.time.DateTime;

import java.util.Map;

@NonNullByDefault
public class RouterosWirelessRegistration extends RouterosRegistrationBase {
    public RouterosWirelessRegistration(Map<String, String> props) {
        super(props);
    }

    public String getUptime(){ return propMap.get("uptime"); }


}
