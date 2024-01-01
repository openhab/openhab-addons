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
package org.openhab.binding.onewire.internal.discovery;

import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.DS2438Configuration;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.OwPageBuffer;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OwDiscoveryItem} class defines a discovery item for OneWire devices
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwDiscoveryItem {
    private final Logger logger = LoggerFactory.getLogger(OwDiscoveryItem.class);

    private final SensorId sensorId;
    private OwSensorType sensorType = OwSensorType.UNKNOWN;
    private String vendor = "Dallas/Maxim";

    private final Map<SensorId, OwSensorType> associatedSensors = new HashMap<>();

    public OwDiscoveryItem(OwserverBridgeHandler bridgeHandler, SensorId sensorId) throws OwException {
        this.sensorId = sensorId;
        sensorType = bridgeHandler.getType(sensorId);
        switch (sensorType) {
            case DS2438 -> {
                bridgeHandler.readPages(sensorId);
                DS2438Configuration config = new DS2438Configuration(bridgeHandler, sensorId);
                associatedSensors.putAll(config.getAssociatedSensors());
                logger.trace("found associated sensors: {}", associatedSensors);
                vendor = config.getVendor();
                sensorType = config.getSensorSubType();
            }
            case EDS -> {
                vendor = "Embedded Data Systems";
                OwPageBuffer pages = bridgeHandler.readPages(sensorId);
                try { // determine subsensorType
                    sensorType = OwSensorType.valueOf(new String(pages.getPage(0), 0, 7, StandardCharsets.US_ASCII));
                } catch (IllegalArgumentException e) {
                    sensorType = OwSensorType.UNKNOWN;
                }
            }
            default -> {
            }
        }
    }

    /**
     * get sensor type
     *
     * @return sensor type
     */
    public OwSensorType getSensorType() {
        return sensorType;
    }

    /**
     * get this sensor id
     *
     * @return sensor id
     */
    public SensorId getSensorId() {
        return sensorId;
    }

    /**
     * normalized sensor id (for naming the discovery result)
     *
     * @return sensor id in format familyId_xxxxxxxxxx
     */
    public String getNormalizedSensorId() {
        return sensorId.getId().replace(".", "_");
    }

    /**
     * get vendor name (if available)
     *
     * @return vendor name
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * get this sensors ThingTypeUID
     *
     * @return ThingTypeUID if mapping successful
     */
    public ThingTypeUID getThingTypeUID() throws OwException {
        ThingTypeUID thingTypeUID = THING_TYPE_MAP.get(sensorType);
        if (thingTypeUID != null) {
            return thingTypeUID;
        } else {
            throw new OwException(sensorType + " cannot be mapped to thing type");
        }
    }

    /**
     * get a list of all sensors associated to this sensor
     *
     * @return list of strings
     */
    public List<SensorId> getAssociatedSensorIds() {
        return new ArrayList<>(associatedSensors.keySet());
    }

    /**
     * determine this sensors type
     */
    public void checkSensorType() {
        logger.debug("checkSensorType: {} with {}", this, associatedSensors);

        switch (sensorType) {
            case MS_TH, MS_TH_S -> sensorType = DS2438Configuration.getMultisensorType(sensorType,
                    new ArrayList<>(associatedSensors.values()));
            default -> {
            }
        }
    }

    /**
     * get Label {@code "<thingtype> (<id>)"}
     *
     * @return the thing label
     */
    public String getLabel() {
        return THING_LABEL_MAP.get(sensorType) + " (" + this.sensorId.getId() + ")";
    }

    @Override
    public String toString() {
        return String.format("%s/%s (associated: %d)", sensorId, sensorType, associatedSensors.size());
    }
}
