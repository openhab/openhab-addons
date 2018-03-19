/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.eclipse.smarthome.core.types.TypeParser;
import org.openhab.binding.oceanic.OceanicBindingConstants.OceanicChannelSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OceanicThingHandler} is the abstract class responsible for handling commands, which are
 * sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 */
public abstract class OceanicThingHandler extends BaseThingHandler {

    public static final String INTERVAL = "interval";
    public static final String BUFFER_SIZE = "buffer";
    private final Logger logger = LoggerFactory.getLogger(OceanicThingHandler.class);

    protected int bufferSize;
    protected ScheduledFuture<?> pollingJob;
    protected static String lastLineReceived = "";

    public OceanicThingHandler(@NonNull Thing thing) {
        super(thing);
    }

    private Runnable resetRunnable = () -> {
        dispose();
        initialize();
    };

    private Runnable pollingRunnable = () -> {
        try {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                for (Channel aChannel : getThing().getChannels()) {
                    for (OceanicChannelSelector selector : OceanicChannelSelector.values()) {
                        ChannelUID theChannelUID = new ChannelUID(getThing().getUID(), selector.toString());
                        if (aChannel.getUID().equals(theChannelUID)
                                && selector.getTypeValue() == OceanicChannelSelector.ValueSelectorType.GET) {
                            String response = requestResponse(selector.name());
                            if (response != null && response != "") {
                                if (selector.isProperty()) {
                                    logger.debug("Updating the property '{}' with value '{}'", selector.toString(),
                                            selector.convertValue(response));
                                    Map<String, String> properties = editProperties();
                                    properties.put(selector.toString(), selector.convertValue(response));
                                    updateProperties(properties);
                                } else {
                                    State value = createStateForType(selector, response);
                                    updateState(theChannelUID, value);
                                }
                            } else {
                                logger.warn("Received an empty answer for '{}'", selector.name());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An exception occurred while polling the Oceanic Water Softener: '{}'", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            scheduler.schedule(resetRunnable, 0, TimeUnit.SECONDS);
        }
    };

    @Override
    public void initialize() {
        if (getConfig().get(BUFFER_SIZE) == null) {
            bufferSize = 1024;
        } else {
            bufferSize = ((BigDecimal) getConfig().get(BUFFER_SIZE)).intValue();
        }

        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 1,
                    ((BigDecimal) getConfig().get(INTERVAL)).intValue(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            if (!(command instanceof RefreshType)) {
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
    }

    @SuppressWarnings("unchecked")
    private State createStateForType(OceanicChannelSelector selector, String value) {
        Class<? extends Type> typeClass = selector.getTypeClass();
        List<Class<? extends State>> stateTypeList = new ArrayList<>();

        stateTypeList.add((Class<? extends State>) typeClass);
        State state = TypeParser.parseState(stateTypeList, selector.convertValue(value));

        return state;
    }

    protected abstract String requestResponse(String commandAsString);

}
