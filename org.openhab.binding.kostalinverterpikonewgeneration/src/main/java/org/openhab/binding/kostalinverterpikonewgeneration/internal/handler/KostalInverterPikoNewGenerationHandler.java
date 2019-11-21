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

package org.openhab.binding.kostalinverterpikonewgeneration.internal.handler;

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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
//import org.eclipse.jdt.annotation.NonNullByDefault;
//import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.binding.kostalinverterpikonewgeneration.internal.ChannelConfig;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.DxsEntries;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.DxsEntriesCfg;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.DxsEntriesCfgExt;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.DxsEntriesCfgExtExt;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.DxsEntriesContainer;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.KostalInverterPikoNewGenerationBindingConstants;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.KostalInverterPikoNewGenerationConfiguration;
import org.openhab.binding.kostalinverterpikonewgeneration.internal.configuration.KostalConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link KostalInverterPikoNewGenerationHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 * @author Örjan Backsell - Redesigned for New Generation models
 */
// @NonNullByDefault
public class KostalInverterPikoNewGenerationHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KostalInverterPikoNewGenerationHandler.class);

    // @Nullable
    private KostalInverterPikoNewGenerationConfiguration config;

    @SuppressWarnings("unused")
    // @Nullable
    private KostalInverterPikoNewGenerationBindingConstants configurationConfig;

    private final List<ChannelConfig> channelConfigs = new ArrayList<>();
    private final List<ChannelConfig> channelConfigsExt = new ArrayList<>();
    private final List<ChannelConfig> channelConfigsExtExt = new ArrayList<>();

    public KostalInverterPikoNewGenerationHandler(Thing thing) {
        super(thing);
    }

    // @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String url = config.url.toString();
        String username = config.username;
        String password = config.password;
        String dxsIdConfiguration = "";
        String valueConfiguration = "";

        if (command instanceof RefreshType) {
            logger.debug("Handle command for {} on channel {}: {}", thing.getUID(), channelUID, command);
        }

        if (command == RefreshType.REFRESH) {
            return;
        }

        if (channelUID.getId().equals("batteryType")) {
            dxsIdConfiguration = "33556252";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error("Unexpected error in KostalConfigurationHandler.configurationHandler, batteryType !", e);
            }
        }

        if (channelUID.getId().equals("batteryUsageConsumption")) {
            dxsIdConfiguration = "33556249";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryUsageConsumption  !",
                        e);
            }
        }

        if (channelUID.getId().equals("batteryUsageStrategy")) {
            dxsIdConfiguration = "83888896";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryUsageStrategy !",
                        e);
            }
        }

        if (channelUID.getId().equals("smartBatteryControl")) {
            dxsIdConfiguration = "33556484";
            valueConfiguration = "";
            if (command == OnOffType.ON) {
                valueConfiguration = "True";
            }
            if (command == OnOffType.OFF) {
                valueConfiguration = "False";
            }

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, smartBatteryControl !",
                        e);
            }
        }

        if (channelUID.getId().equals("smartBatteryControl_Text")) {
            dxsIdConfiguration = "33556484";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, smartBatteryControl_Text !",
                        e);
            }
        }

        if (channelUID.getId().equals("batteryChargeTimeFrom")) {
            dxsIdConfiguration = "33556239";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryChargeTimeFrom !",
                        e);
            }
        }

        if (channelUID.getId().equals("batteryChargeTimeTo")) {
            dxsIdConfiguration = "33556240";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, batteryChargeTimeTo !",
                        e);
            }
        }

        if (channelUID.getId().equals("maxDepthOfDischarge")) {
            dxsIdConfiguration = "33556247";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, maxDepthOfDischarge !",
                        e);
            }
        }

        if (channelUID.getId().equals("shadowManagement")) {
            dxsIdConfiguration = "33556483";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error("Unexpected error in KostalConfigurationHandler.configurationHandler, shadowManagement !",
                        e);
            }
        }

        if (channelUID.getId().equals("externalModuleControl")) {
            dxsIdConfiguration = "33556482";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error(
                        "Unexpected error in KostalConfigurationHandler.configurationHandler, externalModuleControl !",
                        e);
            }
        }

        if (channelUID.getId().equals("inverterName")) {
            dxsIdConfiguration = "16777984";
            valueConfiguration = command.toString();

            try {
                KostalConfigurationHandler.configurationHandler(url, username, password, dxsIdConfiguration,
                        valueConfiguration);
            } catch (Exception e) {
                logger.error("Unexpected error in KostalConfigurationHandler.configurationHandler, inverterName !", e);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(KostalInverterPikoNewGenerationConfiguration.class);

        scheduler.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getClass().getName() + ":" + e.getMessage());
                    logger.debug("Error refreshing source = {}", getThing().getUID(), e);
                }
            }

        }, 0, KostalInverterPikoNewGenerationConfiguration.REFRESHINTERVAL, TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);

        channelConfigs.add(new ChannelConfig("gridOutputPower", "td", 4, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("yield_Day", "td", 7, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("yield_Total", "td", 10, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigs.add(new ChannelConfig("operatingStatus", "td", 13, null));
        channelConfigs.add(new ChannelConfig("gridVoltageL1", "td", 16, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("gridCurrentL1", "td", 19, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("gridPowerL1", "td", 22, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("gridVoltageL2", "td", 25, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("gridCurrentL2", "td", 28, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("gridPowerL2", "td", 31, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("gridVoltageL3", "td", 34, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("gridCurrentL3", "td", 37, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("gridPowerL3", "td", 40, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("dcPowerPV", "td", 43, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("dc1Voltage", "td", 46, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("dc1Current", "td", 49, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("dc1Power", "td", 52, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("dc2Voltage", "td", 55, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("dc2Current", "td", 58, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("dc2Power", "td", 61, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("dc3Voltage", "td", 64, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("dc3Current", "td", 67, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("dc3Power", "td", 70, SmartHomeUnits.WATT));

        channelConfigsExt.add(new ChannelConfig("aktHomeConsumptionSolar", "td", 73, SmartHomeUnits.WATT));
        channelConfigsExt.add(new ChannelConfig("aktHomeConsumptionBat", "td", 76, SmartHomeUnits.WATT));
        channelConfigsExt.add(new ChannelConfig("aktHomeConsumptionGrid", "td", 79, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new ChannelConfig("phaseSelHomeConsumpL1", "td", 82, SmartHomeUnits.WATT));
        channelConfigsExt.add(new ChannelConfig("phaseSelHomeConsumpL2", "td", 85, SmartHomeUnits.WATT));
        channelConfigsExt.add(new ChannelConfig("phaseSelHomeConsumpL3", "td", 88, SmartHomeUnits.WATT));
        channelConfigsExt.add(new ChannelConfig("gridFreq", "td", 91, SmartHomeUnits.HERTZ));
        channelConfigsExt.add(new ChannelConfig("gridCosPhi", "td", 94, SmartHomeUnits.DEGREE_ANGLE));
        channelConfigsExt.add(new ChannelConfig("homeConsumption_Day", "td", 97, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new ChannelConfig("ownConsumption_Day", "td", 100, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new ChannelConfig("ownConsRate_Day", "td", 103, SmartHomeUnits.PERCENT));
        channelConfigsExt.add(new ChannelConfig("autonomyDegree_Day", "td", 106, SmartHomeUnits.PERCENT));
        channelConfigsExt.add(new ChannelConfig("homeConsumption_Total", "td", 109, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new ChannelConfig("ownConsumption_Total", "td", 112, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigsExt.add(new ChannelConfig("totalOperatingTime", "td", 115, SmartHomeUnits.HOUR));
        channelConfigsExt.add(new ChannelConfig("current", "td", 118, SmartHomeUnits.AMPERE));
        channelConfigsExt.add(new ChannelConfig("currentDir", "td", 121, SmartHomeUnits.AMPERE));
        channelConfigsExt.add(new ChannelConfig("chargeCycles", "td", 124, null));
        channelConfigsExt.add(new ChannelConfig("batteryTemperature", "td", 127, SIUnits.CELSIUS));
        channelConfigsExt.add(new ChannelConfig("loginterval", "td", 130, SmartHomeUnits.MINUTE));
        channelConfigsExt.add(new ChannelConfig("s0InPulseCnt", "td", 133, null));
        channelConfigsExt.add(new ChannelConfig("ownConsRate_Total", "td", 136, SmartHomeUnits.PERCENT));
        channelConfigsExt.add(new ChannelConfig("autonomyDegree_Total", "td", 139, SmartHomeUnits.PERCENT));

        channelConfigsExtExt.add(new ChannelConfig("batteryVoltage", "td", 142, SmartHomeUnits.VOLT));
        channelConfigsExtExt.add(new ChannelConfig("batStateOfCharge", "td", 145, SmartHomeUnits.PERCENT));
    }

    @SuppressWarnings("null")
    private void refresh() throws Exception {
        String dxsEntriesConfigFile = config.dxsEntriesCfgFile;
        String dxsEntriesConfigFileExt = config.dxsEntriesCfgFileExt;
        String dxsEntriesConfigFileExtExt = config.dxsEntriesCfgFileExtExt;

        // Create dxsEntries arrays
        String[] dxsEntries = new String[22];
        String[] dxsEntriesExt = new String[22];
        String[] dxsEntriesExtExt = new String[2];

        // Fill dxsEntries with actual values
        dxsEntries = DxsEntriesCfg.getDxsEntriesCfg(dxsEntriesConfigFile);
        dxsEntriesExt = DxsEntriesCfgExt.getDxsEntriesCfgExt(dxsEntriesConfigFileExt);
        dxsEntriesExtExt = DxsEntriesCfgExtExt.getDxsEntriesCfgExtExt(dxsEntriesConfigFileExtExt);

        // Catch data from actual DxsEntries
        String jsonDxsEntriesResponse = callURL(config.url.toString() + "/api/dxs.json?dxsEntries=" + dxsEntries[0]
                + "&dxsEntries=" + dxsEntries[1] + "&dxsEntries=" + dxsEntries[2] + "&dxsEntries=" + dxsEntries[3]
                + "&dxsEntries=" + dxsEntries[4] + "&dxsEntries=" + dxsEntries[5] + "&dxsEntries=" + dxsEntries[6]
                + "&dxsEntries=" + dxsEntries[7] + "&dxsEntries=" + dxsEntries[8] + "&dxsEntries=" + dxsEntries[9]
                + "&dxsEntries=" + dxsEntries[10] + "&dxsEntries=" + dxsEntries[11] + "&dxsEntries=" + dxsEntries[12]
                + "&dxsEntries=" + dxsEntries[13] + "&dxsEntries=" + dxsEntries[14] + "&dxsEntries=" + dxsEntries[15]
                + "&dxsEntries=" + dxsEntries[16] + "&dxsEntries=" + dxsEntries[17] + "&dxsEntries=" + dxsEntries[18]
                + "&dxsEntries=" + dxsEntries[19] + "&dxsEntries=" + dxsEntries[20] + "&dxsEntries=" + dxsEntries[21]
                + "&dxsEntries=" + dxsEntries[22]);

        String jsonDxsEntriesResponseExt = callURL(config.url.toString() + "/api/dxs.json?dxsEntries="
                + dxsEntriesExt[0] + "&dxsEntries=" + dxsEntriesExt[1] + "&dxsEntries=" + dxsEntriesExt[2]
                + "&dxsEntries=" + dxsEntriesExt[3] + "&dxsEntries=" + dxsEntriesExt[4] + "&dxsEntries="
                + dxsEntriesExt[5] + "&dxsEntries=" + dxsEntriesExt[6] + "&dxsEntries=" + dxsEntriesExt[7]
                + "&dxsEntries=" + dxsEntriesExt[8] + "&dxsEntries=" + dxsEntriesExt[9] + "&dxsEntries="
                + dxsEntriesExt[10] + "&dxsEntries=" + dxsEntriesExt[11] + "&dxsEntries=" + dxsEntriesExt[12]
                + "&dxsEntries=" + dxsEntriesExt[13] + "&dxsEntries=" + dxsEntriesExt[14] + "&dxsEntries="
                + dxsEntriesExt[15] + "&dxsEntries=" + dxsEntriesExt[16] + "&dxsEntries=" + dxsEntriesExt[17]
                + "&dxsEntries=" + dxsEntriesExt[18] + "&dxsEntries=" + dxsEntriesExt[19] + "&dxsEntries="
                + dxsEntriesExt[20] + "&dxsEntries=" + dxsEntriesExt[21] + "&dxsEntries=" + dxsEntriesExt[22]);

        String jsonDxsEntriesResponseExtExt = callURL(config.url.toString() + "/api/dxs.json?dxsEntries="
                + dxsEntriesExtExt[0] + "&dxsEntries=" + dxsEntriesExtExt[1]);

        // Get Gson object
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Parse result
        DxsEntriesContainer dxsEntriesContainer = gson.fromJson(jsonDxsEntriesResponse, DxsEntriesContainer.class);
        DxsEntriesContainer dxsEntriesContainerExt = gson.fromJson(jsonDxsEntriesResponseExt,
                DxsEntriesContainer.class);
        DxsEntriesContainer dxsEntriesContainerExtExt = gson.fromJson(jsonDxsEntriesResponseExtExt,
                DxsEntriesContainer.class);

        // Create channel-posts array's
        String[] channelPosts = new String[23];
        String[] channelPostsExt = new String[23];
        String[] channelPostsExtExt = new String[2];

        // Fill channelPosts with each item value
        int channelPostsCounter = 0;
        for (DxsEntries dxsentries : dxsEntriesContainer.dxsEntries) {
            channelPosts[channelPostsCounter] = dxsentries.getName();
            channelPostsCounter++;
        }

        // Fill channelPostsExt with each item value
        int channelPostsCounterExt = 0;
        for (DxsEntries dxsentriesExt : dxsEntriesContainerExt.dxsEntries) {
            channelPostsExt[channelPostsCounterExt] = dxsentriesExt.getName();
            channelPostsCounterExt++;
        }

        // Fill channelPostsExtExt with each item value
        int channelPostsCounterExtExt = 0;
        for (DxsEntries dxsentriesExtExt : dxsEntriesContainerExtExt.dxsEntries) {
            channelPostsExtExt[channelPostsCounterExtExt] = dxsentriesExtExt.getName();
            channelPostsCounterExtExt++;
        }

        // Create and update actual values for each channelPost
        int channelValuesCounter = 0;
        for (ChannelConfig cConfig : channelConfigs) {
            Channel channel = getThing().getChannel(cConfig.getId());
            State state = getState(channelPosts[channelValuesCounter], cConfig.unit);

            // Update the channel
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounter++;
            }
        }

        // Create and update actual values for each channelPostExt
        int channelValuesCounterExt = 0;
        for (ChannelConfig cConfig : channelConfigsExt) {
            Channel channel = getThing().getChannel(cConfig.getId());
            State state = getState(channelPostsExt[channelValuesCounterExt], cConfig.unit);

            // Update the channel
            if (state != null) {
                updateState(channel.getUID().getId(), state);
                channelValuesCounterExt++;
            }
        }

        // Create and update actual values for each channelPostExtExt
        int channelValuesCounterExtExt = 0;
        for (ChannelConfig cConfig : channelConfigsExtExt) {
            Channel channel = getThing().getChannel(cConfig.getId());
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
