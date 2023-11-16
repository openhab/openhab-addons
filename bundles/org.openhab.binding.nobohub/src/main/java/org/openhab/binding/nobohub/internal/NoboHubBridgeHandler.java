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
package org.openhab.binding.nobohub.internal;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.CHANNEL_HUB_ACTIVE_OVERRIDE_NAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_HOSTNAME;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_PRODUCTION_DATE;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.PROPERTY_SOFTWARE_VERSION;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.RECOMMENDED_KEEPALIVE_INTERVAL;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.connection.HubCommunicationThread;
import org.openhab.binding.nobohub.internal.connection.HubConnection;
import org.openhab.binding.nobohub.internal.discovery.NoboThingDiscoveryService;
import org.openhab.binding.nobohub.internal.model.Component;
import org.openhab.binding.nobohub.internal.model.ComponentRegister;
import org.openhab.binding.nobohub.internal.model.Hub;
import org.openhab.binding.nobohub.internal.model.NoboCommunicationException;
import org.openhab.binding.nobohub.internal.model.NoboDataException;
import org.openhab.binding.nobohub.internal.model.OverrideMode;
import org.openhab.binding.nobohub.internal.model.OverridePlan;
import org.openhab.binding.nobohub.internal.model.OverrideRegister;
import org.openhab.binding.nobohub.internal.model.SerialNumber;
import org.openhab.binding.nobohub.internal.model.Temperature;
import org.openhab.binding.nobohub.internal.model.WeekProfile;
import org.openhab.binding.nobohub.internal.model.WeekProfileRegister;
import org.openhab.binding.nobohub.internal.model.Zone;
import org.openhab.binding.nobohub.internal.model.ZoneRegister;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NoboHubBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class NoboHubBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(NoboHubBridgeHandler.class);
    private @Nullable HubCommunicationThread hubThread;
    private @Nullable NoboThingDiscoveryService discoveryService;
    private @Nullable Hub hub;

    private final OverrideRegister overrideRegister = new OverrideRegister();
    private final WeekProfileRegister weekProfileRegister = new WeekProfileRegister();
    private final ZoneRegister zoneRegister = new ZoneRegister();
    private final ComponentRegister componentRegister = new ComponentRegister();

    public NoboHubBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Handle command {} for channel {}!", command.toFullString(), channelUID);

        HubCommunicationThread ht = this.hubThread;
        Hub h = this.hub;
        if (command instanceof RefreshType) {
            try {
                if (ht != null) {
                    ht.getConnection().refreshAll();
                }
            } catch (NoboCommunicationException noboEx) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/message.bridge.status.failed [\"" + noboEx.getMessage() + "\"]");
            }

            return;
        }

        if (CHANNEL_HUB_ACTIVE_OVERRIDE_NAME.equals(channelUID.getId())) {
            if (ht != null && h != null) {
                if (command instanceof StringType stringCommand) {
                    logger.debug("Changing override for hub {} to {}", channelUID, stringCommand);
                    try {
                        OverrideMode mode = OverrideMode.getByName(stringCommand.toFullString());
                        ht.getConnection().setOverride(h, mode);
                    } catch (NoboCommunicationException nce) {
                        logger.debug("Failed setting override mode", nce);
                    } catch (NoboDataException nde) {
                        logger.debug("Date format error setting override mode", nde);
                    }
                } else {
                    logger.debug("Command of wrong type: {} ({})", command, command.getClass().getName());
                }
            } else {
                if (null == h) {
                    logger.debug("Could not set override, hub not detected yet");
                }

                if (null == ht) {
                    logger.debug("Could not set override, hub connection thread not set up yet");
                }
            }
        }
    }

    @Override
    public void initialize() {
        NoboHubBridgeConfiguration config = getConfigAs(NoboHubBridgeConfiguration.class);

        String serialNumber = config.serialNumber;
        if (null == serialNumber) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/message.missing.serial");
            return;
        }

        String hostName = config.hostName;
        if (null == hostName || hostName.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/message.bridge.missing.hostname");
            return;
        }

        logger.debug("Looking for Hub {} at {}", config.serialNumber, config.hostName);

        // Set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);

        // Background handshake:
        scheduler.execute(() -> {
            try {
                HubConnection conn = new HubConnection(hostName, serialNumber, this);
                conn.connect();

                logger.debug("Done connecting to {} ({})", hostName, serialNumber);

                Duration timeout = RECOMMENDED_KEEPALIVE_INTERVAL;
                if (config.pollingInterval > 0) {
                    timeout = Duration.ofSeconds(config.pollingInterval);
                }

                logger.debug("Starting communication thread to {}", hostName);

                HubCommunicationThread ht = new HubCommunicationThread(conn, this, timeout);
                ht.start();
                hubThread = ht;

                if (ht.getConnection().isConnected()) {
                    logger.debug("Communication thread to {} is up and running, we are online", hostName);
                    updateProperty(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    logger.debug("HubCommunicationThread is not connected anymore, setting to OFFLINE");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/message.bridge.connection.failed");
                }
            } catch (NoboCommunicationException commEx) {
                logger.debug("HubCommunicationThread failed, exiting thread");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, commEx.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Disposing NoboHub '{}'", getThing().getUID().getId());

        final NoboThingDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.stopScan();
        }

        HubCommunicationThread ht = this.hubThread;
        if (ht != null) {
            logger.debug("Stopping communication thread");
            ht.stopNow();
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler handler, Thing thing) {
        logger.info("Adding thing: {}", thing.getLabel());
    }

    @Override
    public void childHandlerDisposed(ThingHandler handler, Thing thing) {
        logger.info("Disposing thing: {}", thing.getLabel());
    }

    private void onUpdate(Hub hub) {
        logger.debug("Updating Hub: {}", hub.getName());
        this.hub = hub;
        OverridePlan activeOverridePlan = getOverride(hub.getActiveOverrideId());

        if (null != activeOverridePlan) {
            logger.debug("Updating Hub with ActiveOverrideId {} with Name {}", activeOverridePlan.getId(),
                    activeOverridePlan.getMode().name());

            updateState(NoboHubBindingConstants.CHANNEL_HUB_ACTIVE_OVERRIDE_NAME,
                    StringType.valueOf(activeOverridePlan.getMode().name()));
        }

        // Update all zones to set online status and update profile name from weekProfileRegister
        for (Zone zone : zoneRegister.values()) {
            refreshZone(zone);
        }

        Map<String, String> properties = editProperties();
        properties.put(PROPERTY_HOSTNAME, hub.getName());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, hub.getSerialNumber().toString());
        properties.put(PROPERTY_SOFTWARE_VERSION, hub.getSoftwareVersion());
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, hub.getHardwareVersion());
        properties.put(PROPERTY_PRODUCTION_DATE, hub.getProductionDate());
        updateProperties(properties);
    }

    public void receivedData(@Nullable String line) {
        try {
            parseLine(line);
        } catch (NoboDataException nde) {
            logger.debug("Failed parsing line '{}': {}", line, nde.getMessage());
        }
    }

    private void parseLine(@Nullable String line) throws NoboDataException {
        if (null == line) {
            return;
        }

        NoboThingDiscoveryService ds = this.discoveryService;
        if (line.startsWith("H01")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.put(zone);
            if (null != ds) {
                ds.detectZones(zoneRegister.values());
            }
        } else if (line.startsWith("H02")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component);
            if (null != ds) {
                ds.detectComponents(componentRegister.values());
            }
        } else if (line.startsWith("H03")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile);
        } else if (line.startsWith("H04")) {
            OverridePlan overridePlan = OverridePlan.fromH04(line);
            overrideRegister.put(overridePlan);
        } else if (line.startsWith("H05")) {
            Hub hub = Hub.fromH05(line);
            onUpdate(hub);
        } else if (line.startsWith("S00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.remove(zone.getId());
        } else if (line.startsWith("S01")) {
            Component component = Component.fromH02(line);
            componentRegister.remove(component.getSerialNumber());
        } else if (line.startsWith("S02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.remove(weekProfile.getId());
        } else if (line.startsWith("S03")) {
            OverridePlan overridePlan = OverridePlan.fromH04(line);
            overrideRegister.remove(overridePlan.getId());
        } else if (line.startsWith("B00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.put(zone);
            if (null != ds) {
                ds.detectZones(zoneRegister.values());
            }
        } else if (line.startsWith("B01")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component);
            if (null != ds) {
                ds.detectComponents(componentRegister.values());
            }
        } else if (line.startsWith("B02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile);
        } else if (line.startsWith("B03")) {
            OverridePlan overridePlan = OverridePlan.fromH04(line);
            overrideRegister.put(overridePlan);
        } else if (line.startsWith("V00")) {
            Zone zone = Zone.fromH01(line);
            zoneRegister.put(zone);
            refreshZone(zone);
        } else if (line.startsWith("V01")) {
            Component component = Component.fromH02(line);
            componentRegister.put(component);
            refreshComponent(component);
        } else if (line.startsWith("V02")) {
            WeekProfile weekProfile = WeekProfile.fromH03(line);
            weekProfileRegister.put(weekProfile);
        } else if (line.startsWith("V03")) {
            Hub hub = Hub.fromH05(line);
            onUpdate(hub);
        } else if (line.startsWith("Y02")) {
            Temperature temp = Temperature.fromY02(line);
            Component component = getComponent(temp.getSerialNumber());
            if (null != component) {
                component.setTemperature(temp.getTemperature());
                refreshComponent(component);
                int zoneId = component.getTemperatureSensorForZoneId();
                if (zoneId >= 0) {
                    Zone zone = getZone(zoneId);
                    if (null != zone) {
                        zone.setTemperature(temp.getTemperature());
                        refreshZone(zone);
                    }
                }
            }
        } else if (line.startsWith("E00")) {
            logger.debug("Error from Hub: {}", line);
        } else {
            // HANDSHAKE: Basic part of keepalive
            // V06: Encryption key
            // H00: contains no information
            if (!line.startsWith("HANDSHAKE") && !line.startsWith("V06") && !line.startsWith("H00")) {
                logger.info("Unknown information from Hub: '{}}'", line);
            }
        }
    }

    public @Nullable Zone getZone(Integer id) {
        return zoneRegister.get(id);
    }

    public @Nullable WeekProfile getWeekProfile(Integer id) {
        return weekProfileRegister.get(id);
    }

    public @Nullable Component getComponent(SerialNumber serialNumber) {
        return componentRegister.get(serialNumber);
    }

    public @Nullable OverridePlan getOverride(Integer id) {
        return overrideRegister.get(id);
    }

    public void sendCommand(String command) {
        @Nullable
        HubCommunicationThread ht = this.hubThread;
        if (ht != null) {
            HubConnection conn = ht.getConnection();
            conn.sendCommand(command);
        }
    }

    private void refreshZone(Zone zone) {
        this.getThing().getThings().forEach(thing -> {
            if (thing.getHandler() instanceof ZoneHandler) {
                ZoneHandler handler = (ZoneHandler) thing.getHandler();
                if (handler != null && handler.getZoneId() == zone.getId()) {
                    handler.onUpdate(zone);
                }
            }
        });
    }

    private void refreshComponent(Component component) {
        this.getThing().getThings().forEach(thing -> {
            if (thing.getHandler() instanceof ComponentHandler) {
                ComponentHandler handler = (ComponentHandler) thing.getHandler();
                if (handler != null) {
                    SerialNumber handlerSerial = handler.getSerialNumber();
                    if (handlerSerial != null && component.getSerialNumber().equals(handlerSerial)) {
                        handler.onUpdate(component);
                    }
                }
            }
        });
    }

    public void startScan() {
        try {
            @Nullable
            HubCommunicationThread ht = this.hubThread;
            if (ht != null) {
                ht.getConnection().refreshAll();
            }
        } catch (NoboCommunicationException noboEx) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/message.bridge.status.failed [\"" + noboEx.getMessage() + "\"]");
        }
    }

    public void setDicsoveryService(NoboThingDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public Collection<WeekProfile> getWeekProfiles() {
        return weekProfileRegister.values();
    }

    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        updateStatus(status, statusDetail, description);
    }
}
