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
package org.openhab.persistence.influxdb2;

import org.openhab.persistence.influxdb2.internal.FilterCriteriaQueryCreator;
import org.openhab.persistence.influxdb2.internal.InfluxDB2Repository;
import org.openhab.persistence.influxdb2.internal.InfluxDBConfiguration;
import org.openhab.persistence.influxdb2.internal.influx2.Influx2FilterCriteriaQueryCreatorImpl;
import org.openhab.persistence.influxdb2.internal.influx2.InfluxDB2RepositoryImpl;

/**
 * Factory that returns {@link InfluxDB2Repository} and {@link FilterCriteriaQueryCreator} implementations
 * depending on InfluxDB version
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
public class RepositoryFactory {

    public static InfluxDB2Repository createRepository(InfluxDBConfiguration influxDBConfiguration) {
        switch (influxDBConfiguration.getVersion()) {
            case V1:
            case V2:
                return new InfluxDB2RepositoryImpl(influxDBConfiguration);
            default:
                throw new RuntimeException("Not expected version " + influxDBConfiguration.getVersion());
        }
    }

    public static FilterCriteriaQueryCreator createQueryCreator(InfluxDBConfiguration influxDBConfiguration) {
        switch (influxDBConfiguration.getVersion()) {
            case V1:
            case V2:
                return new Influx2FilterCriteriaQueryCreatorImpl();
            default:
                throw new RuntimeException("Not expected version " + influxDBConfiguration.getVersion());
        }
    }
}
