/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
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
 * sent to one of the channels, regarded to second generation part of the binding.
 *
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 * @author Örjan Backsell - Redesigned regarding Piko1020, Piko New Generation
 */
// @NonNullByDefault
public class SecondGenerationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SecondGenerationHandler.class);

    private HttpClient httpClient;

    // @Nullable
    private SecondGenerationConfiguration config;

    @SuppressWarnings("unused")
    // @Nullable
    private SecondGenerationBindingConstants configurationConfig;

    private final List<SecondGenerationChannelConfig> channelConfigs = new ArrayList<>();
    private final List<SecondGenerationChannelConfig> channelConfigsExt = new ArrayList<>();
    private final List<SecondGenerationChannelConfig> channelConfigsExtExt = new ArrayList<>();

    public SecondGenerationHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;

    }

    // @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String url = config.url.toString();
        String username = config.username;
        String password = config.password;
        String valueConfiguration = "";

        if (command instanceof RefreshType) {
            logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);
        }

        if (command == RefreshType.REFRESH) {
            return;
        }

        if (channelUID.getId().equals("chargeTimeEnd")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556236", valueConfiguration);
            } catch (Exception e) {
                logger.debug("Unexpected error in KostalConfigurationHandler.configurationHandler, chargeTimeEnd !", e);
            }
        }

        if (channelUID.getId().equals("batteryType")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556252", valueConfiguration);
            } catch (Exception e) {
                logger.debug("Unexpected error in KostalConfigurationHandler.configurationHandler, batteryType !", e);
            }
        }

        if (channelUID.getId().equals("batteryUsageConsumption")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556249", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryUsageConsumption  !",
                        e);
            }
        }

        if (channelUID.getId().equals("batteryUsageStrategy")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "83888896", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryUsageStrategy !",
                        e);
            }
        }

        if (channelUID.getId().equals("smartBatteryControl")) {
            valueConfiguration = "";
            if (command == OnOffType.ON) {
                valueConfiguration = "True";
            }
            if (command == OnOffType.OFF) {
                valueConfiguration = "False";
            }

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556484", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, smartBatteryControl !",
                        e);
            }
        }

        if (channelUID.getId().equals("smartBatteryControlText")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556484", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, smartBatteryControlText !",
                        e);
            }
        }

        if (channelUID.getId().equals("batteryChargeTimeFrom")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556239", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryChargeTimeFrom !",
                        e);
            }
        }

        if (channelUID.getId().equals("batteryChargeTimeTo")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556240", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryChargeTimeTo !",
                        e);
            }
        }

        if (channelUID.getId().equals("maxDepthOfDischarge")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556247", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, maxDepthOfDischarge !",
                        e);
            }
        }

        if (channelUID.getId().equals("shadowManagement")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556483", valueConfiguration);
            } catch (Exception e) {
                logger.debug("Unexpected error in KostalConfigurationHandler.configurationHandler, shadowManagement !",
                        e);
            }
        }

        if (channelUID.getId().equals("externalModuleControl")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "33556482", valueConfiguration);
            } catch (Exception e) {
                logger.debug(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, externalModuleControl !",
                        e);
            }
        }

        if (channelUID.getId().equals("inverterName")) {
            valueConfiguration = command.toString();

            try {
                SecondGenerationConfigurationHandler.executeConfigurationChanges(httpClient, url, username, password,
                        "16777984", valueConfiguration);
            } catch (Exception e) {
                logger.debug("Unexpected error in KostalConfigurationHandler.configurationHandler, inverterName !", e);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SecondGenerationConfiguration.class);

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                refresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        e.getClass().getName() + ":" + e.getMessage());
                logger.debug("Error refreshing source = {}", getThing().getUID(), e);
            }
        }, 0, SecondGenerationConfiguration.REFRESHINTERVAL, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);

        channelConfigs.add(new SecondGenerationChannelConfig("gridOutputPower", "td", 4, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("yieldDay", "td", 7, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("yieldTotal", "td", 10, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigs.add(new SecondGenerationChannelConfig("operatingStatus", "td", 13, null));
        channelConfigs.add(new SecondGenerationChannelConfig("gridVoltageL1", "td", 16, SmartHomeUnits.VOLT));
        channelConfigs.add(new SecondGenerationChannelConfig("gridCurrentL1", "td", 19, SmartHomeUnits.AMPERE));
        channelConfigs.add(new SecondGenerationChannelConfig("gridPowerL1", "td", 22, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("gridVoltageL2", "td", 25, SmartHomeUnits.VOLT));
        channelConfigs.add(new SecondGenerationChannelConfig("gridCurrentL2", "td", 28, SmartHomeUnits.AMPERE));
        channelConfigs.add(new SecondGenerationChannelConfig("gridPowerL2", "td", 31, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("gridVoltageL3", "td", 34, SmartHomeUnits.VOLT));
        channelConfigs.add(new SecondGenerationChannelConfig("gridCurrentL3", "td", 37, SmartHomeUnits.AMPERE));
        channelConfigs.add(new SecondGenerationChannelConfig("gridPowerL3", "td", 40, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("dcPowerPV", "td", 43, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("dc1Voltage", "td", 46, SmartHomeUnits.VOLT));
        channelConfigs.add(new SecondGenerationChannelConfig("dc1Current", "td", 49, SmartHomeUnits.AMPERE));
        channelConfigs.add(new SecondGenerationChannelConfig("dc1Power", "td", 52, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("dc2Voltage", "td", 55, SmartHomeUnits.VOLT));
        channelConfigs.add(new SecondGenerationChannelConfig("dc2Current", "td", 58, SmartHomeUnits.AMPERE));
        channelConfigs.add(new SecondGenerationChannelConfig("dc2Power", "td", 61, SmartHomeUnits.WATT));
        channelConfigs.add(new SecondGenerationChannelConfig("dc3Voltage", "td", 64, SmartHomeUnits.VOLT));
        channelConfigs.add(new SecondGenerationChannelConfig("dc3Current", "td", 67, SmartHomeUnits.AMPERE));
        channelConfigs.add(new SecondGenerationChannelConfig("dc3Power", "td", 70, SmartHomeUnits.WATT));

        channelConfigsExt
                .add(new SecondGenerationChannelConfig("aktHomeConsumptionSolar", "td", 73, SmartHomeUnits.WATT));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("aktHomeConsumptionBat", "td", 76, SmartHomeUnits.WATT));
        channelConfigsExt.add(
                new SecondGenerationChannelConfig("aktHomeConsumptionGrid", "td", 79, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("phaseSelHomeConsumpL1", "td", 82, SmartHomeUnits.WATT));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("phaseSelHomeConsumpL2", "td", 85, SmartHomeUnits.WATT));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("phaseSelHomeConsumpL3", "td", 88, SmartHomeUnits.WATT));
        channelConfigsExt.add(new SecondGenerationChannelConfig("gridFreq", "td", 91, SmartHomeUnits.HERTZ));
        channelConfigsExt.add(new SecondGenerationChannelConfig("gridCosPhi", "td", 94, SmartHomeUnits.DEGREE_ANGLE));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("homeConsumptionDay", "td", 97, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("ownConsumptionDay", "td", 100, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new SecondGenerationChannelConfig("ownConsRateDay", "td", 103, SmartHomeUnits.PERCENT));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("autonomyDegreeDay", "td", 106, SmartHomeUnits.PERCENT));
        channelConfigsExt.add(
                new SecondGenerationChannelConfig("homeConsumptionTotal", "td", 109, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("ownConsumptionTotal", "td", 112, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new SecondGenerationChannelConfig("totalOperatingTime", "td", 115, SmartHomeUnits.HOUR));
        channelConfigsExt.add(new SecondGenerationChannelConfig("current", "td", 118, SmartHomeUnits.AMPERE));
        channelConfigsExt.add(new SecondGenerationChannelConfig("currentDir", "td", 121, SmartHomeUnits.AMPERE));
        channelConfigsExt.add(new SecondGenerationChannelConfig("chargeCycles", "td", 124, null));
        channelConfigsExt.add(new SecondGenerationChannelConfig("batteryTemperature", "td", 127, SIUnits.CELSIUS));
        channelConfigsExt.add(new SecondGenerationChannelConfig("loginterval", "td", 130, SmartHomeUnits.MINUTE));
        channelConfigsExt.add(new SecondGenerationChannelConfig("s0InPulseCnt", "td", 133, null));
        channelConfigsExt.add(new SecondGenerationChannelConfig("ownConsRateTotal", "td", 136, SmartHomeUnits.PERCENT));
        channelConfigsExt
                .add(new SecondGenerationChannelConfig("autonomyDegreeTotal", "td", 139, SmartHomeUnits.PERCENT));

        channelConfigsExtExt.add(new SecondGenerationChannelConfig("batteryVoltage", "td", 142, SmartHomeUnits.VOLT));
        channelConfigsExtExt
                .add(new SecondGenerationChannelConfig("batStateOfCharge", "td", 145, SmartHomeUnits.PERCENT));
        channelConfigsExtExt
                .add(new SecondGenerationChannelConfig("selfConsumption", "td", 148, SmartHomeUnits.KILOWATT_HOUR));
    }

    @SuppressWarnings("null")
    private void refresh() throws Exception {

        // Create dxsEntries arrays
        String[] dxsEntries = new String[22];
        String[] dxsEntriesExt = new String[22];
        String[] dxsEntriesExtExt = new String[2];

        // Fill dxsEntries with actual values
        dxsEntries = SecondGenerationDxsEntriesCfg.getDxsEntriesCfg();
        dxsEntriesExt = SecondGenerationDxsEntriesCfgExt.getDxsEntriesCfgExt();
        dxsEntriesExtExt = SecondGenerationDxsEntriesCfgExtExt.getDxsEntriesCfgExtExt();

        String dxsEntriesCall = config.url.toString() + "/api/dxs.json?dxsEntries=" + dxsEntries[0];
        String dxsEntriesCallExt = config.url.toString() + "/api/dxs.json?dxsEntries=" + dxsEntriesExt[0];

        for (int i = 1; i < 23; i++) {
            dxsEntriesCall += ("&dxsEntries=" + dxsEntries[i]);
            dxsEntriesCallExt += ("&dxsEntries=" + dxsEntriesExt[i]);
        }

        String jsonDxsEntriesResponse = callURL(dxsEntriesCall);
        String jsonDxsEntriesResponseExt = callURL(dxsEntriesCallExt);

        String jsonDxsEntriesResponseExtExt = callURL(config.url.toString() + "/api/dxs.json?dxsEntries="
                + dxsEntriesExtExt[0] + "&dxsEntries=" + dxsEntriesExtExt[1] + "&dxsEntries=" + dxsEntriesExtExt[2]);

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
        for (SecondGenerationChannelConfig cConfig : channelConfigs) {
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
        for (SecondGenerationChannelConfig cConfig : channelConfigsExt) {
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
        for (SecondGenerationChannelConfig cConfig : channelConfigsExtExt) {
            Channel channel = getThing().getChannel(cConfig.id);
            State state = getState(channelPostsExtExt[channelValuesCounterExtExt], cConfig.unit);

            // Update the channel
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounterExtExt++;
            }
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
                in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
            }
            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception while calling URL:" + myURL, e);
        }
        return sb.toString();
    }

    private State getState(String value, Unit<?> unit) {
        if (unit == null) {
            return new StringType(value);
        } else {
            try {
                return new QuantityType<>(new BigDecimal(value), unit);
            } catch (NumberFormatException e) {
                logger.debug("Error parsing value '{}'", value, e);
                return UnDefType.UNDEF;
            }
        }
    }
}
