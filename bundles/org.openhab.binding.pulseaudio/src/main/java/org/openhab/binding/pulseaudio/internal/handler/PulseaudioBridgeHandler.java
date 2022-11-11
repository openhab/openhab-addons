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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.PulseAudioBindingConfiguration;
import org.openhab.binding.pulseaudio.internal.PulseAudioBindingConfigurationListener;
import org.openhab.binding.pulseaudio.internal.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.internal.PulseaudioClient;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PulseaudioBridgeHandler} is the handler for a Pulseaudio server and
 * connects it to the framework.
 *
 * @author Tobias Bräutigam - Initial contribution
 * @author Gwendal Roulleau - Rewrite for child handler notification
 *
 */
@NonNullByDefault
public class PulseaudioBridgeHandler extends BaseBridgeHandler implements PulseAudioBindingConfigurationListener {
    private final Logger logger = LoggerFactory.getLogger(PulseaudioBridgeHandler.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(PulseaudioBindingConstants.BRIDGE_THING_TYPE);

    public String host = "localhost";
    public int port = 4712;

    public int refreshInterval = 30000;

    @Nullable
    private PulseaudioClient client;

    private PulseAudioBindingConfiguration configuration;

    private List<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArrayList<>();
    private Set<String> lastActiveDevices = new HashSet<>();

    @Nullable
    private ScheduledFuture<?> pollingJob;

    private Set<PulseaudioHandler> childHandlersInitialized = new HashSet<>();

    public synchronized void update() {
        try {
            getClient().connect();
        } catch (IOException e) {
            logger.debug("{}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Couldn't connect to Pulsaudio server [Host '%s':'%d']: %s", host, port,
                            e.getMessage() != null ? e.getMessage() : ""));
            return;
        }

        getClient().update();
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Established connection to Pulseaudio server on Host '{}':'{}'.", host, port);
            // The framework will automatically notify the child handlers as the bridge status is changed
        } else {
            // browse all child handlers to update status according to the result of the query to the pulse audio server
            for (PulseaudioHandler pulseaudioHandler : childHandlersInitialized) {
                pulseaudioHandler.deviceUpdate(getDevice(pulseaudioHandler.getDeviceIdentifier()));
            }
        }
        // browse query result to notify add event
        for (AbstractAudioDeviceConfig device : getClient().getItems()) {
            if (!lastActiveDevices.contains(device.getPaName())) {
                for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                    try {
                        deviceStatusListener.onDeviceAdded(getThing(), device);
                    } catch (Exception e) {
                        logger.warn("An exception occurred while calling the DeviceStatusListener", e);
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
            getClient().update();
        } else {
            logger.debug("received unexpected command for pulseaudio bridge '{}'.", host);
        }
    }

    public @Nullable AbstractAudioDeviceConfig getDevice(@Nullable DeviceIdentifier deviceIdentifier) {
        return deviceIdentifier == null ? null : getClient().getGenericAudioItem(deviceIdentifier);
    }

    public PulseaudioClient getClient() {
        PulseaudioClient clientFinal = client;
        if (clientFinal == null) {
            throw new AssertionError("PulseaudioClient is null !");
        }
        return clientFinal;
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

        if (!host.isBlank()) {
            client = new PulseaudioClient(host, port, configuration);
            updateStatus(ThingStatus.UNKNOWN);
            final ScheduledFuture<?> pollingJobFinal = pollingJob;
            if (pollingJobFinal == null || pollingJobFinal.isCancelled()) {
                pollingJob = scheduler.scheduleWithFixedDelay(this::update, 0, refreshInterval, TimeUnit.MILLISECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                    "Couldn't connect to Pulseaudio server because of missing connection parameters [Host '%s':'%d']",
                    host, port));
        }

        this.configuration.addPulseAudioBindingConfigurationListener(this);
    }

    @Override
    public void dispose() {
        this.configuration.removePulseAudioBindingConfigurationListener(this);
        ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        var clientFinal = client;
        if (clientFinal != null) {
            clientFinal.disconnect();
        }
        super.dispose();
    }

    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.add(deviceStatusListener);
    }

    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        return deviceStatusListeners.remove(deviceStatusListener);
    }

    @Override
    public void bindingConfigurationChanged() {
        // If the bridge thing is not well setup, we do nothing
        if (getThing().getStatus() != ThingStatus.OFFLINE
                || getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
            update();
        }
    }

    public void resetKnownActiveDevices() {
        // If the bridge thing is not well setup, we do nothing
        if (getThing().getStatus() != ThingStatus.OFFLINE
                || getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
            lastActiveDevices = new HashSet<>();
            update();
        }
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof PulseaudioHandler) {
            this.childHandlersInitialized.add((PulseaudioHandler) childHandler);
        } else {
            logger.error("This bridge can only support PulseaudioHandler child");
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        this.childHandlersInitialized.remove(childHandler);
    }
}
