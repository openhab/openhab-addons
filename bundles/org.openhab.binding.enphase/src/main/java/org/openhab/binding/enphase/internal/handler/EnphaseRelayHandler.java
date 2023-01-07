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
package org.openhab.binding.enphase.internal.handler;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enphase.internal.MessageTranslator;
import org.openhab.binding.enphase.internal.dto.InventoryJsonDTO.DeviceDTO;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * The {@link EnphaseInverterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnphaseRelayHandler extends EnphaseDeviceHandler {

    public EnphaseRelayHandler(final Thing thing, MessageTranslator messageTranslator) {
        super(thing, messageTranslator);
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            final String channelId = channelUID.getId();

            switch (channelId) {
                case RELAY_CHANNEL_RELAY:
                    refreshRelayChannel(lastKnownDeviceState);
                    break;
                case RELAY_CHANNEL_LINE_1_CONNECTED:
                    refreshLine1Connect(lastKnownDeviceState);
                    break;
                case RELAY_CHANNEL_LINE_2_CONNECTED:
                    refreshLine2Connect(lastKnownDeviceState);
                    break;
                case RELAY_CHANNEL_LINE_3_CONNECTED:
                    refreshLine3Connect(lastKnownDeviceState);
                    break;
                default:
                    super.handleCommandRefresh(channelId);
                    break;
            }
        }
    }

    private void refreshRelayChannel(@Nullable final DeviceDTO deviceDTO) {
        updateState(RELAY_CHANNEL_RELAY, deviceDTO == null ? UnDefType.UNDEF
                : (RELAY_STATUS_CLOSED.equals(deviceDTO.relay) ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    private void refreshLine1Connect(@Nullable final DeviceDTO deviceDTO) {
        updateState(RELAY_CHANNEL_LINE_1_CONNECTED, deviceDTO == null ? UnDefType.UNDEF
                : (deviceDTO.line1Connected ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    private void refreshLine2Connect(@Nullable final DeviceDTO deviceDTO) {
        updateState(RELAY_CHANNEL_LINE_2_CONNECTED, deviceDTO == null ? UnDefType.UNDEF
                : (deviceDTO.line2Connected ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    private void refreshLine3Connect(@Nullable final DeviceDTO deviceDTO) {
        updateState(RELAY_CHANNEL_LINE_3_CONNECTED, deviceDTO == null ? UnDefType.UNDEF
                : (deviceDTO.line3Connected ? OpenClosedType.CLOSED : OpenClosedType.OPEN));
    }

    @Override
    public void refreshDeviceState(@Nullable final DeviceDTO deviceDTO) {
        refreshRelayChannel(deviceDTO);
        refreshLine1Connect(deviceDTO);
        refreshLine2Connect(deviceDTO);
        refreshLine3Connect(deviceDTO);
        super.refreshDeviceState(deviceDTO);
    }
}
