package org.openhab.binding.mikrotik.internal.model;

import static org.openhab.binding.mikrotik.internal.model.RouterosInstance.PROP_ID_KEY;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosRegistrationBase {
    protected Map<String, String> propMap;

    public RouterosRegistrationBase(Map<String, String> props) {
        this.propMap = props;
    }

    public String getId() {
        return propMap.get(PROP_ID_KEY);
    }

    public String getComment() {
        return propMap.get("comment");
    }

    public String getMacAddress() {
        return propMap.get("mac-address");
    }

    public String getSSID() {
        return propMap.get("ssid");
    }
}
