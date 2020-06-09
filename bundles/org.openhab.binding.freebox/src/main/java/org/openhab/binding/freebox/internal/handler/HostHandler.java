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

import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.freebox.internal.action.HostActions;
import org.openhab.binding.freebox.internal.api.APIRequests;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.api.model.LanHost;
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
    private @NonNullByDefault({}) HostConfiguration config;
    protected String ipAddress = "";

    public HostHandler(Thing thing, ZoneId zoneId) {
        super(thing, zoneId);
    }

    @Override
    public void initialize() {
        config = getConfigAs(HostConfiguration.class);
        super.initialize();
    }

    @Override
    protected Map<String, String> discoverAttributes() throws FreeboxException {
        final Map<String, String> properties = super.discoverAttributes();
        LanHost lanHost = getApiManager().execute(new APIRequests.LanHost(config.macAddress));
        lanHost.getNames()
                .ifPresent(names -> names.forEach(name -> properties.put(name.getSource().name(), name.getName())));
        return properties;
    }

    @Override
    protected void internalPoll() throws FreeboxException {
        LanHost lanHost = getApiManager().execute(new APIRequests.LanHost(config.macAddress));
        updateChannelOnOff(CONNECTIVITY, REACHABLE, lanHost.isReachable());
        updateChannelDateTimeState(CONNECTIVITY, LAST_SEEN, lanHost.getLastSeen());
        ipAddress = lanHost.getIpv4();
    }

    public void wol() {
        try {
            getApiManager().execute(new APIRequests.LanHostWOL(config.macAddress));
        } catch (FreeboxException e) {
            logger.debug("Thing {}: error waking up host : {}", getThing().getUID(), e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singletonList(HostActions.class);
    }

}
