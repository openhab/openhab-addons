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
package org.openhab.binding.dbquery.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dbquery.internal.config.InfluxDB2BridgeConfiguration;
import org.openhab.binding.dbquery.internal.dbimpl.influx2.Influx2Database;
import org.openhab.binding.dbquery.internal.dbimpl.influx2.InfluxDBClientFacadeImpl;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.core.thing.Bridge;

/**
 * Concrete implementation of {@link DatabaseBridgeHandler} for Influx2
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class InfluxDB2BridgeHandler extends DatabaseBridgeHandler {
    private InfluxDB2BridgeConfiguration config = new InfluxDB2BridgeConfiguration();

    public InfluxDB2BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    Database createDatabase() {
        return new Influx2Database(config, new InfluxDBClientFacadeImpl(config));
    }

    @Override
    protected void initConfig() {
        config = getConfig().as(InfluxDB2BridgeConfiguration.class);
    }
}
