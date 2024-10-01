/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.freecurrency.internal;

import static org.openhab.binding.freecurrency.internal.FreecurrencyBindingConstants.CHANNEL_TYPE_EXCHANGE_RATE;
import static org.openhab.binding.freecurrency.internal.FreecurrencyBindingConstants.CHANNEL_TYPE_LAST_UPDATE;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freecurrency.internal.config.FreecurrencyExhangeRateChannelConfig;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * The {@link FreecurrencyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class FreecurrencyHandler extends BaseThingHandler implements ExchangeRateListener {
    private final FreecurrencyProvider freecurrencyProvider;

    public FreecurrencyHandler(Thing thing, FreecurrencyProvider freecurrencyProvider) {
        super(thing);
        this.freecurrencyProvider = freecurrencyProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Channel channel = thing.getChannel(channelUID);
        if (RefreshType.REFRESH.equals(command) && channel != null) {
            refreshChannel(channel);
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        freecurrencyProvider.addListener(this);
    }

    @Override
    public void dispose() {
        freecurrencyProvider.removeListener(this);
    }

    private void refreshChannel(Channel channel) {
        if (CHANNEL_TYPE_EXCHANGE_RATE.equals(channel.getChannelTypeUID())) {
            FreecurrencyExhangeRateChannelConfig config = channel.getConfiguration()
                    .as(FreecurrencyExhangeRateChannelConfig.class);
            BigDecimal val = freecurrencyProvider.getExchangeRate(config.currency1, config.currency2);
            updateState(channel.getUID(), val != null ? new DecimalType(val) : UnDefType.UNDEF);
        } else if (CHANNEL_TYPE_LAST_UPDATE.equals(channel.getChannelTypeUID())) {
            ZonedDateTime lastUpdated = freecurrencyProvider.getLastUpdated();
            updateState(channel.getUID(), lastUpdated == null ? UnDefType.UNDEF : new DateTimeType(lastUpdated));
        }
    }

    @Override
    public void onExchangeRatesChanged() {
        thing.getChannels().forEach(this::refreshChannel);
    }
}
