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
package org.openhab.binding.solarwatt.internal.handler;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.THING_PROPERTIES_GUID;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solarwatt.internal.SolarwattConfiguration;
import org.openhab.binding.solarwatt.internal.channel.SolarwattChannelTypeProvider;
import org.openhab.binding.solarwatt.internal.discovery.SolarwattDevicesDiscoveryService;
import org.openhab.binding.solarwatt.internal.domain.SolarwattChannel;
import org.openhab.binding.solarwatt.internal.domain.model.Device;
import org.openhab.binding.solarwatt.internal.domain.model.EnergyManager;
import org.openhab.binding.solarwatt.internal.exception.SolarwattConnectionException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergyManagerHandler} is responsible for handling energy manager thing itself
 * and handle data retrieval for the child things.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class EnergyManagerHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EnergyManagerHandler.class);

    private final EnergyManagerConnector connector;
    private final SolarwattChannelTypeProvider channelTypeProvider;

    private final Map<String, ThingHandler> childHandlers = new HashMap<>();
    private @Nullable ExpiringCache<Map<String, Device>> devicesCache;
    private @Nullable ScheduledFuture<?> refreshJob;

    /**
     * Guid of this energy manager itself.
     */
    private @Nullable String energyManagerGuid;

    /**
     * Runner for the {@link ExpiringCache} refresh.
     *
     * Triggers update of all child things.
     */
    private final Runnable refreshRunnable = () -> {
        EnergyManagerHandler.this.updateChannels();
        EnergyManagerHandler.this.updateAllChildThings();
    };

    /**
     * Create the handler.
     *
     * @param thing for which the handler is responsible
     * @param channelTypeProvider provider for the channels
     * @param httpClient connect to energy manager via this client
     */
    public EnergyManagerHandler(final Bridge thing, final SolarwattChannelTypeProvider channelTypeProvider,
            final HttpClient httpClient) {
        super(thing);
        this.logger.debug("{} created", this);
        this.connector = new EnergyManagerConnector(httpClient);
        this.channelTypeProvider = channelTypeProvider;
    }

    /**
     * Get services which are provided by this handler.
     *
     * Only service discovery is provided by
     * 
     * @return collection containing our discovery service
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(SolarwattDevicesDiscoveryService.class);
    }

    /**
     * Execute the desirec commands.
     *
     * Only refresh is supported and relayed to all childs of this thing.
     *
     * @param channelUID for which the command is issued
     * @param command command issued
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        this.logger.debug("handleCommand called for channel: {}, command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            this.updateChannels();
        }
    }

    /**
     * Dynnamically updates all known channel states of the energy manager.
     */
    public void updateChannels() {
        this.logger.trace("{} updateChannels", this);
        @Nullable
        Map<String, Device> devices = this.getDevices();
        if (devices != null) {
            if (this.energyManagerGuid == null) {
                try {
                    this.findEnergyManagerGuid(devices);
                } catch (SolarwattConnectionException ex) {
                    this.logger.error("Failed updating EnergyManager channels: {}", ex.getMessage());
                }
            }
            @Nullable
            EnergyManager energyManager = (EnergyManager) devices.get(this.energyManagerGuid);
            if (energyManager != null) {
                Map<String, String> properties = this.editProperties();
                properties.put(THING_PROPERTIES_GUID, energyManager.getGuid());
                this.updateProperties(properties);
                energyManager.getStateValues().forEach((stateName, stateValue) -> {
                    this.logger.trace("{}: {} - {}", this.getThing().getUID(), stateName, stateValue.toFullString());
                    this.updateState(stateName, stateValue);
                });
            } else {
                this.logger.error("updateChannels failed, missing device EnergyManager {}", this.energyManagerGuid);
            }
        }
    }

    /**
     * Initial setup of the channels available for this thing.
     *
     * @param device which provides the channels
     */
    protected void initDeviceChannels(Device device) {
        this.logger.debug("{}: initDeviceChannels for device {}", this, this.getThing().getUID());

        device.getSolarwattChannelSet().forEach((channelTag, solarwattChannel) -> {
            this.logger.trace("{}: {}", this.getThing().getUID(), solarwattChannel.getChannelName());
            this.assertChannel(solarwattChannel);
        });
    }

    /**
     * Assert that all channels inside of our thing are well defined.
     *
     * Only channel which can not be found are created.
     *
     * @param solarwattChannel channel description with name and unit
     */
    protected void assertChannel(SolarwattChannel solarwattChannel) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), solarwattChannel.getChannelName());
        ChannelTypeUID channelType = this.channelTypeProvider.assertChannelType(solarwattChannel);
        if (this.getThing().getChannel(channelUID) == null) {
            ThingBuilder thingBuilder = this.editThing();
            thingBuilder.withChannel(
                    AbstractDeviceHandler.getChannelBuilder(solarwattChannel, channelUID, channelType).build());

            this.updateThing(thingBuilder.build());
        }
    }

    /**
     * Finds the guid of the energy manager inside of the known devices.
     *
     * @param devices list with known devices
     * @throws SolarwattConnectionException if there is no energy manager available
     */
    private void findEnergyManagerGuid(Map<String, Device> devices) throws SolarwattConnectionException {
        devices.forEach((guid, device) -> {
            if (device instanceof EnergyManager) {
                this.energyManagerGuid = guid;
            }
        });

        if (this.energyManagerGuid == null) {
            throw new SolarwattConnectionException("unable to find energy manager");
        }
    }

    /**
     * Setup the handler and trigger initial load via {@link EnergyManagerHandler::refreshDevices}.
     *
     * Web request against energy manager and loading of devices is deferred and will send the ONLINE
     * event after loading all devices.
     */
    @Override
    public void initialize() {
        this.logger.debug("{} initialize", this);
        @Nullable
        SolarwattConfiguration localConfig = this.getConfigAs(SolarwattConfiguration.class);
        this.initRefresh(localConfig);
        this.initDeviceCache(localConfig);
    }

    private void initDeviceCache(SolarwattConfiguration localConfig) {
        if (localConfig.hostname.isEmpty()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname is not set");
        } else {
            this.updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Waiting to retrieve devices.");
            this.connector.setConfiguration(localConfig);

            this.devicesCache = new ExpiringCache<>(Duration.of(localConfig.refresh, ChronoUnit.SECONDS),
                    this::refreshDevices);

            @Nullable
            ExpiringCache<Map<String, Device>> localDevicesCache = this.devicesCache;
            if (localDevicesCache != null) {
                // trigger initial load
                this.scheduler.execute(localDevicesCache::getValue);
            }
        }
    }

    /**
     * Stop the refresh job and remove devices.
     */
    @Override
    public void dispose() {
        this.logger.debug("{} dispose", this);

        this.childHandlers.clear();

        @Nullable
        ScheduledFuture<?> localRefreshJob = this.refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            localRefreshJob.cancel(true);
            this.refreshJob = null;
        }

        this.devicesCache = null;
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        String childGuid = childThing.getProperties().get(THING_PROPERTIES_GUID);
        if (childGuid != null) {
            this.childHandlers.put(childGuid, childHandler);
            this.logger.debug("Added handler for thing {}", childThing.getProperties().get(THING_PROPERTIES_GUID));
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childThing.getProperties().get(THING_PROPERTIES_GUID) != null) {
            this.childHandlers.remove(childThing.getProperties().get(THING_PROPERTIES_GUID));
            this.logger.debug("Removed handler for thing {}", childThing.getProperties().get(THING_PROPERTIES_GUID));
        }
    }

    private synchronized void initRefresh(SolarwattConfiguration localConfig) {
        @Nullable
        ScheduledFuture<?> localRefreshJob = this.refreshJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            this.logger.info("Setting Energymanager refreshInterval to '{}' seconds", localConfig.refresh);
            this.refreshJob = this.scheduler.scheduleWithFixedDelay(this.refreshRunnable, 0, localConfig.refresh,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Fetch the map of devices from the cache.
     *
     * Used by all childs to get their values.
     *
     * @return map with all {@link Device}s
     */
    public @Nullable Map<String, Device> getDevices() {
        @Nullable
        ExpiringCache<Map<String, Device>> localDevicesCache = this.devicesCache;
        if (localDevicesCache != null) {
            @Nullable
            Map<String, Device> cache = localDevicesCache.getValue();
            if (cache != null) {
                this.updateStatus(ThingStatus.ONLINE);
            } else {
                this.updateStatus(ThingStatus.OFFLINE);
            }

            return cache;
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Convert the energy manager millisecond timestamps to {@link ZonedDateTime}
     *
     * The energy manager is the only point that knows about the timezone and
     * it is available to all other devices. All timestamps used by all devices
     * are in milliseconds since the epoch.
     *
     * @param timestamp milliseconds since the epoch
     * @return date time in timezone
     */
    public ZonedDateTime getFromMilliTimestamp(BigDecimal timestamp) {
        @Nullable
        Map<String, Device> devices = this.getDevices();
        if (devices != null) {
            @Nullable
            EnergyManager energyManager = (EnergyManager) devices.get(this.energyManagerGuid);
            if (energyManager != null) {

                BigDecimal[] bigDecimals = timestamp.divideAndRemainder(BigDecimal.valueOf(1_000));
                Instant instant = Instant.ofEpochSecond(bigDecimals[0].longValue(),
                        bigDecimals[1].multiply(BigDecimal.valueOf(1_000_000)).longValue());

                @Nullable
                ZoneId zoneId = energyManager.getZoneId();
                if (zoneId != null) {
                    return ZonedDateTime.ofInstant(instant, zoneId);
                }
            }
        }

        throw new DateTimeException("Timezone from energy manager missing.");
    }

    /**
     * Reload all devices from the energy manager.
     *
     * This method is called via the {@link ExpiringCache}.
     * 
     * @return map from guid to {@link Device}}
     */
    private @Nullable Map<String, Device> refreshDevices() {
        this.logger.trace("{} refreshdevices", this);

        try {
            final Map<String, Device> devicesData = this.connector.retrieveDevices().getDevices();
            this.updateStatus(ThingStatus.ONLINE);

            // trigger refresh of the available channels
            if (devicesData.containsKey(this.energyManagerGuid)) {
                Device device = devicesData.get(this.energyManagerGuid);
                if (device != null) {
                    this.initDeviceChannels(device);
                }
            } else {
                this.logger.warn("{}: initDeviceChannels missing energy manager {}", this, this.getThing().getUID());
            }

            return devicesData;
        } catch (final SolarwattConnectionException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return null;
    }

    /**
     * Trigger an update on all child things of this bridge.
     *
     * The available child handlers are traced via childHandlerInitialized and childHandlerDisposed.
     */
    private void updateAllChildThings() {
        this.logger.trace("{} updateAllChildThings", this);

        this.getThing().getThings().forEach(childThing -> {
            try {
                ThingHandler childHandler = this.childHandlers
                        .get(childThing.getProperties().get(THING_PROPERTIES_GUID));
                if (childHandler != null) {
                    childHandler.handleCommand(new ChannelUID(childThing.getUID(), THING_PROPERTIES_GUID),
                            RefreshType.REFRESH);
                } else {
                    this.logger.warn("no handler found for thing/device {}",
                            childThing.getProperties().get(THING_PROPERTIES_GUID));
                }
            } catch (Exception ex) {
                this.logger.error("Error processing child with uid {}", childThing.getUID(), ex);
            }
        });
    }
}
