/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather.handler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.weather.WeatherBindingConstants;
import org.openhab.binding.weather.internal.common.LocationConfig;
import org.openhab.binding.weather.internal.model.ProviderName;
import org.openhab.binding.weather.internal.model.Weather;
import org.openhab.binding.weather.internal.parser.CommonIdHandler;
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
    private final CommonIdHandler commonIdHandler;

    public WeatherBridgeHandler(Bridge bridge, CommonIdHandler commonIdHandler) {
        super(bridge);
        this.commonIdHandler = commonIdHandler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        super.initialize();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);
        LocationConfig config = getConfigAs(LocationConfig.class);
        if (!config.isValid()) {
            if (config.getProviderName() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid provider name");
            } else if (config.getLanguage() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid language");
            } else if (config.getUpdateInterval() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid update interval");
            } else if (config.getApiKey() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid api key");
            } else if (config.getProviderName() == ProviderName.HamWeather && config.getApiKey2() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid api key 2");
            } else if (config.getProviderName() == ProviderName.Yahoo && config.getWoeid() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid woeid");
            } else if (config.getLatitude() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid latitude");
            } else if (config.getLongitude() == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid longitude");
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid configuration setup");
            }
            return;
        }
        // Setup the channels and stuff.
        try {
            provider = WeatherProviderFactory.createWeatherProvider(config.getProviderName(), commonIdHandler);
            if (provider == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Unable to create provider");
            }
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
        assert (this.listeners.isEmpty());
    }

    public void addListener(BridgeListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(BridgeListener listener) {
        this.listeners.remove(listener);
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

    private final void updateWeather(Weather weather) {
        // Atmosphere.
        if (weather.getAtmosphere().getHumidity() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_HUMIDITY);
            updateState(chan.getUID(), new DecimalType(weather.getAtmosphere().getHumidity()));
        }
        if (weather.getAtmosphere().getVisibility() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_VISIBILITY);
            updateState(chan.getUID(), new DecimalType(weather.getAtmosphere().getVisibility()));
        }
        if (weather.getAtmosphere().getPressure() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_PRESSURE);
            updateState(chan.getUID(), new DecimalType(weather.getAtmosphere().getPressure()));
        }
        if (weather.getAtmosphere().getPressureTrend() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_PRESSURE_TREND);
            updateState(chan.getUID(), new StringType(weather.getAtmosphere().getPressureTrend()));
        }
        if (weather.getAtmosphere().getOzone() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_OZONE);
            updateState(chan.getUID(), new DecimalType(weather.getAtmosphere().getOzone()));
        }
        if (weather.getAtmosphere().getUvIndex() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_UVINDEX);
            updateState(chan.getUID(), new DecimalType(weather.getAtmosphere().getUvIndex()));
        }

        // Cloud channels
        if (weather.getClouds().getPercent() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_CLOUD_COVER_PERCENT);
            updateState(chan.getUID(), new DecimalType(weather.getClouds().getPercent()));
        }

        // Condition channels
        if (weather.getCondition().getText() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_CONDITION);
            updateState(chan.getUID(), new StringType(weather.getCondition().getText()));
        }
        if (weather.getCondition().getText() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_OBSERVATION_TIME);
            updateState(chan.getUID(), new DateTimeType(weather.getCondition().getObservationTime()));
        }
        if (weather.getCondition().getCommonId() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_CONDITION_ID);
            updateState(chan.getUID(), new StringType(weather.getCondition().getCommonId()));
        }
        if (weather.getCondition().getIcon() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_ICON);
            updateState(chan.getUID(), new StringType(weather.getCondition().getIcon()));
        }

        // Precipitation channels
        if (weather.getPrecipitation().getRain() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_RAIN);
            updateState(chan.getUID(), new DecimalType(weather.getPrecipitation().getRain()));
        }
        if (weather.getPrecipitation().getSnow() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_SNOW);
            updateState(chan.getUID(), new DecimalType(weather.getPrecipitation().getSnow()));
        }
        if (weather.getPrecipitation().getTotal() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_TOTAL);
            updateState(chan.getUID(), new DecimalType(weather.getPrecipitation().getTotal()));
        }
        if (weather.getPrecipitation().getProbability() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_PROBABILILITY);
            updateState(chan.getUID(), new DecimalType(weather.getPrecipitation().getProbability()));
        }
        if (weather.getPrecipitation().getType() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_TYPE);
            updateState(chan.getUID(), new StringType(weather.getPrecipitation().getType()));
        }

        // Station channels
        if (weather.getStation().getName() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_STATION_NAME);
            updateState(chan.getUID(), new StringType(weather.getStation().getName()));
        }
        if (weather.getStation().getId() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_STATION_ID);
            updateState(chan.getUID(), new StringType(weather.getStation().getId()));
        }
        if (weather.getStation().getLatitude() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_LATITUDE);
            updateState(chan.getUID(), new DecimalType(weather.getStation().getLatitude()));
        }
        if (weather.getStation().getLongitude() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_LONGITUDE);
            updateState(chan.getUID(), new DecimalType(weather.getStation().getLongitude()));
        }
        if (weather.getStation().getAltitude() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_ALTITUDE);
            updateState(chan.getUID(), new DecimalType(weather.getStation().getAltitude()));
        }

        // Temperature channels.
        if (weather.getTemperature().getCurrent() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_CURRENT);
            updateState(chan.getUID(), new DecimalType(weather.getTemperature().getCurrent()));
        }
        if (weather.getTemperature().getMin() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_MIN);
            updateState(chan.getUID(), new DecimalType(weather.getTemperature().getMin()));
        }
        if (weather.getTemperature().getMax() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_MAX);
            updateState(chan.getUID(), new DecimalType(weather.getTemperature().getMax()));
        }
        if (weather.getTemperature().getFeel() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_FEEL);
            updateState(chan.getUID(), new DecimalType(weather.getTemperature().getFeel()));
        }
        if (weather.getTemperature().getDewpoint() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_DEWPOINT);
            updateState(chan.getUID(), new DecimalType(weather.getTemperature().getDewpoint()));
        }

        // Wind channels.
        if (weather.getWind().getSpeed() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_SPEED);
            updateState(chan.getUID(), new DecimalType(weather.getWind().getSpeed()));
        }
        if (weather.getWind().getDirection() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_DIRECTION);
            updateState(chan.getUID(), new DecimalType(weather.getWind().getDirection()));
        }
        if (weather.getWind().getDegree() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_DEGREE);
            updateState(chan.getUID(), new DecimalType(weather.getWind().getDegree()));
        }
        if (weather.getWind().getGust() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_GUST);
            updateState(chan.getUID(), new DecimalType(weather.getWind().getGust()));
        }
        if (weather.getWind().getChill() != null) {
            Channel chan = getThing().getChannel(WeatherBindingConstants.CHANNEL_CHILL);
            updateState(chan.getUID(), new DecimalType(weather.getWind().getChill()));
        }
    }

    private void publish(Weather weather) {
        // See how many things are out there with stuff on them.
        updateWeather(weather);
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
                weather = provider.getWeather(getConfigAs(LocationConfig.class), getThing());
                publish(weather);
            } catch (Exception e) {
                logger.error("Exception trying to get the weather", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Exception retrieving weather");

            }
        }
    }
}
