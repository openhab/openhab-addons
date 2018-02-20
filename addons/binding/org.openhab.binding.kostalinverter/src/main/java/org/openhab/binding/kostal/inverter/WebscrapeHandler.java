/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostal.inverter;

import java.io.IOException;
import java.math.BigDecimal;
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

/**
 * @author Christian Schneider
 */
public class WebscrapeHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(WebscrapeHandler.class);
    private SourceConfig config;
    private List<ChannelConfig> channelConfigs;

    public WebscrapeHandler(Thing thing) {
        super(thing);
        channelConfigs = new ArrayList<>();
        channelConfigs.add(new ChannelConfig("acPower", "td", 4));
        channelConfigs.add(new ChannelConfig("totalEnergy", "td", 7));
        channelConfigs.add(new ChannelConfig("dayEnergy", "td", 10));
        channelConfigs.add(new ChannelConfig("status", "td", 13));
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
                    logger.debug("Error refreshing source {} ", getThing().getUID(), e);
                }
            }

        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read only
    }

    private void refresh() throws Exception {
        Document doc = getDoc();
        for (ChannelConfig cConfig : channelConfigs) {
            String value = getTag(doc, cConfig.tag).get(cConfig.num);
            Channel channel = getThing().getChannel(cConfig.id);
            State state = getState(value);
            updateState(channel.getUID(), state);
        }
    }

    private static List<String> getTag(Document doc, String tag) {
        ArrayList<String> result = new ArrayList<String>();
        Iterator<Element> elIt = doc.getElementsByTag(tag).iterator();
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

    private State getState(String value) {
        try {
            return new DecimalType(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return new StringType(value);
        }
    }

}
