package org.openhab.binding.dbquery.internal;

import org.openhab.binding.dbquery.internal.config.InfluxDB2BridgeConfiguration;
import org.openhab.binding.dbquery.internal.dbimpl.influx2.Influx2Database;
import org.openhab.binding.dbquery.internal.dbimpl.influx2.InfluxDBClientFacadeImpl;
import org.openhab.binding.dbquery.internal.domain.Database;
import org.openhab.core.thing.Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDB2BridgeHandler extends DatabaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(InfluxDB2BridgeHandler.class);
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
