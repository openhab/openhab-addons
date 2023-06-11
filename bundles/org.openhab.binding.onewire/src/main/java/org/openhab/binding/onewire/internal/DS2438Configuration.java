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
package org.openhab.binding.onewire.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;

/**
 * The {@link DS2438Configuration} is a helper class for the multisensor thing configuration
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class DS2438Configuration {
    private OwSensorType sensorSubType = OwSensorType.DS2438;
    private String vendor = "Dallas/Maxim";
    private String hwRevision = "0";
    private String prodDate = "unknown";

    private final Map<SensorId, OwSensorType> associatedSensors = new HashMap<>();

    public DS2438Configuration(OwserverBridgeHandler bridgeHandler, SensorId sensorId) throws OwException {
        OwSensorType sensorType = bridgeHandler.getType(sensorId);
        if (sensorType != OwSensorType.DS2438) {
            throw new OwException("sensor " + sensorId.getId() + " is not a DS2438!");
        }
        OwPageBuffer pageBuffer = bridgeHandler.readPages(sensorId);

        String sensorTypeId = pageBuffer.getPageString(3).substring(0, 2);
        switch (sensorTypeId) {
            case "19":
                vendor = "iButtonLink";
                sensorSubType = OwSensorType.MS_TH;
                break;
            case "1A":
                vendor = "iButtonLink";
                sensorSubType = OwSensorType.MS_TV;
                break;
            case "1B":
                vendor = "iButtonLink";
                sensorSubType = OwSensorType.MS_TL;
                break;
            case "1C":
                vendor = "iButtonLink";
                sensorSubType = OwSensorType.MS_TC;
                break;
            case "F1":
            case "F3":
                vendor = "Elaborated Networks";
                sensorSubType = OwSensorType.MS_TH;
                break;
            case "F2":
                vendor = "Elaborated Networks";
                sensorSubType = OwSensorType.MS_TH_S;
                break;
            case "F4":
                vendor = "Elaborated Networks";
                sensorSubType = OwSensorType.MS_TV;
                break;
            default:
        }

        if (sensorSubType == OwSensorType.MS_TH || sensorSubType == OwSensorType.MS_TH_S
                || sensorSubType == OwSensorType.MS_TV) {
            for (int i = 4; i < 7; i++) {
                String str = new StringBuilder(pageBuffer.getPageString(i)).insert(2, ".").delete(15, 17).toString();
                Matcher matcher = SensorId.SENSOR_ID_PATTERN.matcher(str);
                if (matcher.matches()) {
                    SensorId associatedSensorId = new SensorId(sensorId.getPath() + matcher.group(2));

                    switch (matcher.group(2).substring(0, 2)) {
                        case "26":
                            DS2438Configuration associatedDs2438Config = new DS2438Configuration(bridgeHandler,
                                    associatedSensorId);
                            associatedSensors.put(associatedSensorId, associatedDs2438Config.getSensorSubType());
                            associatedSensors.putAll(associatedDs2438Config.getAssociatedSensors());
                            break;
                        case "28":
                            associatedSensors.put(associatedSensorId, OwSensorType.DS18B20);
                            break;
                        case "3A":
                            associatedSensors.put(associatedSensorId, OwSensorType.DS2413);
                            break;
                        default:
                    }
                }
            }
            prodDate = String.format("%d/%d", pageBuffer.getByte(5, 0),
                    256 * pageBuffer.getByte(5, 1) + pageBuffer.getByte(5, 2));
            hwRevision = String.valueOf(pageBuffer.getByte(5, 3));
        }
    }

    public Map<SensorId, OwSensorType> getAssociatedSensors() {
        return associatedSensors;
    }

    /**
     * get a list of sensor ids associated with this sensor
     *
     * @return a list of the sensor ids (if found), empty list otherwise
     */
    public List<SensorId> getAssociatedSensorIds() {
        return new ArrayList<>(associatedSensors.keySet());
    }

    /**
     * get all secondary sensor ids of a given type
     *
     * @param sensorType filter for sensors
     * @return a list of OwDiscoveryItems
     */
    public List<SensorId> getAssociatedSensorIds(OwSensorType sensorType) {
        return associatedSensors.entrySet().stream().filter(s -> s.getValue() == sensorType).map(s -> s.getKey())
                .collect(Collectors.toList());
    }

    /**
     * get a list of sensor types associated with this sensor
     *
     * @return a list of the sensor typess (if found), empty list otherwise
     */
    public List<OwSensorType> getAssociatedSensorTypes() {
        return new ArrayList<>(associatedSensors.values());
    }

    /**
     * get the number of associated sensors
     *
     * @return the number
     */
    public int getAssociatedSensorCount() {
        return associatedSensors.size();
    }

    /**
     * get hardware revision (available on some multisensors)
     *
     * @return hardware revision
     */
    public String getHardwareRevision() {
        return hwRevision;
    }

    /**
     * get production date (available on some multisensors)
     *
     * @return production date in ww/yy
     */
    public String getProductionDate() {
        return prodDate;
    }

    /**
     * get sensor type (without associated sensors)
     *
     * @return basic sensor type
     */
    public OwSensorType getSensorSubType() {
        return sensorSubType;
    }

    /**
     * get vendor name (if available)
     *
     * @return the vendor name
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * determine multisensor type
     *
     * @param mainsensorType the type of the main sensor
     * @param associatedSensorTypes a list of OwSensorTypes of all associated sensors
     * @return the multisensor type (if known)
     */
    public static OwSensorType getMultisensorType(OwSensorType mainsensorType,
            List<OwSensorType> associatedSensorTypes) {
        OwSensorType multisensorType = OwSensorType.UNKNOWN;
        switch (associatedSensorTypes.size()) {
            case 0:
                multisensorType = mainsensorType;
                break;
            case 1:
                if (mainsensorType == OwSensorType.MS_TH_S && associatedSensorTypes.contains(OwSensorType.DS18B20)) {
                    multisensorType = OwSensorType.BMS_S;
                } else if (mainsensorType == OwSensorType.MS_TH
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)) {
                    multisensorType = OwSensorType.BMS;
                }
                break;
            case 3:
                if (mainsensorType == OwSensorType.MS_TH_S && associatedSensorTypes.contains(OwSensorType.MS_TV)
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)
                        && associatedSensorTypes.contains(OwSensorType.DS2413)) {
                    // two DS2438 (first THS, second TV), DS18B20, DS2413
                    multisensorType = OwSensorType.AMS_S;
                } else if (mainsensorType == OwSensorType.MS_TH && associatedSensorTypes.contains(OwSensorType.MS_TV)
                        && associatedSensorTypes.contains(OwSensorType.DS18B20)
                        && associatedSensorTypes.contains(OwSensorType.DS2413)) {
                    // two DS2438 (first TH, second TV), DS18B20, DS2413
                    multisensorType = OwSensorType.AMS;
                }
                break;
            default:
        }

        return multisensorType;
    }
}
