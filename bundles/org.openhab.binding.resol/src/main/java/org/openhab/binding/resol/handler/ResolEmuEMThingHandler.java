/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.resol.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.resol.internal.ResolEmuEMConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.Packet;
import de.resol.vbus.deviceemulators.EmDeviceEmulator;

/**
 * The {@link ResolEmuEMThingHandler} is responsible for emulating a EM device
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolEmuEMThingHandler extends BaseThingHandler implements PropertyChangeListener {
    public static final String CHANNEL_RELAY = "relay";
    public static final String CHANNEL_TEMP = "temperature";
    public static final String CHANNEL_RESIST = "resitor";

    private final Logger logger = LoggerFactory.getLogger(ResolEmuEMThingHandler.class);

    private int vbusAddress = 0x6650;

    private int deviceId = 1;

    @Nullable
    ResolBridgeHandler bridgeHandler;

    @Nullable
    private EmDeviceEmulator device;

    // Background Runables
    @Nullable
    private ScheduledFuture<?> updateJob;

    Pattern relayPattern = Pattern.compile("Pump speed relay (\\d*).1");

    public ResolEmuEMThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ResolEmuEMConfiguration configuration = getConfigAs(ResolEmuEMConfiguration.class);
        deviceId = configuration.deviceId;
        vbusAddress = 0x6650 + deviceId;

        bridgeHandler = getBridgeHandler();
        registerResolThingListener(bridgeHandler);
    }

    @Override
    public void dispose() {
        EmDeviceEmulator dev = device;
        if (dev != null) {
            dev.stop();
            dev.removePropertyChangeListener(this);
        }
        unregisterResolThingListener(bridgeHandler);
    }

    private Runnable updateRunnable = new Runnable() {

        private long lastTime = System.currentTimeMillis();

        @Override
        public void run() {
            EmDeviceEmulator d = device;
            if (d != null) {
                long now = System.currentTimeMillis();
                int diff = (int) (now - lastTime);
                lastTime = now;

                d.update(diff);
            }
        }
    };

    private void startAutomaticUpdate() {
        ScheduledFuture<?> job = updateJob;
        if (job == null || job.isCancelled()) {
            updateJob = scheduler.scheduleWithFixedDelay(updateRunnable, 0, 1, TimeUnit.SECONDS);
        }
    }

    private synchronized @Nullable ResolBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Required bridge not defined for thing {}.", thing.getThingTypeUID());
            return null;
        } else {
            return getBridgeHandler(bridge);
        }
    }

    private synchronized @Nullable ResolBridgeHandler getBridgeHandler(Bridge bridge) {
        ResolBridgeHandler bridgeHandler = null;

        ThingHandler handler = bridge.getHandler();
        if (handler instanceof ResolBridgeHandler) {
            bridgeHandler = (ResolBridgeHandler) handler;
        } else {
            logger.debug("No available bridge handler found yet. Bridge: {} .", bridge.getUID());
        }
        return bridgeHandler;
    }

    private void registerResolThingListener(@Nullable ResolBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.registerResolThingListener(this);
        } else {
            logger.debug("Can't register {} at bridge as bridgeHandler is null.", this.getThing().getUID());
        }
    }

    private void unregisterResolThingListener(@Nullable ResolBridgeHandler bridgeHandler) {
        if (bridgeHandler != null) {
            bridgeHandler.unregisterThingListener(this);
        } else {
            logger.debug("Can't unregister {} at bridge as bridgeHandler is null.", this.getThing().getUID());
        }
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Nullable
    EmDeviceEmulator getDevice() {
        return device;
    }

    public int getVbusAddress() {
        return vbusAddress;
    }

    public void useConnection(Connection connection) {
        EmDeviceEmulator device = this.device;
        if (device != null) {
            device.stop();
            device.removePropertyChangeListener(this);
        }
        device = new EmDeviceEmulator(connection, deviceId);
        this.device = device;
        device.addPropertyChangeListener(this);
        device.start();
        for (int i = 1; i <= 5; i++) {
            setRelayChannelValue(i, device.getRelayValueByNr(i));
        }
        startAutomaticUpdate();
    }

    public void stop() {
        if (device != null) {
            device.stop();
        }
        if (updateJob != null) {
            updateJob.cancel(false);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        String chID = channelUID.getId();
        boolean update = false;
        int channel = chID.charAt(chID.length() - 1) - '0';
        float value = 0;

        if (command instanceof QuantityType<?>) {
            value = ((QuantityType<?>) command).floatValue();
            update = true;
        } else if (command instanceof DecimalType) {
            value = ((DecimalType) command).floatValue();
            update = true;
        }

        if (update) {
            EmDeviceEmulator dev = device;
            if (dev != null) {
                if (chID.startsWith(CHANNEL_TEMP)) {
                    dev.setResistorValueByNrAndPt1000Temperatur(channel, value);
                    updateState(channelUID, new DecimalType(value));
                } else {
                    dev.setResistorValueByNr(channel, (int) (value * 1000.0));
                    updateState(channelUID, new QuantityType<>(value, Units.OHM));
                }
            }
        }
    }

    @Override
    public void propertyChange(@Nullable PropertyChangeEvent evt) {
        if (evt != null) {
            String s = evt.getPropertyName();
            if (s.startsWith("relay") && s.endsWith("Value")) {
                int v = (Integer) evt.getNewValue();
                int i = Integer.parseInt(s.substring(5, 6));
                setRelayChannelValue(i, v);
            }
        }
    }

    public void handle(Packet packet) {
        updateStatus(ThingStatus.ONLINE);
    }

    private void setRelayChannelValue(int relay, double value) {
        String channelId = CHANNEL_RELAY + relay;
        updateState(channelId, new DecimalType(value));
    }
}
