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
package org.openhab.binding.kostalinverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Christian Schneider - Initial contribution
 * @author Örjan Backsell - Added New Generation
 */
public class WebscrapeHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(WebscrapeHandler.class);
    private SourceConfig config;
    private List<ChannelConfig> channelConfigs;

    public WebscrapeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(SourceConfig.class);
        scheduler.scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    refresh();
                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            e.getClass().getName() + ":" + e.getMessage());
                    // logger.debug("Error refreshing source " + getThing().getUID(), e);
                    logger.debug("Error refreshing source = {}", getThing().getUID(), e);
                }
            }

        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    @SuppressWarnings("null")
    private void refresh() throws Exception {
        // This part applies to Kostal Proven Generation
        if (config.type.equals("proven_generation")) {
            Document doc = getDoc();
            for (ChannelConfig cConfig : channelConfigs) {
                String value = getTag(doc, cConfig.tag).get(cConfig.num);
                Channel channel = getThing().getChannel(cConfig.id);
                State state = getState(value);
                updateState(channel.getUID(), state);
            }
        }
        // End of Kostal Proven Generation part

        // This part applies to Kostal New Generation
        if (config.type.equals("new_generation")) {
            channelConfigs = new ArrayList<>();

            channelConfigs.add(new ChannelConfig("dxsEntries_0", "td", 4));
            channelConfigs.add(new ChannelConfig("dxsEntries_1", "td", 7));
            channelConfigs.add(new ChannelConfig("dxsEntries_2", "td", 10));
            channelConfigs.add(new ChannelConfig("dxsEntries_3", "td", 16));
            channelConfigs.add(new ChannelConfig("dxsEntries_4", "td", 19));
            channelConfigs.add(new ChannelConfig("dxsEntries_5", "td", 22));
            channelConfigs.add(new ChannelConfig("dxsEntries_6", "td", 25));
            channelConfigs.add(new ChannelConfig("dxsEntries_7", "td", 28));
            channelConfigs.add(new ChannelConfig("dxsEntries_8", "td", 31));
            channelConfigs.add(new ChannelConfig("dxsEntries_9", "td", 34));
            channelConfigs.add(new ChannelConfig("dxsEntries_10", "td", 37));
            channelConfigs.add(new ChannelConfig("dxsEntries_11", "td", 40));
            channelConfigs.add(new ChannelConfig("dxsEntries_12", "td", 43));
            channelConfigs.add(new ChannelConfig("dxsEntries_13", "td", 46));
            channelConfigs.add(new ChannelConfig("dxsEntries_14", "td", 49));
            channelConfigs.add(new ChannelConfig("dxsEntries_15", "td", 52));
            channelConfigs.add(new ChannelConfig("dxsEntries_16", "td", 55));
            channelConfigs.add(new ChannelConfig("dxsEntries_17", "td", 58));
            channelConfigs.add(new ChannelConfig("dxsEntries_18", "td", 61));
            channelConfigs.add(new ChannelConfig("dxsEntries_19", "td", 64));
            channelConfigs.add(new ChannelConfig("dxsEntries_20", "td", 67));
            channelConfigs.add(new ChannelConfig("dxsEntries_21", "td", 13));
            channelConfigs.add(new ChannelConfig("dxsEntries_22", "td", 121));

            String dxsEntriesConfigFile = config.dxsEntriesCfgFile;

            // Create an dxsEntries array
            String[] dxsEntries = new String[23];

            // Fill dxsEntries with actual values
            dxsEntries = DxsEntriesCfg.getDxsEntriesCfg(dxsEntriesConfigFile);

            // Catch info from actual DxsEntries
            String jsonDxsEntriesResponse = callURL(config.url.toString() + "/api/dxs.json?dxsEntries=" + dxsEntries[0]
                    + "&dxsEntries=" + dxsEntries[1] + "&dxsEntries=" + dxsEntries[2] + "&dxsEntries=" + dxsEntries[3]
                    + "&dxsEntries=" + dxsEntries[4] + "&dxsEntries=" + dxsEntries[5] + "&dxsEntries=" + dxsEntries[6]
                    + "&dxsEntries=" + dxsEntries[7] + "&dxsEntries=" + dxsEntries[8] + "&dxsEntries=" + dxsEntries[9]
                    + "&dxsEntries=" + dxsEntries[10] + "&dxsEntries=" + dxsEntries[11] + "&dxsEntries="
                    + dxsEntries[12] + "&dxsEntries=" + dxsEntries[13] + "&dxsEntries=" + dxsEntries[14]
                    + "&dxsEntries=" + dxsEntries[15] + "&dxsEntries=" + dxsEntries[16] + "&dxsEntries="
                    + dxsEntries[17] + "&dxsEntries=" + dxsEntries[18] + "&dxsEntries=" + dxsEntries[19]
                    + "&dxsEntries=" + dxsEntries[20] + "&dxsEntries=" + dxsEntries[21] + "&dxsEntries="
                    + dxsEntries[22]);

            // Get Gson object
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // Parse result
            DxsEntriesContainer dxsentriescontainer = gson.fromJson(jsonDxsEntriesResponse, DxsEntriesContainer.class);

            // Create an channel-posts array
            String[] channelPosts = new String[23];

            // Fill channel-posts with each item value
            int channelPostsCounter = 0;
            for (DxsEntries dxsentries : dxsentriescontainer.dxsEntries) {
                channelPosts[channelPostsCounter] = dxsentries.getName();
                channelPostsCounter++;
            }

            // Create and send actual values for each channel-post

            int channelValuesCounter = 0;
            for (ChannelConfig cConfig : channelConfigs) {
                Channel channel = getThing().getChannel(cConfig.id);
                State state = getState(channelPosts[channelValuesCounter]);
                updateState(channel.getUID(), state);
                channelValuesCounter++;

            }
        }
        // End of Kostal New Generation part
    }

    private static List<String> getTag(Document doc, String tag) {
        ArrayList<String> result = new ArrayList<String>();
        Iterator<Element> elIt = ((Element) doc).getElementsByTag(tag).iterator();
        while (elIt.hasNext()) {
            String content = elIt.next().text();
            content = content.replace("\u00A0", "").trim();
            if (!content.isEmpty()) {
                result.add(content);
            }
        }
        return result;
    }

    private Document getDoc() throws IOException {
        String login = config.userName + ":" + config.password;
        String base64login = new String(Base64.getEncoder().encode(login.getBytes()));
        return Jsoup.connect(config.url).header("Authorization", "Basic " + base64login).get();
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

    private State getState(String value) {
        try {
            return new DecimalType(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return new StringType(value);
        }
    }
}
