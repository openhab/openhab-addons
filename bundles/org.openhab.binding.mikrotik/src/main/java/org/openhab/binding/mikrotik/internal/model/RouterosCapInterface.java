package org.openhab.binding.mikrotik.internal.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class RouterosCapInterface extends RouterosInterfaceBase {
    public RouterosCapInterface(Map<String, String> props) {
        super(props);
    }

    @Override
    protected RouterosInterfaceType[] getDesignedTypes() {
        return new RouterosInterfaceType[] { RouterosInterfaceType.CAP };
    }

    public boolean isMaster() {
        return propMap.get("slave").equals("false");
    }

    public boolean isDynamic() {
        return propMap.get("dynamic").equals("true");
    }

    public boolean isBound() {
        return propMap.get("bound").equals("true");
    }

    public boolean isActive() {
        return propMap.get("inactive").equals("false");
    }

    public String getCurrentState() {
        return propMap.get("current-state");
    }

    public int getRegisteredClients() {
        return Integer.parseInt(propMap.get("current-registered-clients"));
    }

    public int getAuthorizedClients() {
        return Integer.parseInt(propMap.get("current-authorized-clients"));
    }
}
