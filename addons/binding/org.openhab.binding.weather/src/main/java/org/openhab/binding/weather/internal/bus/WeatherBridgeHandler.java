package org.openhab.binding.weather.internal.bus;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.weather.WeatherBindingConstants;
import org.openhab.binding.weather.internal.common.LocationConfig;
import org.openhab.binding.weather.internal.model.Weather;
import org.openhab.binding.weather.internal.provider.WeatherProvider;
import org.openhab.binding.weather.internal.provider.WeatherProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Bridge for the weather binding. This will handle creating the forecast
 * days and other things to handle the
 *
 * @author David Bennett - Initial Contribution
 */
public class WeatherBridgeHandler extends BaseBridgeHandler {
    private static final Logger logger = LoggerFactory.getLogger(WeatherBridgeHandler.class);
    private WeatherProvider provider;
    private ScheduledFuture<?> pollingJob;
    private List<BridgeListener> listeners = Lists.newArrayList();

    public WeatherBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        super.initialize();
        LocationConfig config = getConfigAs(LocationConfig.class);
        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration setup");
            return;
        }
        // Setup the channels and stuff.
        try {
            provider = WeatherProviderFactory.createWeatherProvider(config.getProviderName());
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Exception setting up provider");
            return;
        }
        startPolling(config);
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPolling();
        provider = null;
    }

    private void startPolling(LocationConfig config) {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(new ScheduledTask(), 0, config.getUpdateInterval(),
                    TimeUnit.MINUTES);
        }
    }

    private void stopPolling() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private WeatherThingHandler getThingForDay(int day) {
        String dayStr = String.format("%d", day);
        for (Thing thing : getThing().getThings()) {
            if (thing.getProperties().get(WeatherBindingConstants.PROPERTY_DAY).equals(dayStr)) {
                return (WeatherThingHandler) thing.getHandler();
            }
        }
        return null;
    }

    private void publish(Weather weather) {
        // See how many things are out there with stuff on them.
        WeatherThingHandler current = getThingForDay(0);
        if (current != null) {
            current.updateWeather(weather);
        } else {
            for (BridgeListener listen : listeners) {
                listen.onDayFound(0);
            }
        }
        for (int i = 0; i < weather.getForecast().size(); i++) {
            WeatherThingHandler forecast = getThingForDay(i);
            if (forecast != null) {
                forecast.updateWeather(weather.getForecast().get(i));
            } else {
                for (BridgeListener listen : listeners) {
                    listen.onDayFound(i);
                }
            }
        }
    }

    private class ScheduledTask implements Runnable {

        @Override
        public void run() {
            Weather weather;
            try {
                weather = provider.getWeather(getConfigAs(LocationConfig.class));
                publish(weather);
            } catch (Exception e) {
                logger.error("Exception trying to get the weather", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Exception retrieving weather");

            }
        }
    }
}
