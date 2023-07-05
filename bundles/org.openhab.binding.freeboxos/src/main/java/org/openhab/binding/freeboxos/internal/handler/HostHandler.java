/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.HostActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.HostIntf;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.Source;
import org.openhab.binding.freeboxos.internal.api.rest.WebSocketManager;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HostHandler} is responsible for all network equipments hosted on the network
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HostHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(HostHandler.class);

    // We start in pull mode and switch to push after a first update
    private boolean pushSubscribed = false;

    public HostHandler(Thing thing) {
        super(thing);
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        getManager(LanBrowserManager.class).getHost(getMac()).ifPresent(result -> {
            LanHost host = result.host();
            properties.put(Thing.PROPERTY_VENDOR, host.vendorName());
            host.getUPnPName().ifPresent(upnpName -> properties.put(Source.UPNP.name(), upnpName));
        });
    }

    @Override
    public void dispose() {
        try {
            getManager(WebSocketManager.class).unregisterListener(getMac());
        } catch (FreeboxException e) {
            logger.warn("Error unregistering host from the websocket: {}", e.getMessage());
        }
        super.dispose();
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        if (pushSubscribed) {
            return;
        }
        HostIntf data = getManager(LanBrowserManager.class).getHost(getMac())
                .orElseThrow(() -> new FreeboxException("Host data not found"));

        updateConnectivityChannels(data.host());
        logger.debug("Switching to push mode - refreshInterval will now be ignored for Connectivity data");
        getManager(WebSocketManager.class).registerListener(data.host().getMac(), this);
        pushSubscribed = true;
    }

    public void updateConnectivityChannels(LanHost host) {
        updateChannelOnOff(CONNECTIVITY, REACHABLE, host.reachable());
        updateChannelDateTimeState(CONNECTIVITY, LAST_SEEN, host.getLastSeen());
        updateChannelString(CONNECTIVITY, IP_ADDRESS, host.getIpv4());
        updateStatus(host.reachable() ? ThingStatus.ONLINE : ThingStatus.OFFLINE);
    }

    public void wol() {
        try {
            getManager(LanBrowserManager.class).wakeOnLan(getMac(),
                    getConfigAs(ApiConsumerConfiguration.class).password);
        } catch (FreeboxException e) {
            logger.warn("Error waking up host: {}", e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(HostActions.class);
    }
}
