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
package org.openhab.binding.internal.kostal.inverter.firstgeneration;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 */
public class WebscrapeHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(WebscrapeHandler.class);
    private SourceConfig config;

    private final List<ChannelConfig> channelConfigs = new ArrayList<>();

    public WebscrapeHandler(Thing thing) {
        super(thing);
        channelConfigs.add(new ChannelConfig("acPower", "td", 4, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("totalEnergy", "td", 7, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigs.add(new ChannelConfig("dayEnergy", "td", 10, SmartHomeUnits.KILOWATT_HOUR));
        channelConfigs.add(new ChannelConfig("status", "td", 13, null));
        channelConfigs.add(new ChannelConfig("str1Voltage", "td", 19, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("str1Current", "td", 25, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("str2Voltage", "td", 33, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("str2Current", "td", 39, SmartHomeUnits.AMPERE));
        channelConfigs.add(new ChannelConfig("l1Voltage", "td", 22, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("l1Power", "td", 28, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("l2Voltage", "td", 36, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("l2Power", "td", 42, SmartHomeUnits.WATT));
        channelConfigs.add(new ChannelConfig("l3Voltage", "td", 46, SmartHomeUnits.VOLT));
        channelConfigs.add(new ChannelConfig("l3Power", "td", 49, SmartHomeUnits.WATT));
    }

    @Override
    public void initialize() {
        config = getConfigAs(SourceConfig.class);
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                refresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.debug("Error refreshing source '{}'", getThing().getUID(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        e.getClass().getName() + ":" + e.getMessage());
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
            Channel channel = getThing().getChannel(cConfig.id);
            if (channel != null) {
                String value = getTag(doc, cConfig.tag).get(cConfig.num);
                updateState(channel.getUID(), getState(value, cConfig.unit));
            }
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
