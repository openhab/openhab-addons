/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents the definition of capabilities for an LG ThinQ device.
 * <p>
 * This interface provides methods to retrieve and configure various
 * device capabilities, including monitoring formats, protocols, and feature definitions.
 * It serves as a contract for handling different LG ThinQ device capabilities.
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface CapabilityDefinition {

    /**
     * Checks if the device supports the "before command" capability.
     *
     * @return {@code true} if supported, {@code false} otherwise.
     */
    boolean isBeforeCommandSupported();

    /**
     * Sets whether the device supports the "before command" capability.
     *
     * @param supports {@code true} to enable support, {@code false} to disable.
     */
    void setBeforeCommandSupported(boolean supports);

    /**
     * Retrieves the model name of the device.
     *
     * @return The model name as a {@link String}.
     */
    String getModelName();

    /**
     * Sets the model name of the device.
     *
     * @param modelName The model name to set.
     */
    void setModelName(String modelName);

    /**
     * Retrieves the format of monitoring data for the device.
     *
     * @return A {@link MonitoringResultFormat} representing the format.
     */
    MonitoringResultFormat getMonitoringDataFormat();

    /**
     * Sets the format of monitoring data for the device.
     *
     * @param monitoringDataFormat The monitoring data format to set.
     */
    void setMonitoringDataFormat(MonitoringResultFormat monitoringDataFormat);

    /**
     * Retrieves the list of monitoring binary protocols supported by the device.
     *
     * @return A {@link List} of {@link MonitoringBinaryProtocol} objects.
     */
    List<MonitoringBinaryProtocol> getMonitoringBinaryProtocol();

    /**
     * Sets the list of monitoring binary protocols supported by the device.
     *
     * @param monitoringBinaryProtocol The list of protocols to set.
     */
    void setMonitoringBinaryProtocol(List<MonitoringBinaryProtocol> monitoringBinaryProtocol);

    /**
     * Retrieves the device type.
     *
     * @return The {@link DeviceTypes} representing the device type.
     */
    DeviceTypes getDeviceType();

    /**
     * Sets the device type.
     *
     * @param deviceType The {@link DeviceTypes} to set.
     */
    void setDeviceType(DeviceTypes deviceType);

    /**
     * Retrieves the LG API version associated with the device.
     *
     * @return The {@link LGAPIVerion} of the device.
     */
    LGAPIVerion getDeviceVersion();

    /**
     * Sets the LG API version associated with the device.
     *
     * @param version The {@link LGAPIVerion} to set.
     */
    void setDeviceVersion(LGAPIVerion version);

    /**
     * Retrieves the raw data associated with the device.
     *
     * @return A {@link Map} containing raw data values.
     */
    Map<String, Object> getRawData();

    /**
     * Sets the raw data for the device.
     *
     * @param rawData A {@link Map} containing raw data values.
     */
    void setRawData(Map<String, Object> rawData);

    /**
     * Retrieves raw data values for each feature of the device.
     *
     * @return A {@link Map} where each key is a feature name and
     *         the value is another map of raw feature data.
     */
    Map<String, Map<String, Object>> getFeatureValuesRawData();

    /**
     * Retrieves the feature definition based on its name from the device's JSON definition.
     * <p>
     * Example (for API v2):
     *
     * <pre>
     * "MonitoringValue": {
     *     "spin": {
     *         "valueMapping": { ... }
     *     }
     * }
     * </pre>
     * <p>
     * Calling {@code getFeatureDefinition("spin")} will return the corresponding
     * {@link FeatureDefinition} object representing the "spin" feature.
     * </p>
     *
     * @param featureName The name of the feature in the JSON definition.
     * @return A {@link FeatureDefinition} representing the specified feature.
     */
    FeatureDefinition getFeatureDefinition(String featureName);
}
