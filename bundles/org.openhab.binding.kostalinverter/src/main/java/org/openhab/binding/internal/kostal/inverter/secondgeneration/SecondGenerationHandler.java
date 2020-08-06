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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
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

    private HttpClient httpClient;

    private @Nullable SecondGenerationConfiguration config;

    @SuppressWarnings("unused")
    @Nullable
    private SecondGenerationBindingConstants configurationConfig;

    private List<SecondGenerationChannelConfiguration> channelConfigs = new ArrayList<>();
    private List<SecondGenerationChannelConfiguration> channelConfigsExt = new ArrayList<>();
    private List<SecondGenerationChannelConfiguration> channelConfigsExtExt = new ArrayList<>();

    public SecondGenerationHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        config = getConfigAs(SecondGenerationConfiguration.class);
        String url = config.url.toString();
        String username = config.username;
        String password = config.password;
        String valueConfiguration = "";
        String dxsEntriesConf = "";

        if (command instanceof RefreshType) {
            logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);
        }

        if (command == RefreshType.REFRESH) {
            return;
        }

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

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                refresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception scheduleWithFixedDelayException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        scheduleWithFixedDelayException.getClass().getName() + ":"
                                + scheduleWithFixedDelayException.getMessage());
                logger.debug("Error refreshing source = {}", getThing().getUID(), scheduleWithFixedDelayException);
            }
        }, 0, SecondGenerationConfiguration.REFRESHINTERVAL, TimeUnit.SECONDS);

    }

    @SuppressWarnings("null")
    private void refresh() throws Exception {
        String dxsEntriesCall = config.url.toString() + "/api/dxs.json?dxsEntries=" + channelConfigs.get(0).dxsEntries;
        String dxsEntriesCallExt = config.url.toString() + "/api/dxs.json?dxsEntries="
                + channelConfigsExt.get(0).dxsEntries;

        for (int i = 1; i < channelConfigs.size(); i++) {
            dxsEntriesCall += ("&dxsEntries=" + channelConfigs.get(i).dxsEntries);
            dxsEntriesCallExt += ("&dxsEntries=" + channelConfigsExt.get(i).dxsEntries);
        }

        String jsonDxsEntriesResponse = callURL(dxsEntriesCall);
        String jsonDxsEntriesResponseExt = callURL(dxsEntriesCallExt);
        String jsonDxsEntriesResponseExtExt = callURL(config.url.toString() + "/api/dxs.json?dxsEntries="
                + channelConfigsExt.get(0).dxsEntries + "&dxsEntries=" + channelConfigsExt.get(1).dxsEntries
                + "&dxsEntries=" + channelConfigsExt.get(2).dxsEntries);

        // Get Gson object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Parse result
        SecondGenerationDxsEntriesContainer dxsEntriesContainer = gson.fromJson(jsonDxsEntriesResponse,
                SecondGenerationDxsEntriesContainer.class);
        SecondGenerationDxsEntriesContainer dxsEntriesContainerExt = gson.fromJson(jsonDxsEntriesResponseExt,
                SecondGenerationDxsEntriesContainer.class);
        SecondGenerationDxsEntriesContainer dxsEntriesContainerExtExt = gson.fromJson(jsonDxsEntriesResponseExtExt,
                SecondGenerationDxsEntriesContainer.class);

        // Create channel-posts array's
        String[] channelPosts = new String[23];
        String[] channelPostsExt = new String[23];
        String[] channelPostsExtExt = new String[3];

        // Fill channelPosts with each item value
        int channelPostsCounter = 0;
        for (SecondGenerationDxsEntries dxsentries : dxsEntriesContainer.dxsEntries) {
            channelPosts[channelPostsCounter] = dxsentries.getName();
            channelPostsCounter++;
        }

        // Fill channelPostsExt with each item value
        int channelPostsCounterExt = 0;
        for (SecondGenerationDxsEntries dxsentriesExt : dxsEntriesContainerExt.dxsEntries) {
            channelPostsExt[channelPostsCounterExt] = dxsentriesExt.getName();
            channelPostsCounterExt++;
        }

        // Fill channelPostsExtExt with each item value
        int channelPostsCounterExtExt = 0;
        for (SecondGenerationDxsEntries dxsentriesExtExt : dxsEntriesContainerExtExt.dxsEntries) {
            channelPostsExtExt[channelPostsCounterExtExt] = dxsentriesExtExt.getName();
            channelPostsCounterExtExt++;
        }

        // Create and update actual values for each channelPost
        int channelValuesCounter = 0;

        for (SecondGenerationChannelConfiguration cConfig : channelConfigs) {
            Channel channel = getThing().getChannel(cConfig.id);
            State state = getState(channelPosts[channelValuesCounter], cConfig.unit);

            // Update the channel
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounter++;
            }
        }

        // Create and update actual values for each channelPostExt
        int channelValuesCounterExt = 0;
        for (SecondGenerationChannelConfiguration cConfig : channelConfigsExt) {
            Channel channel = getThing().getChannel(cConfig.id);
            State state = getState(channelPostsExt[channelValuesCounterExt], cConfig.unit);

            // Update the channel
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounterExt++;
            }
        }

        // Create and update actual values for each channelPostExtExt
        int channelValuesCounterExtExt = 0;
        for (SecondGenerationChannelConfiguration cConfig : channelConfigsExtExt) {
            Channel channel = getThing().getChannel(cConfig.id);
            State state = getState(channelPostsExtExt[channelValuesCounterExtExt], cConfig.unit);

            // Update the channel
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounterExtExt++;
            }
        }
    }

    // Helper method to handleCommand method
    public final void preSetExecuteConfigurationChanges(HttpClient httpClient, String url, String username,
            String password, String dxsEntriesConf, String valueConfiguration) {
        try {
            SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                    dxsEntriesConf, valueConfiguration);
        } catch (Exception handleCommandException) {
            logger.debug("Handle command for {} on channel {}: {}: {}: {}: {}: {}", thing.getUID(), httpClient, url,
                    username, password, dxsEntriesConf, valueConfiguration);
        }
    }

    @SuppressWarnings("null")
    public static String callURL(String myURL) {
        StringBuilder sb = new StringBuilder();
        URLConnection urlConn = null;
        InputStreamReader in = null;
        try {
            URL url = new URL(myURL);
            urlConn = url.openConnection();
            if (urlConn != null) {
                urlConn.setReadTimeout(60 * 1000);
            }
            if (urlConn != null && urlConn.getInputStream() != null) {
                try {
                    in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                    BufferedReader bufferedReader = new BufferedReader(in);
                    if (bufferedReader != null) {
                        int cp;
                        while ((cp = bufferedReader.read()) != -1) {
                            sb.append((char) cp);
                        }
                        bufferedReader.close();
                    }
                } catch (IOException getInputStreamReaderException) {
                    throw new RuntimeException("Exception while calling URL:" + myURL, getInputStreamReaderException);
                }
            }

            in.close();
        } catch (IOException callURLException) {
            throw new RuntimeException("Exception while calling URL:" + myURL, callURLException);

        }
        return sb.toString();
    }

    @SuppressWarnings({ "null", "unused" })
    private State getState(String value, Unit<?> unit) {
        if (unit == null) {
            return new StringType(value);
        } else {
            try {
                return new QuantityType<>(new BigDecimal(value), unit);
            } catch (NumberFormatException getStateException) {
                logger.debug("Error parsing value '{}'", value, getStateException);
                return UnDefType.UNDEF;
            }
        }
    }
}
