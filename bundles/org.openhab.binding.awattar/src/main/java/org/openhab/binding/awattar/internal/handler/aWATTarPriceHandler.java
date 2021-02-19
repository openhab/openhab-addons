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
package org.openhab.binding.awattar.internal.handler;

import static org.openhab.binding.awattar.internal.aWATTarBindingConstants.*;
import static org.openhab.binding.awattar.internal.aWATTarUtil.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awattar.internal.aWATTarPrice;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link aWATTarPriceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wolfgang Klimt - Initial contribution
 */
@NonNullByDefault
public class aWATTarPriceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(aWATTarPriceHandler.class);
    private static final long INITIAL_DELAY_IN_SECONDS = 1;

    private int thingRefreshInterval = 60;
    private @Nullable ScheduledFuture<?> thingRefresher;

    public aWATTarPriceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Handling command {} for channel {}", command, channelUID);
        if (command instanceof RefreshType) {
            refreshChannel(channelUID);
        } else {
            logger.debug("Binding {} only supports refresh command", BINDING_ID);
        }
    }

    /**
     * Initialize the binding and start the refresh job.
     * The refresh job runs once after initialization and afterwards every hour.
     */

    @Override
    public void initialize() {
        logger.trace("Initializing aWATTar price handler {}", this);

        synchronized (this) {

            if (thingRefresher == null || thingRefresher.isCancelled()) {
                logger.trace("Start Thing refresh job at interval {} seconds.", thingRefreshInterval);
                thingRefresher = scheduler.scheduleAtFixedRate(this::refreshChannels, getMillisToNextMinute(1),
                        thingRefreshInterval * 1000, TimeUnit.MILLISECONDS);
            }

        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    public void dispose() {
        logger.trace("Diposing aWATTar price handler {}", this);
        thingRefresher.cancel(true);
        thingRefresher = null;
    }

    public void refreshChannels() {
        logger.trace("Refreshing channels for {}", getThing().getUID());
        aWATTarBridgeHandler bridgeHandler = (aWATTarBridgeHandler) getBridge().getHandler();
        updateStatus(ThingStatus.ONLINE);
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (ChannelKind.STATE.equals(channel.getKind()) && channelUID.isInGroup() && channelUID.getGroupId() != null
                    && isLinked(channelUID)) {
                logger.trace("Refreshing linked channel {}", channelUID);
                refreshChannel(channel.getUID());
            }
        }
    }

    public void refreshChannel(ChannelUID channelUID) {
        logger.trace("refreshing channel {}", channelUID);
        State state = UnDefType.UNDEF;
        aWATTarBridgeHandler bridgeHandler = (aWATTarBridgeHandler) getBridge().getHandler();

        String group = channelUID.getGroupId();

        ZonedDateTime target;

        if (group.equals(CHANNEL_GROUP_CURRENT)) {
            target = ZonedDateTime.now(bridgeHandler.getTimeZone());
        } else if (group.startsWith("today")) {
            target = getCalendarForHour(Integer.valueOf(group.substring(5)), bridgeHandler.getTimeZone());
        } else if (group.startsWith("tomorrow")) {
            target = getCalendarForHour(Integer.valueOf(group.substring(8)), bridgeHandler.getTimeZone()).plusDays(1);
        } else {
            logger.warn("Unsupported channel group {}", group);
            updateState(channelUID, state);
            return;
        }
        logger.trace("Got target date: {}", target.toString());

        aWATTarPrice price = bridgeHandler.getPriceFor(target.toInstant().toEpochMilli());

        if (price == null) {
            logger.trace("No price found for hour {}", target.toString());
            updateState(channelUID, state);
            return;
        }
        double currentprice = price.getPrice();

        String channelId = channelUID.getIdWithoutGroup();
        switch (channelId) {
            case CHANNEL_MARKET_NET:
                state = toDecimalType(currentprice);
                break;
            case CHANNEL_MARKET_GROSS:
                state = toDecimalType(currentprice * bridgeHandler.getVatFactor());
                break;
            case CHANNEL_TOTAL_NET:
                state = toDecimalType(currentprice + bridgeHandler.getBasePrice());
                break;
            case CHANNEL_TOTAL_GROSS:
                state = toDecimalType((currentprice + bridgeHandler.getBasePrice()) * bridgeHandler.getVatFactor());
                break;
            default:
                logger.warn("Unknown channel id {} for Thing type {}", channelUID, getThing().getThingTypeUID());
        }
        updateState(channelUID, state);
    }

    private DecimalType toDecimalType(Double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return new DecimalType(bd.setScale(2, RoundingMode.HALF_UP));
    }
}
