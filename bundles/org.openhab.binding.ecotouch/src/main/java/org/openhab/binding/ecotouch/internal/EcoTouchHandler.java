/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
                        updateState(channel, quantity);
                    } else {
                        DecimalType number = new DecimalType(value);
                        updateState(channel, number);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        var localConnector = connector;
        if (localConnector == null) {
            // the binding was not initialized, yet
            return;
        }
        if (command instanceof RefreshType) {
            // request a refresh of a channel
            try {
                EcoTouchTags tag = EcoTouchTags.fromString(channelUID.getId());
                if (tag != null) {
                    String valueStr = localConnector.getValue(tag.getTagName());
                    updateChannel(tag.getTagName(), valueStr);
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (Exception e) {
            }
        } else {
            // send command to heat pump
            try {
                EcoTouchTags ecoTouchTag = EcoTouchTags.fromString(channelUID.getId());
                if (ecoTouchTag == null) {
                    logger.warn("ID: {} unknown", channelUID.getId());
                    return;
                }
                if (ecoTouchTag == EcoTouchTags.TYPE_ADAPT_HEATING) {
                    // this type needs special treatment
                    QuantityType<?> value = (QuantityType<?>) command;
                    int raw = Math.round(value.floatValue() * 2 + 4);
                    localConnector.setValue(ecoTouchTag.getTagName(), raw);
                } else {
                    if (ecoTouchTag.getUnit() != ONE) {
                        if (command instanceof QuantityType) {
                            // convert from user unit to heat pump unit
                            QuantityType<?> value = (QuantityType<?>) command;
                            QuantityType<?> rawUnit = value.toUnit(ecoTouchTag.getUnit());
                            if (rawUnit != null) {
                                int raw = (int) (rawUnit.doubleValue() * ecoTouchTag.getDivisor());
                                localConnector.setValue(ecoTouchTag.getTagName(), raw);
                            }
                        } else {
                            logger.debug("handleCommand: requires a QuantityType");
                        }
                    } else {
                        State state = (State) command;
                        DecimalType decimalType = state.as(DecimalType.class);
                        if (decimalType != null) {
                            BigDecimal decimal = decimalType.toBigDecimal();
                            decimal = decimal.multiply(new BigDecimal(ecoTouchTag.getDivisor()));
                            int raw = decimal.intValue();
                            localConnector.setValue(ecoTouchTag.getTagName(), raw);
                        } else {
                            logger.debug("cannot convert {} to a DecimalType", state);
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("handleCommand: {}", e.toString());
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EcoTouchConfiguration.class);

        var localConfig = config;
        if (localConfig == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        connector = new EcoTouchConnector(localConfig.ip, localConfig.username, localConfig.password);

        scheduler.execute(() -> {
            try {
                // try to get a single value
                var localConnector = connector;
                if (localConnector == null) {
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }
                localConnector.getValue("A1");
            } catch (IOException io) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, io.toString());
                return;
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.toString());
                return;
            }

            updateStatus(ThingStatus.ONLINE);
        });

        // start refresh handler
        startAutomaticRefresh();
    }

    private void startAutomaticRefresh() {
        var localRefreshJob = refreshJob;
        if (localRefreshJob == null || localRefreshJob.isCancelled()) {
            Runnable runnable = () -> {
                try {
                    Set<String> tags = new HashSet<String>();
                    for (EcoTouchTags ecoTouchTag : EcoTouchTags.values()) {
                        String channel = ecoTouchTag.getCommand();
                        boolean linked = isLinked(channel);
                        if (linked) {
                            tags.add(ecoTouchTag.getTagName());
                        }
                    }
                    var localConnector = connector;
                    if (localConnector != null) {
                        Map<String, String> result = localConnector.getValues(tags);

                        Iterator<Map.Entry<String, String>> it = result.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, String> pair = it.next();
                            updateChannel(pair.getKey(), pair.getValue());
                        }
                    }
                } catch (IOException io) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, io.toString());
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.toString());
                } catch (Error e) {
                    // during thing creation, the following error is thrown:
                    // java.lang.NoSuchMethodError: 'org.openhab.binding.ecotouch.internal.EcoTouchTags[]
                    // org.openhab.binding.ecotouch.internal.EcoTouchTags.values()'
                    // not sure why... lets ignore it for now
                    updateStatus(ThingStatus.OFFLINE);
                }
            };

            var localConfig = config;
            if (localConfig != null) {
                refreshJob = scheduler.scheduleWithFixedDelay(runnable, 10, localConfig.refresh, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        var localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            localRefreshJob.cancel(true);
            localRefreshJob = null;
        }
        var localConnector = connector;
        if (localConnector != null) {
            localConnector.logout();
        }
    }
}
