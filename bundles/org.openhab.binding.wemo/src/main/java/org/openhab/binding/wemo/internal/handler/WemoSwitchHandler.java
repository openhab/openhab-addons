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
package org.openhab.binding.wemo.internal.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoSwitchHandler} is responsible for handling commands for
 * a WeMo device supporting a binary switch: Socket or Light Switch.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoSwitchHandler extends WemoHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoSwitchHandler.class);
    private final Map<String, String> stateMap = new ConcurrentHashMap<>();

    public WemoSwitchHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WemoSwitchHandler for thing '{}'", thing.getUID());
        updateStatus(ThingStatus.UNKNOWN);
        super.initialize();
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });

        updateStatus(ThingStatus.ONLINE);

        if (!"BinaryState".equals(variable)) {
            return;
        }

        String oldValue = this.stateMap.get(variable);
        if (variable != null && value != null) {
            this.stateMap.put(variable, value);
        }

        if (value != null && value.length() == 1) {
            String binaryState = stateMap.get("BinaryState");
            if (binaryState != null) {
                if (oldValue == null || !oldValue.equals(binaryState)) {
                    State state = OnOffType.from(!"0".equals(binaryState));
                    logger.debug("State '{}' for device '{}' received", state, getThing().getUID());
                    updateState(WemoBindingConstants.CHANNEL_STATE, state);
                }
            }
        }
    }
}
