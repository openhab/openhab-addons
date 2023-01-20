package org.openhab.binding.freeboxos.internal.handler;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.config.ClientConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandler;

import inet.ipaddr.MACAddressString;
import inet.ipaddr.mac.MACAddress;

@NonNullByDefault
public interface ApiConsumerIntf extends ThingHandler {

    ScheduledExecutorService getScheduler();

    Map<String, String> editProperties();

    Configuration getConfig();

    void updateProperties(@Nullable Map<String, String> properties);

    void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description);

    void stopRefreshJob();

    default int getClientId() {
        return ((BigDecimal) getConfig().get(ClientConfiguration.ID)).intValue();
    }

    default MACAddress getMac() {
        String mac = (String) getConfig().get(Thing.PROPERTY_MAC_ADDRESS);
        return new MACAddressString(mac).getAddress();
    }
}
