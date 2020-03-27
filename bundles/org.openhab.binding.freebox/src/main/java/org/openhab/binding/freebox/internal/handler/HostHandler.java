/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.handler;

import static org.openhab.binding.freebox.internal.FreeboxBindingConstants.*;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.freebox.internal.action.HostActions;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.LanHost;
import org.openhab.binding.freebox.internal.api.model.LanHostResponse;
import org.openhab.binding.freebox.internal.api.model.LanHostWOLConfig;
import org.openhab.binding.freebox.internal.config.HostConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HostHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - use new internal API manager
 */
@NonNullByDefault
public class HostHandler extends APIConsumerHandler {
    private final Logger logger = LoggerFactory.getLogger(HostHandler.class);
    private @NonNullByDefault({}) String netAddress;

    public HostHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_HOST)
                || getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_VM)) {
            netAddress = getConfigAs(HostConfiguration.class).macAddress;
        } else if (getThing().getThingTypeUID().equals(FREEBOX_THING_TYPE_PLAYER)) {
            netAddress = thing.getProperties().get(Thing.PROPERTY_MAC_ADDRESS);
        }
        netAddress = (netAddress == null) ? "" : netAddress;
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        LanHost lanHost = getApiManager().executeGet(LanHostResponse.class, "ether-" + netAddress);
        updateChannelOnOff(CONNECTIVITY, REACHABLE, lanHost.isReachable());
        updateChannelDateTimeState(CONNECTIVITY, LAST_SEEN, lanHost.getLastSeen());
    }

    public void wol() {
        LanHostWOLConfig wol = new LanHostWOLConfig(netAddress);
        try {
            getApiManager().execute(wol, null);
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error waking up host : {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(HostActions.class);
    }

}
