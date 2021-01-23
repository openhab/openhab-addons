package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosRouterboardInfo {
    protected Map<String, String> propMap;

    public RouterosRouterboardInfo(Map<String, String> props) {
        this.propMap = props;
    }

    public String getFirmware() {
        return String.format("v%s (%s)", getFirmwareVersion(), getFirmwareType());
    }

    public boolean isRouterboard() {
        return propMap.get("routerboard").equals("true");
    }

    public String getModel() {
        return propMap.get("model");
    }

    public String getSerialNumber() {
        return propMap.get("serial-number");
    }

    public String getFirmwareType() {
        return propMap.get("firmware-type");
    }

    public String getFirmwareVersion() {
        return propMap.get("current-firmware");
    }
}
