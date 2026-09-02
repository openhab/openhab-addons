/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.FreeplugActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.FreeplugManager;
import org.openhab.binding.freeboxos.internal.api.rest.FreeplugManager.Freeplug;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link FreeplugHandler} is responsible for handling everything associated to a
 * powerline gateway managed by the freebox server
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeplugHandler extends ApiConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(FreeplugHandler.class);

    public FreeplugHandler(Thing thing) {
        super(thing);
        statusDrivenByBridge = false;
    }

    private MACAddress getCheckMac() throws FreeboxException {
        MACAddress mac = getMac();
        if (mac == null) {
            throw new FreeboxException("MAC address is undefined for the thing " + thing.getUID());
        }
        return mac;
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        MACAddress mac = getCheckMac();
        if (getManager(FreeplugManager.class).getPlug(mac) instanceof Freeplug plug) {
            properties.put(Thing.PROPERTY_MODEL_ID, plug.model());
            properties.put(ROLE, plug.netRole().name());
            properties.put(NET_ID, plug.netId());
            properties.put(ETHERNET_SPEED, "%d Mb/s".formatted(plug.ethSpeed()));
            properties.put(LOCAL, Boolean.toString(plug.local()));
            properties.put(FULL_DUPLEX, Boolean.toString(plug.ethFullDuplex()));

            if (plug.local()) { // Plug connected to the freebox does not provide rate up or down
                List<Channel> channels = new ArrayList<>(getThing().getChannels());
                int nbInit = channels.size();
                channels.removeIf(channel -> channel.getUID().getId().contains(RATE));
                if (nbInit != channels.size()) {
                    updateThing(editThing().withChannels(channels).build());
                }
            }
        } else {
            throw new FreeboxException("Freeplug is absent");
        }
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        MACAddress mac = getCheckMac();
        if (getManager(FreeplugManager.class).getPlug(mac) instanceof Freeplug plug) {
            updateChannelDateTimeState(LAST_SEEN, Instant.now().minusSeconds(plug.inactive()));

            updateChannelString(LINE_STATUS, plug.ethPortStatus());
            updateChannelOnOff(REACHABLE, plug.hasNetwork());

            updateRateChannel(RATE + "-down", plug.rxRate());
            updateRateChannel(RATE + "-up", plug.txRate());
            if (plug.hasNetwork()) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/info-plug-not-reachable");
            }
        } else {
            throw new FreeboxException("Freeplug is absent");
        }
    }

    private void updateRateChannel(String channel, int rate) {
        // According to https://dev.freebox.fr/bugs/task/35895
        updateChannelQuantity(channel, new QuantityType<>(rate > 0 ? rate : 9, Units.MEGABIT_PER_SECOND));
    }

    public void reset() {
        try {
            MACAddress mac = getCheckMac();
            getManager(FreeplugManager.class).reboot(mac);
            logger.debug("Freeplug {} successfully restarted", mac);
        } catch (FreeboxException e) {
            logger.warn("Error restarting freeplug {}: {}", thing.getUID(), e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(FreeplugActions.class);
    }
}
