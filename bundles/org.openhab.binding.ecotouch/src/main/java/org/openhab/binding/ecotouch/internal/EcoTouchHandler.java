/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.ecotouch.internal;

import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcoTouchHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Sebastian Held - Initial contribution
 */
@NonNullByDefault
public class EcoTouchHandler extends BaseThingHandler {

    // List of Configuration constants
    // public static final String IP = "ip";
    // public static final String USERNAME = "username";
    // public static final String PASSWORD = "password";

    private @Nullable EcoTouchConnector connector = null;

    private final Logger logger = LoggerFactory.getLogger(EcoTouchHandler.class);

    private @Nullable EcoTouchConfiguration config = null;

    private @Nullable ScheduledFuture<?> refreshJob = null;

    public EcoTouchHandler(Thing thing) {
        super(thing);
    }

    private void updateChannel(String tag, String value_str) {
        try {
            List<EcoTouchTags> ecoTouchTags = EcoTouchTags.fromTag(tag);
            for (EcoTouchTags ecoTouchTag : ecoTouchTags) {
                String channel = ecoTouchTag.getCommand();
                BigDecimal value = ecoTouchTag.decodeValue(value_str);
                if (ecoTouchTag == EcoTouchTags.TYPE_ADAPT_HEATING) {
                    // this type needs special treatment
                    // the following reads: value = value / 2 - 2
                    value = value.divide(new BigDecimal(2), 1, RoundingMode.UNNECESSARY).subtract(new BigDecimal(2));
                    QuantityType<?> quantity = new QuantityType<javax.measure.quantity.Temperature>(value, CELSIUS);
                    updateState(channel, quantity);
                } else {
                    if (ecoTouchTag.getUnit() != ONE) {
                        // this is a quantity type
                        QuantityType<?> quantity = new QuantityType<>(value, ecoTouchTag.getUnit());
                        logger.debug("refresh: {} = {}", ecoTouchTag.getTagName(), quantity);
                        updateState(channel, quantity);
                    } else {
                        DecimalType number = new DecimalType(value);
                        logger.debug("refresh: {} = {}", ecoTouchTag.getTagName(), number);
                        updateState(channel, number);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand()");
        if (command instanceof RefreshType) {
            // request a refresh of a channel
            try {
                EcoTouchTags tag = EcoTouchTags.fromString(channelUID.getId());
                String value_str = connector.getValue_str(tag.getTagName());
                updateChannel(tag.getTagName(), value_str);
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
            }
        } else {
            // send command to heat pump
            logger.debug("handleCommand() no refresh");
            try {
                EcoTouchTags ecoTouchTag = EcoTouchTags.fromString(channelUID.getId());
                if (ecoTouchTag == EcoTouchTags.TYPE_ADAPT_HEATING) {
                    // this type needs special treatment
                    QuantityType<?> value = (QuantityType<?>) command;
                    int raw = Math.round(value.floatValue() * 2 + 4);
                    connector.setValue(ecoTouchTag.getTagName(), raw);
                } else {
                    if (ecoTouchTag.getUnit() != ONE) {
                        if (command instanceof QuantityType) {
                            // convert from user unit to heat pump unit
                            QuantityType<?> value = (QuantityType<?>) command;
                            QuantityType<?> raw_unit = value.toUnit(ecoTouchTag.getUnit());
                            int raw = raw_unit.intValue();
                            raw *= ecoTouchTag.getDivisor();
                            connector.setValue(ecoTouchTag.getTagName(), raw);
                        } else {
                            logger.debug("handleCommand: requires a QuantityType");
                        }
                    } else {
                        State state = (State) command;
                        BigDecimal decimal = (state.as(DecimalType.class)).toBigDecimal();
                        decimal = decimal.multiply(new BigDecimal(ecoTouchTag.getDivisor()));
                        int raw = decimal.intValue();
                        connector.setValue(ecoTouchTag.getTagName(), raw);
                    }
                }
            } catch (Exception e) {
                logger.debug("handleCommand: {}", e.toString());
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        config = getConfigAs(EcoTouchConfiguration.class);

        connector = new EcoTouchConnector(config.ip, config.username, config.password);

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = true;
            try {
                // try to get a single value
                connector.getValue_str("A1");
            } catch (IOException io) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, io.toString());
                return;
            } catch (Exception e) {
                thingReachable = false;
            }

            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // start refresh handler
        startAutomaticRefresh();

        logger.debug("Finished initializing!");
    }

    private void startAutomaticRefresh() {
        if (refreshJob == null || refreshJob.isCancelled()) {
            Runnable runnable = () -> {
                logger.debug("startAutomaticRefresh");
                try {
                    Set<String> tags = new HashSet<String>();
                    for (EcoTouchTags ecoTouchTag : EcoTouchTags.values()) {
                        String channel = ecoTouchTag.getCommand();
                        boolean linked = isLinked(channel);
                        if (linked)
                            tags.add(ecoTouchTag.getTagName());
                    }
                    logger.debug("request: {}", tags);
                    Map<String, String> result = connector.getValues_str(tags);
                    logger.debug("result: {}", result);

                    Iterator<Map.Entry<String, String>> it = result.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> pair = it.next();
                        updateChannel(pair.getKey(), pair.getValue());
                    }
                } catch (IOException io) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, io.getMessage());
                } catch (Exception e) {
                    logger.error("Exception occurred during execution: {}", e.toString());
                    updateStatus(ThingStatus.OFFLINE);
                } catch (Error e) {
                    // during thing creation, the following error is thrown:
                    // java.lang.NoSuchMethodError: 'org.openhab.binding.ecotouch.internal.EcoTouchTags[]
                    // org.openhab.binding.ecotouch.internal.EcoTouchTags.values()'
                    // not sure why... lets ignore it for now
                    updateStatus(ThingStatus.OFFLINE);
                }
            };

            refreshJob = scheduler.scheduleWithFixedDelay(runnable, 0, config.refresh, TimeUnit.SECONDS);
        }
    }

    @Override
    public void dispose() {
        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        if (connector != null)
            connector.logout();
    }
}
