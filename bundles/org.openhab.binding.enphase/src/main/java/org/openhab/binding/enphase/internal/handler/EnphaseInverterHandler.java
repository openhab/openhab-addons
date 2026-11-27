/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.MessageTranslator;
import org.openhab.binding.enphase.internal.dto.InverterDTO;
import org.openhab.binding.enphase.internal.dto.PdmDeviceDataDTO;
import org.openhab.binding.enphase.internal.dto.PdmDeviceDataDTO.ChannelDataDTO;
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
 * @author Cedric Boon - Added support for detailed inverter stats
 */
@NonNullByDefault
public class EnphaseInverterHandler extends EnphaseDeviceHandler {

    private static final int SECONDS_PER_HOUR = 3600;

    private @Nullable InverterDTO lastKnownState;
    private @Nullable ChannelDataDTO lastKnownEnergyState;

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
                case INVERTER_CHANNEL_WATT_HOURS_TODAY:
                    refreshWattHoursToday(lastKnownEnergyState);
                    break;
                case INVERTER_CHANNEL_WATT_HOURS_SEVEN_DAYS:
                    refreshWattHoursSevenDays(lastKnownEnergyState);
                    break;
                case INVERTER_CHANNEL_WATT_HOURS_LIFETIME:
                    refreshWattHoursLifetime(lastKnownEnergyState);
                    break;
                case INVERTER_CHANNEL_WATTS_NOW:
                    refreshWattsNow(lastKnownEnergyState);
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
            state = new DateTimeType(Instant.ofEpochSecond(inverterDTO.lastReportDate));
        }
        updateState(INVERTER_CHANNEL_LAST_REPORT_DATE, state);
    }

    /**
     * Updates the watt-hours today/7-days/lifetime and current watts channels from the Envoy per-device energy data
     * ({@code /ivp/pdm/device_data}).
     *
     * @param deviceData the device data of this inverter, or null if not (yet) available.
     */
    public void refreshInverterEnergyChannels(final @Nullable PdmDeviceDataDTO deviceData) {
        final ChannelDataDTO channel = deviceData == null || deviceData.channels == null
                || deviceData.channels.length == 0 ? null : deviceData.channels[0];

        refreshWattHoursToday(channel);
        refreshWattHoursSevenDays(channel);
        refreshWattHoursLifetime(channel);
        refreshWattsNow(channel);
        lastKnownEnergyState = channel;
    }

    private void refreshWattHoursToday(final @Nullable ChannelDataDTO channel) {
        updateState(INVERTER_CHANNEL_WATT_HOURS_TODAY, channel == null || channel.wattHours == null ? UnDefType.UNDEF
                : new QuantityType<>(channel.wattHours.today, Units.WATT_HOUR));
    }

    private void refreshWattHoursSevenDays(final @Nullable ChannelDataDTO channel) {
        updateState(INVERTER_CHANNEL_WATT_HOURS_SEVEN_DAYS,
                channel == null || channel.wattHours == null ? UnDefType.UNDEF
                        : new QuantityType<>(channel.wattHours.week, Units.WATT_HOUR));
    }

    private void refreshWattHoursLifetime(final @Nullable ChannelDataDTO channel) {
        updateState(INVERTER_CHANNEL_WATT_HOURS_LIFETIME,
                channel == null || channel.lifetime == null ? UnDefType.UNDEF
                        : new QuantityType<>(Math.round(channel.lifetime.joulesProduced / (double) SECONDS_PER_HOUR),
                                Units.WATT_HOUR));
    }

    private void refreshWattsNow(final @Nullable ChannelDataDTO channel) {
        updateState(INVERTER_CHANNEL_WATTS_NOW, channel == null || channel.watts == null ? UnDefType.UNDEF
                : new QuantityType<>(channel.watts.now, Units.WATT));
    }
}
