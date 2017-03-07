package org.openhab.binding.weather.internal.discovery;

import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.weather.WeatherBindingConstants;
import org.openhab.binding.weather.handler.BridgeListener;
import org.openhab.binding.weather.handler.WeatherBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class WeatherDiscoveryHandler extends AbstractDiscoveryService implements BridgeListener {
    private Logger logger = LoggerFactory.getLogger(WeatherDiscoveryHandler.class);

    private WeatherBridgeHandler bridge;

    public WeatherDiscoveryHandler(WeatherBridgeHandler bridge) throws IllegalArgumentException {
        super(60);
        this.bridge = bridge;
    }

    @Override
    public void activate(Map<String, Object> configProperties) {
        logger.debug("Activate!");
        super.activate(configProperties);
        this.bridge.addListener(this);
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivate!");
        super.deactivate();
        this.bridge.removeListener(this);
    }

    @Override
    protected void startScan() {
        // this.bridge.startScan();
    }

    @Override
    public void onDayFound(int day) {
        logger.info("Forecase day discovered {}", day);
        ThingUID thingUID = new ThingUID(WeatherBindingConstants.THING_TYPE_FORECAST, bridge.getThing().getUID(),
                Integer.toString(day));
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(WeatherBindingConstants.PROPERTY_DAY, Integer.toString(day));
        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(bridge.getThing().getUID())
                .withLabel("Forecast " + day).withProperties(properties).build();
        thingDiscovered(result);
    }
}
