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

package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link SecondGenerationHandler} is responsible for handling commands, which are
 * sent to one of the channels, and initiation and refreshing regarded to second generation part of the binding.
 *
 *
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 * @author Örjan Backsell - Redesigned regarding Piko1020, Piko New Generation
 */
@NonNullByDefault
public class SecondGenerationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SecondGenerationHandler.class);

    private @Nullable ScheduledFuture<?> secondGenerationPoller;

    private final HttpClient httpClient;

    private List<SecondGenerationChannelConfiguration> channelConfigs = new ArrayList<>();
    private List<SecondGenerationChannelConfiguration> channelConfigsExt = new ArrayList<>();
    private List<SecondGenerationChannelConfiguration> channelConfigsExtExt = new ArrayList<>();

    public SecondGenerationHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final @Nullable SecondGenerationBindingConstants handleCommandConfig;
        handleCommandConfig = getConfigAs(SecondGenerationBindingConstants.class);

        String url = handleCommandConfig.url.toString();
        String username = handleCommandConfig.username;
        String password = handleCommandConfig.password;
        String valueConfiguration = "";
        String dxsEntriesConf = "";

        switch (channelUID.getId()) {
            case SecondGenerationBindingConstants.CHANNEL_CHARGETIMEEND:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556236";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_BATTERYTYPE:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556252";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_BATTERYUSAGECONSUMPTION:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556249";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_BATTERYUSAGESTRATEGY:
                valueConfiguration = command.toString();
                dxsEntriesConf = "83888896";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_SMARTBATTERYCONTROL:
                valueConfiguration = "";
                if (command == OnOffType.ON) {
                    valueConfiguration = "True";
                }
                if (command == OnOffType.OFF) {
                    valueConfiguration = "False";
                }
                dxsEntriesConf = "33556484";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_SMARTBATTERYCONTROL_TEXT:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556484";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_BATTERYCHARGETIMEFROM:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556239";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_BATTERYCHARGETIMETO:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556240";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_MAXDEPTHOFDISCHARGE:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556247";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_SHADOWMANAGEMENT:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556483";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_EXTERNALMODULECONTROL:
                valueConfiguration = command.toString();
                dxsEntriesConf = "33556482";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
            case SecondGenerationBindingConstants.CHANNEL_INVERTERNAME:
                valueConfiguration = command.toString();
                dxsEntriesConf = "16777984";
                preSetExecuteConfigurationChanges(httpClient, url, username, password, dxsEntriesConf,
                        valueConfiguration);
                break;
        }
    }

    @Override
    public void initialize() {
        channelConfigs = SecondGenerationChannelConfiguration.getChannelConfiguration();
        channelConfigsExt = SecondGenerationChannelConfiguration.getChannelConfigurationExt();
        channelConfigsExtExt = SecondGenerationChannelConfiguration.getChannelConfigurationExtExt();

        // Temporary value during initializing
        updateStatus(ThingStatus.UNKNOWN);

        // Start update as configured
        secondGenerationPoller = scheduler.scheduleWithFixedDelay(() -> {
            try {
                refresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (RuntimeException scheduleWithFixedDelayException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        scheduleWithFixedDelayException.getClass().getName() + ":"
                                + scheduleWithFixedDelayException.getMessage());
                logger.debug("Error when connecting to inverter, {}: {}", getThing().getUID(),
                        scheduleWithFixedDelayException.getMessage());
            } catch (InterruptedException e) {
                logger.debug("Communication with inverter interrupted, exception {}", e.getMessage());
            } catch (ExecutionException e) {
                logger.debug("Communication with inverter failed, exception {}", e.getMessage());
            } catch (TimeoutException e) {
                logger.debug("Communication with inverter timed out, exception {}", e.getMessage());
            }
        }, 0, SecondGenerationBindingConstants.REFRESHINTERVAL_SEC, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> secondGenerationLocalPoller = secondGenerationPoller;

        if (secondGenerationLocalPoller != null) {
            secondGenerationLocalPoller.cancel(true);
            secondGenerationPoller = null;
        }
    }

    @SuppressWarnings("null")
    private void refresh() throws InterruptedException, ExecutionException, TimeoutException {
        final @Nullable SecondGenerationBindingConstants refreshConfig;
        refreshConfig = getConfigAs(SecondGenerationBindingConstants.class);

        String dxsEntriesCall = refreshConfig.url.toString() + "/api/dxs.json?dxsEntries="
                + channelConfigs.get(0).dxsEntries;
        String dxsEntriesCallExt = refreshConfig.url.toString() + "/api/dxs.json?dxsEntries="
                + channelConfigsExt.get(0).dxsEntries;

        for (int i = 1; i < channelConfigs.size(); i++) {
            dxsEntriesCall += ("&dxsEntries=" + channelConfigs.get(i).dxsEntries);
            dxsEntriesCallExt += ("&dxsEntries=" + channelConfigsExt.get(i).dxsEntries);
        }

        String jsonDxsEntriesResponse = callURL(dxsEntriesCall);
        String jsonDxsEntriesResponseExt = callURL(dxsEntriesCallExt);
        String jsonDxsEntriesResponseExtExt = callURL(refreshConfig.url.toString() + "/api/dxs.json?dxsEntries="
                + channelConfigsExtExt.get(0).dxsEntries + "&dxsEntries=" + channelConfigsExtExt.get(1).dxsEntries
                + "&dxsEntries=" + channelConfigsExtExt.get(2).dxsEntries);

        // Get Gson object
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Parse result
        SecondGenerationDxsEntriesContainerDTO dxsEntriesContainer = gson.fromJson(jsonDxsEntriesResponse,
                SecondGenerationDxsEntriesContainerDTO.class);
        SecondGenerationDxsEntriesContainerDTO dxsEntriesContainerExt = gson.fromJson(jsonDxsEntriesResponseExt,
                SecondGenerationDxsEntriesContainerDTO.class);
        SecondGenerationDxsEntriesContainerDTO dxsEntriesContainerExtExt = gson.fromJson(jsonDxsEntriesResponseExtExt,
                SecondGenerationDxsEntriesContainerDTO.class);

        String[] channelPostsAll = new String[49];

        // Fill channelPosts with each item value
        int channelPostsCounterAll = 0;
        for (SecondGenerationDxsEntries dxsentries : dxsEntriesContainer.dxsEntries) {
            channelPostsAll[channelPostsCounterAll] = dxsentries.getName();
            channelPostsCounterAll++;
        }

        for (SecondGenerationDxsEntries dxsentriesExt : dxsEntriesContainerExt.dxsEntries) {
            channelPostsAll[channelPostsCounterAll] = dxsentriesExt.getName();
            channelPostsCounterAll++;
        }

        for (SecondGenerationDxsEntries dxsentriesExtExt : dxsEntriesContainerExtExt.dxsEntries) {
            channelPostsAll[channelPostsCounterAll] = dxsentriesExtExt.getName();
            channelPostsCounterAll++;
        }

        // Create and update actual values for each channelPost
        int channelValuesCounter = 0;

        for (SecondGenerationChannelConfiguration cConfig : channelConfigs) {
            Channel channel = getThing().getChannel(cConfig.id);
            State state = getState(channelPostsAll[channelValuesCounter], cConfig.unit);

            // Update the channels
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounter++;
            }
        }
    }

    // Help method of handleCommand to with SecondGenerationConfigurationHandler.executeConfigurationChanges method send
    // configuration changes.
    public final void preSetExecuteConfigurationChanges(HttpClient httpClient, String url, String username,
            String password, String dxsEntriesConf, String valueConfiguration) {
        try {
            SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                    dxsEntriesConf, valueConfiguration);
        } catch (Exception handleCommandException) {
            logger.debug("Handle command for {} on channel {}: {}: {}: {}: {}", thing.getUID(), httpClient, url,
                    dxsEntriesConf, valueConfiguration, handleCommandException.getMessage());
        }
    }

    public final String callURL(String dxsEntriesCall)
            throws InterruptedException, ExecutionException, TimeoutException {
        String jsonDxsresponse = httpClient.GET(dxsEntriesCall).getContentAsString();

        return jsonDxsresponse;
    }

    private State getState(String value, @Nullable Unit<?> unit) {
        if (unit == null) {
            return new StringType(value);
        } else {
            try {
                return new QuantityType<>(new BigDecimal(value), unit);
            } catch (NumberFormatException getStateException) {
                logger.debug("Error parsing value '{}: {}'", value, getStateException.getMessage());
                return UnDefType.UNDEF;
            }
        }
    }
}
