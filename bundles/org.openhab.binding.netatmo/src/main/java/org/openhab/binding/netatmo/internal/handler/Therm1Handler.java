/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAThermostat;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;

/**
 * {@link Therm1Handler} is the class used to handle the thermostat
 * module of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Therm1Handler extends NetatmoDeviceHandler {

    // private final Logger logger = LoggerFactory.getLogger(Therm1Handler.class);

    public Therm1Handler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    private @NonNullByDefault({}) HomeEnergyHandler getHomeHandler() {
        Bridge bridge = super.getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.ONLINE) {
            PlugHandler plughandler = (PlugHandler) bridge.getHandler();
            if (plughandler != null) {
                return plughandler.getHomeHandler();
            }
        }
        return null;
    }

    @Override
    protected NAThermostat updateReadings() throws NetatmoException {
        HomeEnergyHandler handler = getHomeHandler();
        if (handler != null) {
            NAHome localHome = handler.getHome();
            if (localHome != null) {
                return (NAThermostat) Objects.requireNonNullElse(localHome.getModule(config.id), new NAThermostat());
            }
        }
        return new NAThermostat();
    }
}
