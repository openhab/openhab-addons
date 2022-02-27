/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.wemo.internal.InsightParser;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WemoInsightHandler} is responsible for handling commands for
 * a WeMo Insight Switch.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoInsightHandler extends WemoHandler {

    private final Logger logger = LoggerFactory.getLogger(WemoInsightHandler.class);
    private final Map<String, String> stateMap = new ConcurrentHashMap<String, String>();

    public WemoInsightHandler(Thing thing, UpnpIOService upnpIOService, WemoHttpCall wemoHttpCaller) {
        super(thing, upnpIOService, wemoHttpCaller);
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received pair '{}':'{}' (service '{}') for thing '{}'",
                new Object[] { variable, value, service, this.getThing().getUID() });

        updateStatus(ThingStatus.ONLINE);

        if (!"BinaryState".equals(variable) && !"InsightParams".equals(variable)) {
            return;
        }

        if (variable != null && value != null) {
            this.stateMap.put(variable, value);
        }

        if (value != null && value.length() > 1) {
            String insightParams = stateMap.get(variable);

            if (insightParams != null) {
                InsightParser parser = new InsightParser(insightParams);
                Map<String, State> results = parser.parse();
                results.forEach((channel, state) -> {
                    logger.trace("New InsightParam {} '{}' for device '{}' received", channel, state,
                            getThing().getUID());
                    updateState(channel, state);
                });

                // Update helper channel onStandBy by checking if currentPower > standByLimit.
                var standByLimit = (QuantityType<?>) results.get(WemoBindingConstants.CHANNEL_STANDBYLIMIT);
                if (standByLimit != null) {
                    var currentPower = (QuantityType<?>) results.get(WemoBindingConstants.CHANNEL_CURRENTPOWER);
                    if (currentPower != null) {
                        updateState(WemoBindingConstants.CHANNEL_ONSTANDBY,
                                OnOffType.from(currentPower.intValue() <= standByLimit.intValue()));
                    }
                }
            }
        }
    }
}
