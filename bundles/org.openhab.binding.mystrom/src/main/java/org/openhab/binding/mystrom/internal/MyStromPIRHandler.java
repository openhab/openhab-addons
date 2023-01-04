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
package org.openhab.binding.mystrom.internal;

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.PERCENT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

import com.google.gson.JsonParseException;

/**
 *
 * @author Stefan Navratil - Initial Contribution
 *
 */

@NonNullByDefault
public class MyStromPIRHandler extends AbstractMyStromHandler {

    private static class MyStromReport {

        public float light;
        public boolean motion;
        public float temperature;
    }

    public MyStromPIRHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
        try {
            sendHttpRequest(HttpMethod.POST, "/api/v1/settings/pir",
                    "{\"backoff_time\":" + config.getBackoffTime() + ",\"led_enable\":" + config.getLedEnable() + "}");
        } catch (MyStromException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    protected void pollDevice() {
        MyStromReport report = getReport();
        if (report != null) {
            updateState(CHANNEL_MOTION, OnOffType.from(report.motion));
            updateState(CHANNEL_TEMPERATURE, QuantityType.valueOf(report.temperature, CELSIUS));
            // The Default Light thresholds are from 30 to 300.
            updateState(CHANNEL_LIGHT, QuantityType.valueOf(report.light / 3, PERCENT));
        }
    }

    private @Nullable MyStromReport getReport() {
        try {
            String json = sendHttpRequest(HttpMethod.GET, "/api/v1/sensors", null);
            MyStromReport report = gson.fromJson(json, MyStromReport.class);
            updateStatus(ThingStatus.ONLINE);
            return report;
        } catch (MyStromException | JsonParseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
    }
}
