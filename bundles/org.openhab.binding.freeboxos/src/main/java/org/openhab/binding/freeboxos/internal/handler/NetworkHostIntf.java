package org.openhab.binding.freeboxos.internal.handler;

import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;

public interface NetworkHostIntf {
    Configuration getConfig();

    default String getMac() {
        String mac = (String) getConfig().get(Thing.PROPERTY_MAC_ADDRESS);
        return mac.toLowerCase();
    }
}
