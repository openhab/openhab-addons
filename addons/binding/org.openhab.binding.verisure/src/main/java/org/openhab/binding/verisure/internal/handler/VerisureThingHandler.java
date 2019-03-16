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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.DeviceStatusListener;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureThingConfiguration;
import org.openhab.binding.verisure.internal.model.VerisureBroadbandConnectionJSON;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindowJSON;
import org.openhab.binding.verisure.internal.model.VerisureThingJSON;
import org.openhab.binding.verisure.internal.model.VerisureUserPresenceJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class and handler for some of the different thing types that Verisure provides.
 *
 * @author Jarle Hjortland - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureThingHandler extends BaseThingHandler implements DeviceStatusListener {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<ThingTypeUID>();
    static {
        SUPPORTED_THING_TYPES.add(THING_TYPE_USERPRESENCE);
        SUPPORTED_THING_TYPES.add(THING_TYPE_DOORWINDOW);
        SUPPORTED_THING_TYPES.add(THING_TYPE_BROADBAND_CONNECTION);
    }

    protected final Logger logger = LoggerFactory.getLogger(VerisureThingHandler.class);

    protected @Nullable VerisureSession session;

    protected @Nullable VerisureThingConfiguration config;

    public VerisureThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);
        if (command instanceof RefreshType) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                BridgeHandler bridgeHandler = bridge.getHandler();
                if (bridgeHandler != null) {
                    bridgeHandler.handleCommand(channelUID, command);
                }
            }
            if (session != null && config.deviceId != null) {
                VerisureThingJSON thing = session.getVerisureThing(config.deviceId);
                update(thing);
            }
        } else {
            logger.warn("Unknown command! {}", command);
        }
    }

    protected void scheduleImmediateRefresh() {
        logger.debug("scheduleImmediateRefresh on thing: {}", thing);
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            VerisureBridgeHandler vbh = (VerisureBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                vbh.scheduleImmediateRefresh(VerisureBridgeHandler.REFRESH_DELAY_SECONDS);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("initialize on thing: {}", thing);
        // Do not go online
        config = getConfigAs(VerisureThingConfiguration.class);
        if (config.deviceId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Verisure device is missing deviceId");
        }
        Bridge bridge = getBridge();
        if (bridge != null) {
            this.bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void dispose() {
        logger.debug("dispose on thing: {}", thing);
        Bridge bridge = getBridge();
        if (bridge != null) {
            VerisureBridgeHandler vbh = (VerisureBridgeHandler) bridge.getHandler();
            if (vbh != null) {
                session = vbh.getSession();
                if (session != null) {
                    session.unregisterDeviceStatusListener(this);
                }
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged bridgeStatusInfo: {}", bridgeStatusInfo);
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            Bridge bridge = getBridge();
            if (bridge != null) {
                VerisureBridgeHandler vbh = (VerisureBridgeHandler) bridge.getHandler();
                if (vbh != null) {
                    session = vbh.getSession();
                    if (session != null && config.deviceId != null) {
                        update(session.getVerisureThing(config.deviceId));
                        session.registerDeviceStatusListener(this);
                    }
                }
            }
        }
        super.bridgeStatusChanged(bridgeStatusInfo);
    }

    public synchronized void update(@Nullable VerisureThingJSON thing) {
        logger.debug("update on thing: {}", thing);
        if (thing != null) {
            updateStatus(ThingStatus.ONLINE);
            if (getThing().getThingTypeUID().equals(THING_TYPE_DOORWINDOW)) {
                VerisureDoorWindowJSON obj = (VerisureDoorWindowJSON) thing;
                updateDoorWindowState(obj);
            } else if (getThing().getThingTypeUID().equals(THING_TYPE_USERPRESENCE)) {
                VerisureUserPresenceJSON obj = (VerisureUserPresenceJSON) thing;
                updateUserPresenceState(obj);
            } else if (getThing().getThingTypeUID().equals(THING_TYPE_BROADBAND_CONNECTION)) {
                VerisureBroadbandConnectionJSON obj = (VerisureBroadbandConnectionJSON) thing;
                updateBroadbandConnection(obj);
            } else {
                logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
            }
        } else {
            logger.warn("Thing JSON is null: {}", getThing().getThingTypeUID());
        }
    }

    private void updateDoorWindowState(VerisureDoorWindowJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATE);
        if ("OPEN".equals(status.getState())) {
            updateState(cuid, OpenClosedType.OPEN);
        } else {
            updateState(cuid, OpenClosedType.CLOSED);
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_LOCATION);
        updateState(cuid, new StringType(status.getArea()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
        BigDecimal siteId = status.getSiteId();
        if (siteId != null) {
            updateState(cuid, new DecimalType(siteId.intValue()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
        StringType instName = new StringType(status.getSiteName());
        updateState(cuid, instName);
    }

    private void updateUserPresenceState(VerisureUserPresenceJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_USER_LOCATION_NAME);
        updateState(cuid, new StringType(status.getLocation()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_WEBACCOUNT);
        updateState(cuid, new StringType(status.getWebAccount()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_USER_LOCATION_STATUS);
        updateState(cuid, new StringType(status.getUserLocationStatus()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
        BigDecimal siteId = status.getSiteId();
        if (siteId != null) {
            updateState(cuid, new DecimalType(status.getSiteId().intValue()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
        StringType instName = new StringType(status.getSiteName());
        updateState(cuid, instName);
    }

    private void updateBroadbandConnection(VerisureBroadbandConnectionJSON status) {
        ChannelUID cuid = new ChannelUID(getThing().getUID(), CHANNEL_TIMESTAMP);
        String lastCheckedDateTime = status.getDate();
        try {
            logger.debug("Parsing date {}", lastCheckedDateTime);
            try {
                Date date = new SimpleDateFormat("dd/MM/yy HH:mm").parse(lastCheckedDateTime);
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");//
                String strDate = sdfDate.format(date);

                logger.trace("Parsing datetime successful. Using date. {}", new DateTimeType(strDate));
                updateState(cuid, new DateTimeType(strDate));
            } catch (ParseException fpe) {
                logger.warn("Parsing date failed {}.", fpe);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Parsing date failed: {}.", e);
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_HAS_WIFI);
        Boolean hasWiFi = status.hasWiFi();
        if (hasWiFi != null) {
            updateState(cuid, new StringType(hasWiFi.toString()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_STATUS);
        updateState(cuid, new StringType(status.getStatus()));
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_ID);
        BigDecimal siteId = status.getSiteId();
        if (siteId != null) {
            updateState(cuid, new DecimalType(status.getSiteId().intValue()));
        }
        cuid = new ChannelUID(getThing().getUID(), CHANNEL_SITE_INSTALLATION_NAME);
        StringType instName = new StringType(status.getSiteName());
        updateState(cuid, instName);
    }

    @Override
    public void onDeviceStateChanged(@Nullable VerisureThingJSON thing) {
        logger.trace("onDeviceStateChanged on thing: {}", thing);
        if (thing != null) {
            String id = thing.getDeviceId();
            if (config.deviceId.equals(id)) {
                update(thing);
            }
        }
    }

    @Override
    public void onDeviceRemoved(@Nullable VerisureThingJSON thing) {
        logger.trace("onDeviceRemoved on thing: {}", thing);
    }

    @Override
    public void onDeviceAdded(@Nullable VerisureThingJSON thing) {
        logger.trace("onDeviceAdded on thing: {}", thing);
    }
}
