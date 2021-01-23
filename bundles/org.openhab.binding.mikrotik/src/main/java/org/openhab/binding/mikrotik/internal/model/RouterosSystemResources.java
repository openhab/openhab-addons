package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosSystemResources {
    protected Map<String, String> propMap;

    public RouterosSystemResources(Map<String, String> props) {
        this.propMap = props;
    }

    public String getUptime() {
        return propMap.get("uptime");
    }

    public int getFreeSpace() {
        return Integer.parseInt(propMap.get("free-hdd-space"));
    }

    public int getTotalSpace() {
        return Integer.parseInt(propMap.get("total-hdd-space"));
    }

    public int getSpaceUse() {
        return 15;
    }

    public int getFreeMem() {
        return Integer.parseInt(propMap.get("free-memory"));
    }

    public int getTotalMem() {
        return Integer.parseInt(propMap.get("total-memory"));
    }

    public int getMemUse() {
        return 18;
    }

    public String getCpuLoad() {
        return propMap.get("cpu-load");
    }
}
