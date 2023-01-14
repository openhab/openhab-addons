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
package org.openhab.binding.mystrom.internal;

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_POWER;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_SWITCH;
import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.CHANNEL_TEMPERATURE;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.WATT;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyStromPlugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Frank - Initial contribution
 * @author Frederic Chastagnol - Extends from new abstract class
 */
@NonNullByDefault
public class MyStromPlugHandler extends AbstractMyStromHandler {

    private static class MyStromReport {

        public float power;
        public boolean relay;
        public float temperature;
    }

    private final Logger logger = LoggerFactory.getLogger(MyStromPlugHandler.class);

    private final ExpiringCache<MyStromReport> cache = new ExpiringCache<>(Duration.ofSeconds(3), this::getReport);

    public MyStromPlugHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                pollDevice();
            } else {
                if (command instanceof OnOffType && CHANNEL_SWITCH.equals(channelUID.getId())) {
                    sendHttpRequest(HttpMethod.GET, "/relay?state=" + (command == OnOffType.ON ? "1" : "0"), null);
                    scheduler.schedule(this::pollDevice, 500, TimeUnit.MILLISECONDS);
                }
            }
        } catch (MyStromException e) {
            logger.warn("Error while handling command {}", e.getMessage());
        }
    }

    private @Nullable MyStromReport getReport() {
        try {
            String returnContent = sendHttpRequest(HttpMethod.GET, "/report", null);
            MyStromReport report = gson.fromJson(returnContent, MyStromReport.class);
            updateStatus(ThingStatus.ONLINE);
            return report;
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return null;
        }
    }

    @Override
    protected void pollDevice() {
        MyStromReport report = cache.getValue();
        if (report != null) {
            updateState(CHANNEL_SWITCH, report.relay ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_POWER, QuantityType.valueOf(report.power, WATT));
            updateState(CHANNEL_TEMPERATURE, QuantityType.valueOf(report.temperature, CELSIUS));
        }
    }
}
