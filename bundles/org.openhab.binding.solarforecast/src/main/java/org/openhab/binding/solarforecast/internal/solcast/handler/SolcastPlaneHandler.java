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
package org.openhab.binding.solarforecast.internal.solcast.handler;

import static org.openhab.binding.solarforecast.internal.SolarForecastBindingConstants.*;
import static org.openhab.binding.solarforecast.internal.solcast.SolcastConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.solarforecast.internal.actions.SolarForecast;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastActions;
import org.openhab.binding.solarforecast.internal.actions.SolarForecastProvider;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject.QueryMode;
import org.openhab.binding.solarforecast.internal.solcast.config.SolcastPlaneConfiguration;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolcastPlaneHandler} is a non active handler instance. It will be triggerer by the bridge.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class SolcastPlaneHandler extends BaseThingHandler implements SolarForecastProvider {
    private final Logger logger = LoggerFactory.getLogger(SolcastPlaneHandler.class);
    private final HttpClient httpClient;
    private SolcastPlaneConfiguration configuration = new SolcastPlaneConfiguration();
    private Optional<SolcastBridgeHandler> bridgeHandler = Optional.empty();
    protected Optional<SolcastObject> forecast = Optional.empty();

    public SolcastPlaneHandler(Thing thing, HttpClient hc) {
        super(thing);
        httpClient = hc;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(SolarForecastActions.class);
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(SolcastPlaneConfiguration.class);

        // connect Bridge & Status
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof SolcastBridgeHandler sbh) {
                    bridgeHandler = Optional.of(sbh);
                    Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX
                            : Instant.now().minusSeconds(1);
                    forecast = Optional.of(new SolcastObject(thing.getUID().getAsString(), expiration, sbh));
                    sbh.addPlane(this);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/solarforecast.plane.status.wrong-handler [\"" + handler + "\"]");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/solarforecast.plane.status.bridge-handler-not-found");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/solarforecast.plane.status.bridge-missing");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        bridgeHandler.ifPresent(bridge -> bridge.removePlane(this));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            forecast.ifPresent(forecastObject -> {
                String group = channelUID.getGroupId();
                if (group == null) {
                    group = EMPTY;
                }
                String channel = channelUID.getIdWithoutGroup();
                QueryMode mode = QueryMode.Average;
                switch (group) {
                    case GROUP_AVERAGE:
                        mode = QueryMode.Average;
                        break;
                    case GROUP_OPTIMISTIC:
                        mode = QueryMode.Optimistic;
                        break;
                    case GROUP_PESSIMISTIC:
                        mode = QueryMode.Pessimistic;
                        break;
                    case GROUP_RAW:
                        forecast.ifPresent(f -> {
                            updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_JSON,
                                    StringType.valueOf(f.getRaw()));
                        });
                }
                switch (channel) {
                    case CHANNEL_ENERGY_ESTIMATE:
                        sendTimeSeries(CHANNEL_ENERGY_ESTIMATE, forecastObject.getEnergyTimeSeries(mode));
                        break;
                    case CHANNEL_POWER_ESTIMATE:
                        sendTimeSeries(CHANNEL_POWER_ESTIMATE, forecastObject.getPowerTimeSeries(mode));
                        break;
                    default:
                        updateChannels(forecastObject);
                }
            });
        }
    }

    protected synchronized SolcastObject fetchData() {
        bridgeHandler.ifPresent(bridge -> {
            forecast.ifPresent(forecastObject -> {
                if (forecastObject.isExpired()) {
                    logger.trace("Get new forecast {}", forecastObject.toString());
                    String forecastUrl = String.format(FORECAST_URL, configuration.resourceId);
                    String currentEstimateUrl = String.format(CURRENT_ESTIMATE_URL, configuration.resourceId);
                    try {
                        // get actual estimate
                        Request estimateRequest = httpClient.newRequest(currentEstimateUrl);
                        estimateRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge.getApiKey());
                        ContentResponse crEstimate = estimateRequest.send();
                        if (crEstimate.getStatus() == 200) {
                            Instant expiration = (configuration.refreshInterval == 0) ? Instant.MAX
                                    : Instant.now().plus(configuration.refreshInterval, ChronoUnit.MINUTES);
                            SolcastObject localForecast = new SolcastObject(thing.getUID().getAsString(),
                                    crEstimate.getContentAsString(), expiration, bridge);

                            // get forecast
                            Request forecastRequest = httpClient.newRequest(forecastUrl);
                            forecastRequest.header(HttpHeader.AUTHORIZATION, BEARER + bridge.getApiKey());
                            ContentResponse crForecast = forecastRequest.send();

                            if (crForecast.getStatus() == 200) {
                                localForecast.join(crForecast.getContentAsString());
                                setForecast(localForecast);
                                updateState(GROUP_RAW + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_JSON,
                                        StringType.valueOf(forecast.get().getRaw()));
                                updateStatus(ThingStatus.ONLINE);
                            } else {
                                logger.debug("{} Call {} failed {}", thing.getLabel(), forecastUrl,
                                        crForecast.getStatus());
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "@text/solarforecast.plane.status.http-status [\"" + crForecast.getStatus()
                                                + "\"]");
                            }
                        } else {
                            logger.debug("{} Call {} failed {}", thing.getLabel(), currentEstimateUrl,
                                    crEstimate.getStatus());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "@text/solarforecast.plane.status.http-status [\"" + crEstimate.getStatus()
                                            + "\"]");
                        }
                    } catch (ExecutionException | TimeoutException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    } catch (InterruptedException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                } else {
                    updateChannels(forecastObject);
                }
            });
        });
        return forecast.get();
    }

    protected void updateChannels(SolcastObject f) {
        if (bridgeHandler.isEmpty()) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(bridgeHandler.get().getTimeZone());
        List<QueryMode> modes = List.of(QueryMode.Average, QueryMode.Pessimistic, QueryMode.Optimistic);
        modes.forEach(mode -> {
            double energyDay = f.getDayTotal(now.toLocalDate(), mode);
            double energyProduced = f.getActualEnergyValue(now, mode);
            String group = switch (mode) {
                case Average -> GROUP_AVERAGE;
                case Optimistic -> GROUP_OPTIMISTIC;
                case Pessimistic -> GROUP_PESSIMISTIC;
                case Error -> throw new IllegalStateException("mode " + mode + " not expected");
            };
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ACTUAL,
                    Utils.getEnergyState(energyProduced));
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_REMAIN,
                    Utils.getEnergyState(energyDay - energyProduced));
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_TODAY,
                    Utils.getEnergyState(energyDay));
            updateState(group + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ACTUAL,
                    Utils.getPowerState(f.getActualPowerValue(now, QueryMode.Average)));
        });
    }

    protected synchronized void setForecast(SolcastObject f) {
        forecast = Optional.of(f);
        sendTimeSeries(GROUP_AVERAGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                f.getPowerTimeSeries(QueryMode.Average));
        sendTimeSeries(GROUP_AVERAGE + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                f.getEnergyTimeSeries(QueryMode.Average));
        sendTimeSeries(GROUP_OPTIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                f.getPowerTimeSeries(QueryMode.Optimistic));
        sendTimeSeries(GROUP_OPTIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                f.getEnergyTimeSeries(QueryMode.Optimistic));
        sendTimeSeries(GROUP_PESSIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_POWER_ESTIMATE,
                f.getPowerTimeSeries(QueryMode.Pessimistic));
        sendTimeSeries(GROUP_PESSIMISTIC + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_ENERGY_ESTIMATE,
                f.getEnergyTimeSeries(QueryMode.Pessimistic));
        bridgeHandler.ifPresent(h -> {
            h.forecastUpdate();
        });
    }

    @Override
    public synchronized List<SolarForecast> getSolarForecasts() {
        return List.of(forecast.get());
    }
}
