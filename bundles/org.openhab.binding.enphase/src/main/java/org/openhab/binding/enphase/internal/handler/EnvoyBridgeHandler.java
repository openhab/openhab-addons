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
package org.openhab.binding.enphase.internal.handler;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.CONFIG_HOSTNAME;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.ENVOY_CHANNELGROUP_CONSUMPTION;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.ENVOY_WATTS_NOW;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.ENVOY_WATT_HOURS_LIFETIME;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.ENVOY_WATT_HOURS_SEVEN_DAYS;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.ENVOY_WATT_HOURS_TODAY;
import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.PROPERTY_VERSION;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.enphase.internal.EnphaseBindingConstants;
import org.openhab.binding.enphase.internal.EnvoyConfiguration;
import org.openhab.binding.enphase.internal.EnvoyHostAddressCache;
import org.openhab.binding.enphase.internal.discovery.EnphaseDevicesDiscoveryService;
import org.openhab.binding.enphase.internal.dto.EnvoyEnergyDTO;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO.DeviceDTO;
import org.openhab.binding.enphase.internal.dto.InverterDTO;
import org.openhab.binding.enphase.internal.exception.EnphaseException;
import org.openhab.binding.enphase.internal.exception.EntrezConnectionException;
import org.openhab.binding.enphase.internal.exception.EntrezJwtInvalidException;
import org.openhab.binding.enphase.internal.exception.EnvoyConnectionException;
import org.openhab.binding.enphase.internal.exception.EnvoyNoHostnameException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.util.ThingHandlerHelper;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BridgeHandler for the Envoy gateway.
 *
 * @author Thomas Hentschel - Initial contribution
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnvoyBridgeHandler extends BaseBridgeHandler {

    private enum FeatureStatus {
        UNKNOWN,
        SUPPORTED,
        UNSUPPORTED
    }

    private static final long RETRY_RECONNECT_SECONDS = 10;

    private final Logger logger = LoggerFactory.getLogger(EnvoyBridgeHandler.class);
    private final EnvoyHostAddressCache envoyHostnameCache;
    private final EnvoyConnectorWrapper connectorWrapper;

    private EnvoyConfiguration configuration = new EnvoyConfiguration();

    private @Nullable ScheduledFuture<?> updataDataFuture;
    private @Nullable ScheduledFuture<?> updateHostnameFuture;
    private @Nullable ExpiringCache<Map<String, @Nullable InverterDTO>> invertersCache;
    private @Nullable ExpiringCache<Map<String, @Nullable DeviceDTO>> devicesCache;
    private @Nullable EnvoyEnergyDTO productionDTO;
    private @Nullable EnvoyEnergyDTO consumptionDTO;
    private FeatureStatus consumptionSupported = FeatureStatus.UNKNOWN;
    private FeatureStatus jsonSupported = FeatureStatus.UNKNOWN;

    public EnvoyBridgeHandler(final Bridge thing, final HttpClient httpClient,
            final EnvoyHostAddressCache envoyHostAddressCache) {
        super(thing);
        this.envoyHostnameCache = envoyHostAddressCache;
        connectorWrapper = new EnvoyConnectorWrapper(httpClient);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            refresh(channelUID);
        }
    }

    private void refresh(final ChannelUID channelUID) {
        final EnvoyEnergyDTO data = ENVOY_CHANNELGROUP_CONSUMPTION.equals(channelUID.getGroupId()) ? consumptionDTO
                : productionDTO;

        if (data == null) {
            updateState(channelUID, UnDefType.UNDEF);
        } else {
            switch (channelUID.getIdWithoutGroup()) {
                case ENVOY_WATT_HOURS_TODAY:
                    updateState(channelUID, new QuantityType<>(data.wattHoursToday, Units.WATT_HOUR));
                    break;
                case ENVOY_WATT_HOURS_SEVEN_DAYS:
                    updateState(channelUID, new QuantityType<>(data.wattHoursSevenDays, Units.WATT_HOUR));
                    break;
                case ENVOY_WATT_HOURS_LIFETIME:
                    updateState(channelUID, new QuantityType<>(data.wattHoursLifetime, Units.WATT_HOUR));
                    break;
                case ENVOY_WATTS_NOW:
                    updateState(channelUID, new QuantityType<>(data.wattsNow, Units.WATT));
                    break;
            }
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EnphaseDevicesDiscoveryService.class);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(EnvoyConfiguration.class);
        if (!EnphaseBindingConstants.isValidSerial(configuration.serialNumber)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial number is not valid");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        consumptionSupported = FeatureStatus.UNKNOWN;
        jsonSupported = FeatureStatus.UNKNOWN;
        invertersCache = new ExpiringCache<>(Duration.of(configuration.refresh, ChronoUnit.MINUTES),
                this::refreshInverters);
        devicesCache = new ExpiringCache<>(Duration.of(configuration.refresh, ChronoUnit.MINUTES),
                this::refreshDevices);
        connectorWrapper.setVersion(getVersion());
        updataDataFuture = scheduler.scheduleWithFixedDelay(() -> updateData(false), 0, configuration.refresh,
                TimeUnit.MINUTES);
    }

    /**
     * Method called by the ExpiringCache when no inverter data is present to get the data from the Envoy gateway.
     * When there are connection problems it will start a scheduled job to try to reconnect to the
     *
     * @return the inverter data from the Envoy gateway or null if no data is available.
     */
    private @Nullable Map<String, @Nullable InverterDTO> refreshInverters() {
        try {
            return connectorWrapper.getConnector().getInverters().stream()
                    .collect(Collectors.toMap(InverterDTO::getSerialNumber, Function.identity()));
        } catch (final EnvoyNoHostnameException e) {
            // ignore hostname exception here. It's already handled by others.
        } catch (final EnvoyConnectionException e) {
            logger.trace("refreshInverters connection problem", e);
        } catch (final EnphaseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return null;
    }

    private @Nullable final Map<String, @Nullable DeviceDTO> refreshDevices() {
        try {
            if (jsonSupported != FeatureStatus.UNSUPPORTED) {
                final Map<String, @Nullable DeviceDTO> devicesData = connectorWrapper.getConnector().getInventoryJson()
                        .stream().flatMap(inv -> Stream.of(inv.devices).map(d -> {
                            d.type = inv.type;
                            return d;
                        })).collect(Collectors.toMap(DeviceDTO::getSerialNumber, Function.identity()));

                jsonSupported = FeatureStatus.SUPPORTED;
                return devicesData;
            }
        } catch (final EnvoyNoHostnameException e) {
            // ignore hostname exception here. It's already handled by others.
        } catch (final EnvoyConnectionException e) {
            if (jsonSupported == FeatureStatus.UNKNOWN) {
                logger.info(
                        "This Ephase Envoy device ({}) doesn't seem to support json data. So not all channels are set.",
                        getThing().getUID());
                jsonSupported = FeatureStatus.UNSUPPORTED;
            } else if (consumptionSupported == FeatureStatus.SUPPORTED) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } catch (final EnphaseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * Returns the data for the inverters. It get the data from cache or updates the cache if possible in case no data
     * is available.
     *
     * @param force force a cache refresh
     * @return data if present or null
     */
    public @Nullable Map<String, @Nullable InverterDTO> getInvertersData(final boolean force) {
        final ExpiringCache<Map<String, @Nullable InverterDTO>> invertersCache = this.invertersCache;

        if (invertersCache == null || !isOnline()) {
            return null;
        } else {
            if (force) {
                invertersCache.invalidateValue();
            }
            return invertersCache.getValue();
        }
    }

    /**
     * Returns the data for the devices. It get the data from cache or updates the cache if possible in case no data
     * is available.
     *
     * @param force force a cache refresh
     * @return data if present or null
     */
    public @Nullable Map<String, @Nullable DeviceDTO> getDevices(final boolean force) {
        final ExpiringCache<Map<String, @Nullable DeviceDTO>> devicesCache = this.devicesCache;

        if (devicesCache == null || !isOnline()) {
            return null;
        } else {
            if (force) {
                devicesCache.invalidateValue();
            }
            return devicesCache.getValue();
        }
    }

    /**
     * Method called by the refresh thread.
     */
    public synchronized void updateData(final boolean forceUpdate) {
        try {
            if (!ThingHandlerHelper.isHandlerInitialized(this)) {
                logger.debug("Not updating anything. Not initialized: {}", getThing().getStatus());
                return;
            }
            if (checkConnection()) {
                updateEnvoy();
                updateInverters(forceUpdate);
                updateDevices(forceUpdate);
            }
        } catch (final EnvoyNoHostnameException e) {
            scheduleHostnameUpdate(false);
        } catch (final EnvoyConnectionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduleHostnameUpdate(false);
        } catch (final EntrezConnectionException e) {
            logger.debug("EntrezConnectionException in Enphase thing {}: ", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (final EntrezJwtInvalidException e) {
            logger.debug("EntrezJwtInvalidException in Enphase thing {}: ", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (final EnphaseException e) {
            logger.debug("EnphaseException in Enphase thing {}: ", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (final RuntimeException e) {
            logger.debug("Unexpected error in Enphase thing {}: ", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Checks if there is an active connection. If the configuration isn't valid it will set the bridge offline and
     * return false.
     *
     * @return true if an active connection was found, else returns false
     * @throws EnphaseException
     */
    private boolean checkConnection() throws EnphaseException {
        logger.trace("Check connection");
        if (connectorWrapper.hasConnection()) {
            return true;
        }
        final String configurationError = connectorWrapper.setConnector(configuration);

        if (configurationError.isBlank()) {
            updateVersion();
            logger.trace("No configuration error");
            return true;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configurationError);
            return false;
        }
    }

    private @Nullable String getVersion() {
        return getThing().getProperties().get(PROPERTY_VERSION);
    }

    private void updateVersion() {
        if (getVersion() == null) {
            final String version = connectorWrapper.getVersion();

            if (version != null) {
                final BridgeBuilder builder = editThing();
                final Map<String, String> properties = new HashMap<>(thing.getProperties());

                properties.put(PROPERTY_VERSION, version);
                builder.withProperties(properties);
                updateThing(builder.build());
            }
        }
    }

    private void updateEnvoy() throws EnphaseException {
        productionDTO = connectorWrapper.getConnector().getProduction();
        setConsumptionDTOData();
        getThing().getChannels().stream().map(Channel::getUID).filter(this::isLinked).forEach(this::refresh);
        if (isInitialized() && !isOnline()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Retrieve consumption data if supported, and keep track if this feature is supported by the device.
     */
    private void setConsumptionDTOData() throws EnphaseException {
        if (consumptionSupported != FeatureStatus.UNSUPPORTED && isOnline()) {
            try {
                consumptionDTO = connectorWrapper.getConnector().getConsumption();
                consumptionSupported = FeatureStatus.SUPPORTED;
            } catch (final EnvoyNoHostnameException e) {
                // ignore hostname exception here. It's already handled by others.
            } catch (final EnvoyConnectionException e) {
                if (consumptionSupported == FeatureStatus.UNKNOWN) {
                    logger.info(
                            "This Enphase Envoy device ({}) doesn't seem to support consumption data. So no consumption channels are set.",
                            getThing().getUID());
                    consumptionSupported = FeatureStatus.UNSUPPORTED;
                } else if (consumptionSupported == FeatureStatus.SUPPORTED) {
                    throw e;
                }
            }
        }
    }

    /**
     * Updates channels of the inverter things with inverter specific data.
     *
     * @param forceUpdate if true forces to update the data, otherwise gets the data from the cache
     */
    private void updateInverters(final boolean forceUpdate) {
        final Map<String, @Nullable InverterDTO> inverters = getInvertersData(forceUpdate);

        if (inverters != null) {
            getThing().getThings().stream().map(Thing::getHandler).filter(h -> h instanceof EnphaseInverterHandler)
                    .map(EnphaseInverterHandler.class::cast)
                    .forEach(invHandler -> updateInverter(inverters, invHandler));
        }
    }

    private void updateInverter(final @Nullable Map<String, @Nullable InverterDTO> inverters,
            final EnphaseInverterHandler invHandler) {
        if (inverters == null) {
            return;
        }
        final InverterDTO inverterDTO = inverters.get(invHandler.getSerialNumber());

        invHandler.refreshInverterChannels(inverterDTO);
        if (jsonSupported == FeatureStatus.UNSUPPORTED) {
            // if inventory json is supported device status is set in #updateDevices
            invHandler.refreshDeviceStatus(inverterDTO != null);
        }
    }

    /**
     * Updates channels of the device things with device specific data.
     * This data is not available on all envoy devices.
     *
     * @param forceUpdate if true forces to update the data, otherwise gets the data from the cache
     */
    private void updateDevices(final boolean forceUpdate) {
        final Map<String, @Nullable DeviceDTO> devices = getDevices(forceUpdate);

        getThing().getThings().stream().map(Thing::getHandler).filter(h -> h instanceof EnphaseDeviceHandler)
                .map(EnphaseDeviceHandler.class::cast).forEach(invHandler -> invHandler
                        .refreshDeviceState(devices == null ? null : devices.get(invHandler.getSerialNumber())));
    }

    /**
     * Schedules a hostname update, but only schedules the task when not yet running or forced.
     * Force is used to reschedule the task and should only be used from within {@link #updateHostname()}.
     *
     * @param force if true will always schedule the task
     */
    private synchronized void scheduleHostnameUpdate(final boolean force) {
        if (force || updateHostnameFuture == null) {
            logger.debug("Schedule hostname/ip address update for thing {} in {} seconds.", getThing().getUID(),
                    RETRY_RECONNECT_SECONDS);
            updateHostnameFuture = scheduler.schedule(this::updateHostname, RETRY_RECONNECT_SECONDS, TimeUnit.SECONDS);
        }
    }

    @Override
    public void childHandlerInitialized(final ThingHandler childHandler, final Thing childThing) {
        if (childHandler instanceof EnphaseInverterHandler handler) {
            updateInverter(getInvertersData(false), handler);
        }
        if (childHandler instanceof EnphaseDeviceHandler handler) {
            final Map<String, @Nullable DeviceDTO> devices = getDevices(false);

            if (devices != null) {
                handler.refreshDeviceState(devices.get(handler.getSerialNumber()));
            }
        }
    }

    /**
     * Handles a host name / ip address update.
     */
    private void updateHostname() {
        final String lastKnownHostname = envoyHostnameCache.getLastKnownHostAddress(configuration.serialNumber);

        if (lastKnownHostname.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No ip address known of the Envoy gateway. If this isn't updated in a few minutes run discovery scan or check your connection.");
            scheduleHostnameUpdate(true);
        } else {
            updateConfigurationOnHostnameUpdate(lastKnownHostname);
            updateData(true);
        }
    }

    private void updateConfigurationOnHostnameUpdate(final String lastKnownHostname) {
        final Configuration config = editConfiguration();

        config.put(CONFIG_HOSTNAME, lastKnownHostname);
        logger.info("Enphase Envoy ({}) hostname/ip address set to {}", getThing().getUID(), lastKnownHostname);
        configuration.hostname = lastKnownHostname;
        updateConfiguration(config);
        // The task is done so the future can be released by setting it to null.
        updateHostnameFuture = null;
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> retryFuture = this.updateHostnameFuture;
        if (retryFuture != null) {
            retryFuture.cancel(true);
        }
        final ScheduledFuture<?> inverterFuture = this.updataDataFuture;

        if (inverterFuture != null) {
            inverterFuture.cancel(true);
        }
    }

    /**
     * @return Returns true if the bridge is online and not has a configuration pending.
     */
    public boolean isOnline() {
        return getThing().getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public String toString() {
        return "EnvoyBridgeHandler(" + thing.getUID() + ") Status: " + thing.getStatus();
    }
}
