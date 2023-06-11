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
package org.openhab.binding.dbquery.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.error.UnnexpectedCondition;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Updates a query result to needed channels doing needed conversions
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class QueryResultChannelUpdater {
    private final ChannelStateUpdater channelStateUpdater;
    private final ChannelsToUpdateQueryResult channels2Update;
    private final Value2StateConverter value2StateConverter;

    public QueryResultChannelUpdater(ChannelStateUpdater channelStateUpdater,
            ChannelsToUpdateQueryResult channelsToUpdate) {
        this.channelStateUpdater = channelStateUpdater;
        this.channels2Update = channelsToUpdate;
        this.value2StateConverter = new Value2StateConverter();
    }

    public void clearChannelResults() {
        for (Channel channel : channels2Update.getChannels()) {
            channelStateUpdater.updateChannelState(channel, UnDefType.NULL);
        }
    }

    public void updateChannelResults(@Nullable Object extractedResult) {
        for (Channel channel : channels2Update.getChannels()) {
            Class<? extends State> targetType = calculateItemType(channel);
            State state = value2StateConverter.convertValue(extractedResult, targetType);
            channelStateUpdater.updateChannelState(channel, state);
        }
    }

    private Class<? extends State> calculateItemType(Channel channel) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        String channelID = channelTypeUID != null ? channelTypeUID.getId()
                : DBQueryBindingConstants.RESULT_STRING_CHANNEL_TYPE;
        switch (channelID) {
            case DBQueryBindingConstants.RESULT_STRING_CHANNEL_TYPE:
                return StringType.class;
            case DBQueryBindingConstants.RESULT_NUMBER_CHANNEL_TYPE:
                return DecimalType.class;
            case DBQueryBindingConstants.RESULT_DATETIME_CHANNEL_TYPE:
                return DateTimeType.class;
            case DBQueryBindingConstants.RESULT_SWITCH_CHANNEL_TYPE:
                return OnOffType.class;
            case DBQueryBindingConstants.RESULT_CONTACT_CHANNEL_TYPE:
                return OpenClosedType.class;
            default:
                throw new UnnexpectedCondition("Unexpected channel type " + channelTypeUID);
        }
    }
}
