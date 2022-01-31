/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.oceanic.internal.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.oceanic.internal.OceanicBindingConstants.OceanicChannelSelector;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OceanicThingHandler} is the abstract class responsible for handling commands, which are
 * sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public abstract class OceanicThingHandler extends BaseThingHandler {

    public static final String INTERVAL = "interval";
    public static final String BUFFER_SIZE = "buffer";
    private final Logger logger = LoggerFactory.getLogger(OceanicThingHandler.class);

    protected int bufferSize;
    protected @Nullable ScheduledFuture<?> pollingJob;
    protected static String lastLineReceived = "";

    public OceanicThingHandler(Thing thing) {
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
                            if (response != null && !response.isEmpty()) {
                                if (selector.isProperty()) {
                                    logger.debug("Updating the property '{}' with value '{}'", selector.toString(),
                                            selector.convertValue(response));
                                    Map<String, String> properties = editProperties();
                                    properties.put(selector.toString(), selector.convertValue(response));
                                    updateProperties(properties);
                                } else {
                                    State value = createStateForType(selector, response);
                                    if (value != null) {
                                        updateState(theChannelUID, value);
                                    }
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
                            if ("ERR".equals(response)) {
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
    private @Nullable State createStateForType(OceanicChannelSelector selector, String value) {
        return TypeParser.parseState(List.of((Class<? extends State>) selector.getTypeClass()),
                selector.convertValue(value));
    }

    protected abstract @Nullable String requestResponse(String commandAsString);
}
