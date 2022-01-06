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
package org.openhab.binding.pulseaudio.internal.handler;

import static org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.pulseaudio.internal.PulseAudioBindingConfiguration;
import org.openhab.binding.pulseaudio.internal.PulseAudioBindingConfigurationListener;
import org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.internal.PulseaudioClient;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PulseaudioBridgeHandler} is the handler for a Pulseaudio server and
 * connects it to the framework.
 *
 * @author Tobias Bräutigam - Initial contribution
 *
 */
public class PulseaudioBridgeHandler extends BaseBridgeHandler implements PulseAudioBindingConfigurationListener {
    private final Logger logger = LoggerFactory.getLogger(PulseaudioBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(PulseaudioBindingConstants.BRIDGE_THING_TYPE);

    public String host = "localhost";
    public int port = 4712;

    public int refreshInterval = 30000;

    private PulseaudioClient client;

    private PulseAudioBindingConfiguration configuration;

    private List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private HashSet<String> lastActiveDevices = new HashSet<>();

    private ScheduledFuture<?> pollingJob;
    private Runnable pollingRunnable = () -> {
        update();
    };

    private synchronized void update() {
        client.update();
        for (AbstractAudioDeviceConfig device : client.getItems()) {
            if (lastActiveDevices != null && lastActiveDevices.contains(device.getPaName())) {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    try {
                        deviceStatusListener.onDeviceStateChanged(getThing().getUID(), device);
                    } catch (Exception e) {
                        logger.error("An exception occurred while calling the DeviceStatusListener", e);
                    }
                }
            } else {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    try {
                        deviceStatusListener.onDeviceAdded(getThing(), device);
                        deviceStatusListener.onDeviceStateChanged(getThing().getUID(), device);
                    } catch (Exception e) {
                        logger.error("An exception occurred while calling the DeviceStatusListener", e);
                    }
                    lastActiveDevices.add(device.getPaName());
                }
            }
        }
    }

    public PulseaudioBridgeHandler(Bridge bridge, PulseAudioBindingConfiguration configuration) {
        super(bridge);
        this.configuration = configuration;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            client.update();
        } else {
            logger.warn("received invalid command for pulseaudio bridge '{}'.", host);
        }
    }

    private synchronized void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.MILLISECONDS);
        }
    }

    public AbstractAudioDeviceConfig getDevice(String name) {
        return client.getGenericAudioItem(name);
    }

    public PulseaudioClient getClient() {
        return client;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Pulseaudio handler.");
        Configuration conf = this.getConfig();

        if (conf.get(BRIDGE_PARAMETER_HOST) != null) {
            this.host = String.valueOf(conf.get(BRIDGE_PARAMETER_HOST));
        }
        if (conf.get(BRIDGE_PARAMETER_PORT) != null) {
            this.port = ((BigDecimal) conf.get(BRIDGE_PARAMETER_PORT)).intValue();
        }
        if (conf.get(BRIDGE_PARAMETER_REFRESH_INTERVAL) != null) {
            this.refreshInterval = ((BigDecimal) conf.get(BRIDGE_PARAMETER_REFRESH_INTERVAL)).intValue();
        }

        if (host != null && !host.isEmpty()) {
            Runnable connectRunnable = () -> {
                try {
                    client = new PulseaudioClient(host, port, configuration);
                    if (client.isConnected()) {
                        updateStatus(ThingStatus.ONLINE);
                        logger.info("Established connection to Pulseaudio server on Host '{}':'{}'.", host, port);
                        startAutomaticRefresh();
                    }
                } catch (IOException e) {
                    logger.error("Couldn't connect to Pulsaudio server [Host '{}':'{}']: {}", host, port,
                            e.getLocalizedMessage());
                    updateStatus(ThingStatus.OFFLINE);
                }
            };
            scheduler.schedule(connectRunnable, 0, TimeUnit.SECONDS);
        } else {
            logger.warn(
                    "Couldn't connect to Pulseaudio server because of missing connection parameters [Host '{}':'{}'].",
                    host, port);
            updateStatus(ThingStatus.OFFLINE);
        }

        this.configuration.addPulseAudioBindingConfigurationListener(this);
    }

    @Override
    public void dispose() {
        this.configuration.removePulseAudioBindingConfigurationListener(this);
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        if (client != null) {
            client.disconnect();
        }
        super.dispose();
    }

    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        return deviceStatusListeners.add(deviceStatusListener);
    }

    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    @Override
    public void bindingConfigurationChanged() {
        update();
    }
}
