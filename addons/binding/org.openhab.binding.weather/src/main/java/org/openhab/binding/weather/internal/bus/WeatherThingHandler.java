package org.openhab.binding.weather.internal.bus;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.weather.internal.model.Weather;

/**
 * Does the bridge bits to work with openhab 2.0
 *
 * @author David Bennett - Initial Contribution
 */
public class WeatherThingHandler extends BaseThingHandler {

    public WeatherThingHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        super.initialize();

    }

    public void updateWeather(Weather weather) {
    }
}
