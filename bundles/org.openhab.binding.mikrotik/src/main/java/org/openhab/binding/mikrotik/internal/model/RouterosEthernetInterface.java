package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosEthernetInterface extends RouterosInterfaceBase {
    public RouterosEthernetInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    protected RouterosInterfaceType[] getDesignedTypes() {
        return new RouterosInterfaceType[] { RouterosInterfaceType.ETHERNET, RouterosInterfaceType.BRIDGE };
    }

    public String getDefaultName() {
        return propMap.get("default-name");
    }
}
