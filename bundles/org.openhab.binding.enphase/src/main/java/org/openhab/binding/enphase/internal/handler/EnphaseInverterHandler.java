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
package org.openhab.binding.enphase.internal.handler;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.MessageTranslator;
import org.openhab.binding.enphase.internal.dto.InverterDTO;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EnphaseInverterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnphaseInverterHandler extends EnphaseDeviceHandler {

    private @Nullable InverterDTO lastKnownState;

    public EnphaseInverterHandler(final Thing thing, MessageTranslator messageTranslator) {
        super(thing, messageTranslator);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            final String channelId = channelUID.getId();

            switch (channelId) {
                case INVERTER_CHANNEL_LAST_REPORT_WATTS:
                    refreshLastReportWatts(lastKnownState);
                    break;
                case INVERTER_CHANNEL_MAX_REPORT_WATTS:
                    refreshMaxReportWatts(lastKnownState);
                    break;
                case INVERTER_CHANNEL_LAST_REPORT_DATE:
                    refreshLastReportDate(lastKnownState);
                    break;
                default:
                    super.handleCommandRefresh(channelId);
                    break;
            }
        }
    }

    public void refreshInverterChannels(final @Nullable InverterDTO inverterDTO) {
        refreshLastReportWatts(inverterDTO);
        refreshMaxReportWatts(inverterDTO);
        refreshLastReportDate(inverterDTO);
        lastKnownState = inverterDTO;
    }

    private void refreshLastReportWatts(final @Nullable InverterDTO inverterDTO) {
        updateState(INVERTER_CHANNEL_LAST_REPORT_WATTS,
                inverterDTO == null ? UnDefType.UNDEF : new QuantityType<>(inverterDTO.lastReportWatts, Units.WATT));
    }

    private void refreshMaxReportWatts(final @Nullable InverterDTO inverterDTO) {
        updateState(INVERTER_CHANNEL_MAX_REPORT_WATTS,
                inverterDTO == null ? UnDefType.UNDEF : new QuantityType<>(inverterDTO.maxReportWatts, Units.WATT));
    }

    private void refreshLastReportDate(final @Nullable InverterDTO inverterDTO) {
        final State state;

        if (inverterDTO == null) {
            state = UnDefType.UNDEF;
        } else {
            final Instant instant = Instant.ofEpochSecond(inverterDTO.lastReportDate);
            final ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            logger.trace("[{}] Epoch time {}, zonedDateTime: {}", getThing().getUID(), inverterDTO.lastReportDate,
                    zonedDateTime);
            state = new DateTimeType(zonedDateTime);
        }
        updateState(INVERTER_CHANNEL_LAST_REPORT_DATE, state);
    }
}
