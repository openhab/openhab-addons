/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.model.VerisureSmartPlugJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;

/**
 * Handler for the Smart Plug Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureSmartPlugThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_SMARTPLUG);
    }

    public VerisureSmartPlugThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else if (channelUID.getId().equals(CHANNEL_SET_SMARTPLUG_STATUS)) {
            handleSmartPlugState(command);
            scheduleImmediateRefresh();
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    private void handleSmartPlugState(Command command) {
        if (session != null && this.id != null) {
            VerisureSmartPlugJSON smartPlug = (VerisureSmartPlugJSON) session.getVerisureThing(this.id);
            if (smartPlug != null) {
                String siteName = smartPlug.getSiteName();
                if (siteName != null) {
                    String smartPlugUrl = this.id.replaceAll("_", "+");
                    String url = SMARTPLUG_COMMAND;
                    String data = null;
                    if (command == OnOffType.OFF) {
                        data = "targetDeviceLabel=" + smartPlugUrl + "&targetOn=off";
                        logger.debug("Trying to set SmartPlug state to off with URL {} and data {}", url, data);
                    } else if (command == OnOffType.ON) {
                        data = "targetDeviceLabel=" + smartPlugUrl + "&targetOn=on";
                        logger.debug("Trying to set SmartPlug state to on with URL {} and data {}", url, data);
                    } else {
                        logger.debug("Unknown command! {}", command);
                        return;
                    }
                    session.sendCommand(siteName, url, data);
                    ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
                    updateState(cuid, new StringType("pending"));
                }
            }
        }
    }

    @Override
    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        updateStatus(ThingStatus.ONLINE);
        if (getThing().getThingTypeUID().equals(THING_TYPE_SMARTPLUG)) {
            VerisureSmartPlugJSON obj = (VerisureSmartPlugJSON) thing;
            if (obj != null) {
                updateSmartPlugState(obj);
            }
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateSmartPlugState(VerisureSmartPlugJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
        String smartPlugStatus = status.getStatus();
        updateState(cuid, new StringType(smartPlugStatus));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTPLUG_STATUS);
        if ("on".equals(smartPlugStatus)) {
            updateState(cuid, OnOffType.ON);
        } else if ("off".equals(smartPlugStatus)) {
            updateState(cuid, OnOffType.OFF);
        } else if ("pending".equals(smartPlugStatus)) {
            // Schedule another refresh.
            this.scheduleImmediateRefresh();
        } else {
            logger.warn("Unknown SmartPLug status: {}", smartPlugStatus);
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SMARTPLUG_STATUS);
        updateState(cuid, new StringType(status.getStatusText()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        updateState(cuid, new StringType(status.getLocation()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_HAZARDOUS);
        updateState(cuid, new StringType(status.getHazardous().toString()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
        BigDecimal siteId = status.getSiteId();
        if (siteId != null) {
            updateState(cuid, new DecimalType(status.getSiteId().intValue()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
        StringType instName = new StringType(status.getSiteName());
        updateState(cuid, instName);
    }
}
