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
package org.openhab.binding.onewire.internal.device;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractOwClass} class defines an abstract onewire device
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractOwDevice {
    private final Logger logger = LoggerFactory.getLogger(AbstractOwDevice.class);

    protected SensorId sensorId;
    protected OwSensorType sensorType;
    protected OwBaseThingHandler callback;
    protected Boolean isConfigured = false;

    protected Set<String> enabledChannels = new HashSet<>();

    /**
     * constructor for the onewire device
     *
     * @param sensorId onewire ID of the sensor
     * @param callback ThingHandler callback for posting updates
     */
    public AbstractOwDevice(SensorId sensorId, OwBaseThingHandler callback) {
        this.sensorId = sensorId;
        this.callback = callback;
        this.sensorType = OwSensorType.UNKNOWN;
    }

    public AbstractOwDevice(SensorId sensorId, OwSensorType sensorType, OwBaseThingHandler callback) {
        this.sensorId = sensorId;
        this.callback = callback;
        this.sensorType = sensorType;
    }

    /**
     * configures the onewire devices channels
     *
     */
    public abstract void configureChannels() throws OwException;

    /**
     * refresh this sensor
     *
     * @param bridgeHandler for sending requests
     * @param forcedRefresh post update even if state did not change
     * @throws OwException in case of communication error
     */
    public abstract void refresh(OwserverBridgeHandler owBridgeHandler, Boolean forcedRefresh) throws OwException;

    /**
     * enables a channel on this device
     *
     * @param channelID the channels channelID
     */
    public void enableChannel(String channelID) {
        if (!enabledChannels.contains(channelID)) {
            enabledChannels.add(channelID);
        }
    }

    /**
     * disables a channel on this device
     *
     * @param channelID the channels channelID
     */
    public void disableChannel(String channelID) {
        if (enabledChannels.contains(channelID)) {
            enabledChannels.remove(channelID);
        }
    }

    /**
     * get onewire ID of this sensor
     *
     * @return sensor ID
     */
    public SensorId getSensorId() {
        return sensorId;
    }

    /**
     * check sensor presence and update thing state
     *
     * @param owServerConnection
     * @return sensors presence state
     */

    public Boolean checkPresence(OwserverBridgeHandler bridgeHandler) {
        try {
            State present = bridgeHandler.checkPresence(sensorId);
            callback.updatePresenceStatus(present);
            return OnOffType.ON.equals(present);
        } catch (OwException e) {
            logger.debug("error refreshing presence {} on bridge {}: {}", this.sensorId,
                    bridgeHandler.getThing().getUID(), e.getMessage());
            return false;
        }
    }

    /**
     * get this sensors type
     *
     * @param bridgeHandler bridge handler to request from if type formerly unknown
     * @return this sensors type
     * @throws OwException
     */
    public OwSensorType getSensorType(OwserverBridgeHandler bridgeHandler) throws OwException {
        if (sensorType == OwSensorType.UNKNOWN) {
            sensorType = bridgeHandler.getType(sensorId);
        }
        return sensorType;
    }
}
