/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.HostActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.Source;
import org.openhab.binding.freeboxos.internal.api.rest.WebSocketManager;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link HostHandler} is responsible for all network equipments hosted on the network
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class HostHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(HostHandler.class);

    // We start in pull mode and switch to push after a first update...
    protected boolean pushSubscribed = false;

    protected boolean statusDrivenByLanConnectivity = true;

    protected boolean reachable;

    private int tryConfigureMediaSink = 1;

    public HostHandler(Thing thing) {
        super(thing);
        statusDrivenByBridge = false;
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        LanHost host = getLanHost();
        properties.put(Thing.PROPERTY_VENDOR, host.vendorName());
        host.getName(Source.UPNP).ifPresent(upnpName -> properties.put(Source.UPNP.name(), upnpName));
    }

    @Override
    public void dispose() {
        cancelPushSubscription();
        super.dispose();
    }

    protected void cancelPushSubscription() {
        MACAddress mac = getMac();
        if (pushSubscribed && mac != null) {
            try {
                getManager(WebSocketManager.class).unregisterListener(mac);
            } catch (FreeboxException e) {
                logger.warn("Error unregistering host from the websocket: {}", e.getMessage());
            }
            pushSubscribed = false;
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        if (tryConfigureMediaSink > 0) {
            configureMediaSink();
            tryConfigureMediaSink--;
        }

        if (pushSubscribed) {
            return;
        }

        LanHost host = getLanHost();
        updateConnectivityChannels(host);
        logger.debug("{}: switching to push mode - refreshInterval will now be ignored for Connectivity data",
                thing.getUID());
        pushSubscribed = getManager(WebSocketManager.class).registerListener(host.getMac(), this);
    }

    @Override
    protected void internalForcePoll() throws FreeboxException {
        LanHost host = getLanHost();
        updateConnectivityChannels(host);
    }

    protected LanHost getLanHost() throws FreeboxException {
        MACAddress mac = getMac();
        if (mac == null) {
            throw new FreeboxException(
                    "getLanHost is not possible because MAC address is undefined for the thing " + thing.getUID());
        }
        return getManager(LanBrowserManager.class).getHost(mac).map(hostIntf -> hostIntf.host())
                .orElseThrow(() -> new FreeboxException("Host data not found"));
    }

    public void updateConnectivityChannels(LanHost host) {
        logger.debug("{}: updateConnectivityChannels with host.reachable() = {}", thing.getUID(), host.reachable());
        updateChannelOnOff(GROUP_CONNECTIVITY, REACHABLE, host.reachable());
        updateChannelDateTimeState(GROUP_CONNECTIVITY, LAST_SEEN, host.getLastSeen());
        updateChannelString(GROUP_CONNECTIVITY, IP_ADDRESS, host.getIpv4());
        if (statusDrivenByLanConnectivity) {
            if (host.reachable()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/info-host-not-reachable");
            }
        }
        // We will check and configure audio sink only when the host reachability changed
        if (reachable != host.reachable()) {
            reachable = host.reachable();
            // It can take time until the Media Receiver API returns the receiver after it becomes reachable.
            // So this will be checked during the next 2 polls.
            tryConfigureMediaSink = 2;
        }
    }

    public void wol() {
        MACAddress mac = getMac();
        if (mac == null) {
            logger.warn("Waking up host is not possible because MAC address is undefined for the thing {}",
                    thing.getUID());
            return;
        }
        try {
            getManager(LanBrowserManager.class).wakeOnLan(mac, getConfigAs(ApiConsumerConfiguration.class).password);
        } catch (FreeboxException e) {
            logger.warn("Error waking up host: {}", e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HostActions.class);
    }
}
