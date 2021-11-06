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
package org.openhab.binding.openwebnet.internal.handler;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.Alarm;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAlarm;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAlarmHandler} is responsible for handling commands/messages for Alarm Central Unit and zones. It
 * extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetAlarmHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAlarmHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.ALARM_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = -1; // timestamp when the last request for all device refresh was sent
    // for this handler

    protected static final int ALL_DEVICES_REFRESH_INTERVAL_MSEC = 5000; // interval in msec before sending another all
    // devices refresh request

    public OpenWebNetAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void handleChannelCommand(@NonNull ChannelUID channel, @NonNull Command command) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void requestChannelState(@NonNull ChannelUID channel) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        OpenWebNetBridgeHandler brH = bridgeHandler;
        if (brH != null) {
            if (brH.isBusGateway() && refreshAll) {
                long now = System.currentTimeMillis();
                if (now - lastAllDevicesRefreshTS > ALL_DEVICES_REFRESH_INTERVAL_MSEC) {
                    try {
                        send(Alarm.requestSystemStatus());
                        lastAllDevicesRefreshTS = now;
                    } catch (OWNException e) {
                        logger.warn("Excpetion while requesting all devices refresh: {}", e.getMessage());
                    }
                } else {
                    logger.debug("Refresh all devices just sent...");
                }
            } else { // single device
                if (deviceWhere != null) {
                    String w = deviceWhere.value();
                    try {
                        send(Alarm.requestZoneStatus(w));
                    } catch (OWNException e) {
                        logger.warn("refreshDevice() where='{}' returned OWNException {}", w, e.getMessage());
                    }
                }

            }
        }
    }

    @Override
    protected @NonNull Where buildBusWhere(@NonNull String wStr) throws IllegalArgumentException {
        return new WhereAlarm(wStr);
    }

    @Override
    protected @NonNull String ownIdPrefix() {
        return Who.BURGLAR_ALARM.value().toString();
    }
}
