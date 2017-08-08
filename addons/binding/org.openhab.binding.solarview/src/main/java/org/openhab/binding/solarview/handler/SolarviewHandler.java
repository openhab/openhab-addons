/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.handler;

import static org.openhab.binding.solarview.SolarviewBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.solarview.internal.Device.SolarviewDeviceType;
import org.openhab.binding.solarview.internal.Energy;
import org.openhab.binding.solarview.internal.Energy.Channel;
import org.openhab.binding.solarview.internal.config.SolarviewThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SolarviewHandler} is responsible for handling commands, which are
 * sent via {@link SolarviewBridgeHandler} to one of the channels.
 *
 * @author Guenther Schreiner - Initial contribution
 */
public class SolarviewHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(SolarviewHandler.class);
    /**
     * Set of things provided by {@link SolarviewHandler}.
     */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ENERGY_PRODUCTION,
            THING_TYPE_ENERGY_INJECTION, THING_TYPE_ENERGY_IMPORT, THING_TYPE_PRODUCTION_INVERTER_ONE,
            THING_TYPE_PRODUCTION_INVERTER_TWO, THING_TYPE_PRODUCTION_INVERTER_THREE,
            THING_TYPE_PRODUCTION_INVERTER_FOUR, THING_TYPE_PRODUCTION_INVERTER_FIVE,
            THING_TYPE_PRODUCTION_INVERTER_SIX, THING_TYPE_PRODUCTION_INVERTER_SEVEN,
            THING_TYPE_PRODUCTION_INVERTER_EIGHT, THING_TYPE_PRODUCTION_INVERTER_NINE);

    private SolarviewThingConfiguration configuration = null;
    private SolarviewBridgeHandler bridgeHandler;

    ScheduledFuture<?> refreshJob;

    private boolean propertiesInitializedSuccessfully = false;

    /**
     * Solarview query details for Thing
     */
    private String queryString;
    /**
     * Server provided energy information for this Thing
     */
    private Energy energyInformation;

    public SolarviewHandler(Thing thing) {
        super(thing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        logger.debug("Initializing thing {}", getThing().getUID());
        initializeThing((getBridge() == null) ? null : getBridge().getStatus());
        logger.trace("done with initialization of thing {}", getThing().getUID());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        logger.debug("initializeThing thing {} bridge status {}", getThing().getUID(), bridgeStatus);

        if (getSolarviewBridgeHandler() != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                initializeProperties();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
        }
    }

    private synchronized SolarviewBridgeHandler getSolarviewBridgeHandler() {
        logger.trace("getSolarviewBridgeHandler() called.");
        if (this.bridgeHandler == null) {
            logger.trace("no bridge yet active.");
            Bridge bridge = getBridge();
            logger.trace("returned bridge {}.", bridge);
            if (bridge == null) {
                logger.trace("returning null");
                return null;
            }
            logger.trace("continuing");
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof SolarviewBridgeHandler) {
                this.bridgeHandler = (SolarviewBridgeHandler) handler;
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }

    private synchronized void initializeProperties() {

        configuration = getConfigAs(SolarviewThingConfiguration.class);

        if (!propertiesInitializedSuccessfully) {
            ThingTypeUID thingTypeUID = getThing().getThingTypeUID();
            // Determine the ThingType and choose appropriate device type
            if (thingTypeUID.equals(THING_TYPE_ENERGY_PRODUCTION)) {
                this.queryString = SolarviewDeviceType.METER_PRODUCTION.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_ENERGY_INJECTION)) {
                this.queryString = SolarviewDeviceType.METER_INJECTION.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_ENERGY_IMPORT)) {
                this.queryString = SolarviewDeviceType.METER_IMPORT.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_ONE)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_ONE.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_TWO)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_TWO.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_THREE)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_THREE.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_FOUR)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_FOUR.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_FIVE)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_FIVE.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_SIX)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_SIX.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_SEVEN)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_SEVEN.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_EIGHT)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_EIGHT.getQueryString();
            } else if (thingTypeUID.equals(THING_TYPE_PRODUCTION_INVERTER_NINE)) {
                this.queryString = SolarviewDeviceType.METER_INVERTER_NINE.getQueryString();
            } else {
                logger.error("Could not initialize query as UID {} is unknown", thingTypeUID);
                return;
            }
            logger.trace("initialize(): Solarview Query String set to {}.", queryString);
            propertiesInitializedSuccessfully = true;
        }
        scheduleRefreshJob();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        logger.trace("SolarviewHandler disposed.");
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        super.dispose();
    }

    /**
     * Start the periodic information retrieval from the <B>Solarview</B> server.
     */
    private void scheduleRefreshJob() {
        logger.trace("scheduleRefreshJob() called.");
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.trace("(refreshJob) started.");
            try {
                Energy thisEnergy = bridgeHandler.updateEnergyDataFromServer(queryString);

                if (thisEnergy != null) {
                    for (final Channel val : Channel.values()) {
                        logger.trace("(refreshJob) updating state of {} to {}.",
                                new ChannelUID(getThing().getUID(), val.name()), thisEnergy.getChannelValue(val));
                        updateState(new ChannelUID(getThing().getUID(), val.name()), thisEnergy.getChannelValue(val));
                    }
                    logger.trace("(refreshJob) updating ThingStatus to ONLINE.");
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    logger.trace("(refreshJob) updating ThingStatus to OFFLINE.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                }
            } catch (Exception e) {
                logger.error("Exception occurred during execution: {}", e.getMessage());
                logger.trace("(refreshJob) updating ThingStatus to OFFLINE.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
            }

            logger.trace("(refreshJob) finished.");
        }, 0, configuration.refreshSecs, TimeUnit.SECONDS);
        logger.trace("scheduleRefreshJob() finished.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) called.", channelUID.getAsString(), command);

        Bridge solarviewBridge = getBridge();
        if (solarviewBridge == null) {
            logger.warn("Cannot handle command without bridge.");
            return;
        }
        SolarviewBridgeHandler solarviewBridgeHandler = (SolarviewBridgeHandler) solarviewBridge.getHandler();

        if (command instanceof RefreshType) {
            energyInformation = solarviewBridgeHandler.updateEnergyDataFromServer(queryString);
            if (energyInformation != null) {
                updateState(channelUID, energyInformation.getChannelValue(channelUID.getId()));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.BRIDGE_OFFLINE);
            }
        } else {
            logger.error("Command {} is not supported for channel: {}.", command, channelUID.getId());
        }
    }

}
/**
 * end-of-SolarviewHandler.java
 */
