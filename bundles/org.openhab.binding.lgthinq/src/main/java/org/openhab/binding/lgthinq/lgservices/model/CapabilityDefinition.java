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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.FeatureDefinition;

/**
 * The {@link CapabilityDefinition}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface CapabilityDefinition {
    String getModelName();

    void setModelName(String modelName);

    MonitoringResultFormat getMonitoringDataFormat();

    void setMonitoringDataFormat(MonitoringResultFormat monitoringDataFormat);

    List<MonitoringBinaryProtocol> getMonitoringBinaryProtocol();

    void setMonitoringBinaryProtocol(List<MonitoringBinaryProtocol> monitoringBinaryProtocol);

    DeviceTypes getDeviceType();

    LGAPIVerion getDeviceVersion();

    void setDeviceType(DeviceTypes deviceType);

    void setDeviceVersion(LGAPIVerion version);

    Map<String, Object> getRawData();

    Map<String, Map<String, Object>> getFeatureValuesRawData();

    /**
     * This method get the feature based on its name in the JSON device's definition.
     * Ex: For V2: "MonitoringValue": {
     * ...
     * "spin" : {
     * ...
     * valueMapping{
     * ...
     * }
     * }
     * }
     * getFeatureDefinition("spin") will return the FeatureDefinition object representing "spin" feature configuration.
     * 
     * @param featureName name of the feature node in the json definition
     * @return return FeatureDefinition object representing the feature in case.
     */
    FeatureDefinition getFeatureDefinition(String featureName);

    void setRawData(Map<String, Object> rawData);
}
