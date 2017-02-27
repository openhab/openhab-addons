/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.oceanic.handler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.binding.oceanic.OceanicBindingConstants.OceanicChannelSelector;
import org.openhab.binding.oceanic.internal.SerialPortThrottler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OceanicHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class OceanicThingHandler extends SerialThingHandler {

    // List of Configuration constants
    public static final String INTERVAL = "interval";

    private Logger logger = LoggerFactory.getLogger(OceanicThingHandler.class);

    private ScheduledFuture<?> pollingJob;
    private String lastLineReceived = "";
    private long GRACE_PERIOD = 1000;
    private long REQUEST_TIMEOUT = 15000;
    private long previousRequestTypeCommand = 0;

    public OceanicThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Oceanic handler.");
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Oceanic handler.");

        if (getConfig().get(BAUD_RATE) == null) {
            baud = 19200;
        } else {
            baud = (int) getConfig().get(BAUD_RATE);
        }

        if (getConfig().get(BUFFER_SIZE) == null) {
            bufferSize = 1024;
        } else {
            bufferSize = (int) getConfig().get(BUFFER_SIZE);
        }

        port = (String) getConfig().get(PORT);

        sleep = 250;

        super.initialize();

        onUpdate();
    }

    private synchronized void onUpdate() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            int polling_interval = ((BigDecimal) getConfig().get(INTERVAL)).intValue();
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1, polling_interval, TimeUnit.SECONDS);
        }

        // Read out the properties
        try {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                for (Channel aChannel : getThing().getChannels()) {
                    try {
                        OceanicChannelSelector selector = OceanicChannelSelector.getValueSelector(
                                aChannel.getUID().getId(), OceanicChannelSelector.ValueSelectorType.GET);

                        if (selector != null && selector.isProperty()) {
                            scheduler.schedule(new RequestRunnable(aChannel.getUID(), selector), 0,
                                    TimeUnit.MILLISECONDS);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while reading the properties the Oceanic Water Softener: '{}'",
                    e.getMessage());
        }
    }

    @Override
    public void onDataReceived(String line) {

        logger.trace("Received '{}'", line);

        line = StringUtils.chomp(line);

        // little hack to overcome Locale limits of the Oceanic device
        line = line.replace(",", ".");
        line = line.trim();

        lastLineReceived = line;
    }

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                if (getThing().getStatus() == ThingStatus.ONLINE) {

                    long interval = (((BigDecimal) getConfig().get(INTERVAL)).intValue() * 1000)
                            / getThing().getChannels().size();
                    logger.trace("Sending a request every {} milliseconds", interval);
                    long delay = 0;

                    for (Channel aChannel : getThing().getChannels()) {
                        try {
                            OceanicChannelSelector selector = OceanicChannelSelector.getValueSelector(
                                    aChannel.getUID().getId(), OceanicChannelSelector.ValueSelectorType.GET);

                            if (selector != null) {
                                scheduler.schedule(new RequestRunnable(aChannel.getUID(), selector), delay,
                                        TimeUnit.MILLISECONDS);
                                delay = delay + interval;
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while polling the Oceanic Water Softener: '{}'", e.getMessage());
            }
        }
    };

    private class RequestRunnable implements Runnable {

        private OceanicChannelSelector selector;
        private ChannelUID channelUID;

        public RequestRunnable(ChannelUID theChannelUID, OceanicChannelSelector selector) {
            this.selector = selector;
            this.channelUID = theChannelUID;
        }

        @Override
        public void run() {
            try {
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    logger.trace("Requesting a response for {}", selector.name());
                    String response = requestResponse(selector.name());
                    if (response != "") {
                        if (selector.isProperty()) {
                            logger.debug("Updating the property '{}' with value '{}'", selector.toString(),
                                    selector.convertValue(response));
                            Map<String, String> properties = editProperties();
                            properties.put(selector.toString(), selector.convertValue(response));
                            updateProperties(properties);
                        } else {
                            State value = createStateForType(selector, response);
                            updateState(channelUID, value);
                        }
                    } else {
                        logger.warn("Received an empty answer for '{}'", selector.name());
                    }
                }
            } catch (Exception e) {
                logger.error("An exception occurred while requesting '{}' from the Oceanic Water Softener: '{}'",
                        selector.toString(), e.getMessage());
            }
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (getThing().getStatus() == ThingStatus.ONLINE) {

            if (command instanceof RefreshType) {

                String channelID = channelUID.getId();
                OceanicChannelSelector selector = OceanicChannelSelector.getValueSelector(channelID,
                        OceanicChannelSelector.ValueSelectorType.GET);

                if (selector != null) {
                    scheduler.schedule(new RequestRunnable(channelUID, selector), 0, TimeUnit.MILLISECONDS);
                }

                return;
            }

            String commandAsString = command.toString();
            String channelID = channelUID.getId();

            for (Channel aChannel : getThing().getChannels()) {
                if (aChannel.getUID().equals(channelUID)) {
                    try {
                        OceanicChannelSelector selector = OceanicChannelSelector.getValueSelector(channelID,
                                OceanicChannelSelector.ValueSelectorType.SET);

                        switch (selector) {
                            case setSV1:
                                commandAsString = selector.name() + commandAsString;
                                break;
                            default:
                                commandAsString = selector.name();
                                break;
                        }
                        String response = requestResponse(commandAsString);
                        if (response.equals("ERR")) {
                            logger.error("An error occurred while setting '{}' to {}", selector.toString(),
                                    commandAsString);
                        }
                    } catch (IllegalArgumentException e) {
                        logger.warn(
                                "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                                channelID, command.toString());
                    }

                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private State createStateForType(OceanicChannelSelector selector, String value) {

        Class<? extends Type> typeClass = selector.getTypeClass();
        List<Class<? extends State>> stateTypeList = new ArrayList<Class<? extends State>>();

        stateTypeList.add((Class<? extends State>) typeClass);

        State state = TypeParser.parseState(stateTypeList, selector.convertValue(value));

        return state;
    }

    private String requestResponse(String commandAsString) {

        SerialPortThrottler.lock(port);

        String response = null;
        try {
            lastLineReceived = "";
            writeString(commandAsString + "\r");
            long timeStamp = System.currentTimeMillis();
            while (lastLineReceived.equals("")) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    logger.error("An exception occurred while putting the thread to sleep: {}", e.getMessage());
                }
                if (System.currentTimeMillis() - timeStamp > REQUEST_TIMEOUT) {
                    logger.warn("A timeout occurred while requesting data from the water softener");
                    break;
                }
            }
            response = lastLineReceived;
        } finally {
            SerialPortThrottler.unlock(port);
        }

        return response;

    }
}
