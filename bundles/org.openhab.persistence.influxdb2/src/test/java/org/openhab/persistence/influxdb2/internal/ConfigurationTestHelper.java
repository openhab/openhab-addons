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
package org.openhab.persistence.influxdb2.internal;

import static org.openhab.persistence.influxdb2.internal.InfluxDBConfiguration.*;

import java.util.HashMap;
import java.util.Map;

import org.openhab.persistence.influxdb2.InfluxDBVersion;

/**
 * @author Joan Pujol Espinar - Initial contribution
 */
public class ConfigurationTestHelper {

    public static Map<String, Object> createValidConfigurationParameters() {
        Map<String, Object> config = new HashMap<>();
        config.put(URL_PARAM, "http://localhost:9999");
        config.put(VERSION_PARAM, InfluxDBVersion.V2.name());
        config.put(TOKEN_PARAM, "sampletoken");
        config.put(DATABASE_PARAM, "openhab");
        config.put(RETENTION_POLICY_PARAM, "default");
        return config;
    }

    public static Map<String, Object> createInvalidConfigurationParameters() {
        Map<String, Object> config = createValidConfigurationParameters();
        config.remove(TOKEN_PARAM);
        return config;
    }

}
