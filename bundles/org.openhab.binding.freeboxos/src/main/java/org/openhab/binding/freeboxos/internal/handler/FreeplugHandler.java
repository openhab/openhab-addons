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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.action.FreeplugActions;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.FreeplugManager;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    @Override
    void initializeProperties(Map<String, String> properties) throws FreeboxException {
        getManager(FreeplugManager.class).getPlug(getMac()).ifPresent(plug -> {
            properties.put(Thing.PROPERTY_MODEL_ID, plug.model());
            properties.put(ROLE, plug.netRole().name());
            properties.put(NET_ID, plug.netId());
            properties.put(ETHERNET_SPEED, "%d Mb/s".formatted(plug.ethSpeed()));
            properties.put(LOCAL, Boolean.toString(plug.local()));
            properties.put(FULL_DUPLEX, Boolean.toString(plug.ethFullDuplex()));

            if (plug.local()) { // Plug connected to the freebox does not provide rate up or down
                List<Channel> channels = new ArrayList<>(getThing().getChannels());
                channels.removeIf(channel -> channel.getUID().getId().contains(RATE));
                updateThing(editThing().withChannels(channels).build());
            }
        });
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        getManager(FreeplugManager.class).getPlug(getMac()).ifPresent(plug -> {
            updateChannelDateTimeState(LAST_SEEN, ZonedDateTime.now().minusSeconds(plug.inactive()));

            updateChannelString(LINE_STATUS, plug.ethPortStatus());
            updateChannelOnOff(REACHABLE, plug.hasNetwork());

            updateRateChannel(RATE + "-down", plug.rxRate());
            updateRateChannel(RATE + "-up", plug.txRate());
        });
    }

    private void updateRateChannel(String channel, int rate) {
        // According to https://dev.freebox.fr/bugs/task/35895
        updateChannelQuantity(channel, new QuantityType<>(rate > 0 ? rate : 9, Units.MEGABIT_PER_SECOND));
    }

    public void reset() {
        try {
            getManager(FreeplugManager.class).reboot(getMac());
            logger.debug("Freeplug {} succesfully restarted", getMac());
        } catch (FreeboxException e) {
            logger.warn("Error restarting freeplug {}: {}", getMac(), e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(FreeplugActions.class);
    }
}
