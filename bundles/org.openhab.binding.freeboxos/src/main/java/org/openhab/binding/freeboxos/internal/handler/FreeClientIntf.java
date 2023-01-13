package org.openhab.binding.freeboxos.internal.handler;

import java.math.BigDecimal;

import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.config.core.Configuration;

public interface FreeClientIntf {
    Configuration getConfig();

    default int getClientId() {
        return ((BigDecimal) getConfig().get(ClientConfiguration.ID)).intValue();
    }

}
