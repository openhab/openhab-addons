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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tplinksmarthome.internal.Commands;
import org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType;
import org.openhab.binding.tplinksmarthome.internal.model.Realtime;
import org.openhab.binding.tplinksmarthome.internal.model.Sysinfo.Outlet;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TP-Link Smart Home device with multiple sockets, like the HS107 and HS300.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class PowerStripDevice extends EnergySwitchDevice {

    private final Logger logger = LoggerFactory.getLogger(PowerStripDevice.class);

    private final List<@Nullable Realtime> realTimeCacheList;
    private final List<@Nullable String> childIds;

    public PowerStripDevice(final TPLinkSmartHomeThingType type) {
        final int nrOfSockets = type.getSockets();

        realTimeCacheList = new ArrayList<>(nrOfSockets);
        childIds = new ArrayList<>(Collections.nCopies(nrOfSockets, ""));
    }

    @Override
    public String getUpdateCommand() {
        return Commands.getSysinfo();
    }

    @Override
    public void refreshedDeviceState(@Nullable final DeviceState deviceState) {
        if (deviceState != null) {
            for (int i = 0; i < childIds.size(); i++) {
                childIds.set(i, deviceState.getSysinfo().getChildren().get(i).getId());
                realTimeCacheList.add(i, refreshCache(i));
            }
        }
    }

    @Override
    protected State getOnOffState(final DeviceState deviceState) {
        // Global On/Off state is determined by the combined state of all sockets. If 1 socket is on, the state is on.
        return OnOffType
                .from(deviceState.getSysinfo().getChildren().stream().anyMatch(e -> OnOffType.ON.equals(e.getState())));
    }

    @Override
    public State updateChannel(final ChannelUID channelUid, final DeviceState deviceState) {
        final int idx = channelToIndex(channelUid);

        if (idx >= 0 && idx < childIds.size()) {
            final Outlet outlet = deviceState.getSysinfo().getChildren().get(idx);
            final String baseChannel = channelUid.getIdWithoutGroup();

            if (CHANNEL_SWITCH.equals(baseChannel)) {
                return outlet.getState();
            } else if (CHANNELS_ENERGY.contains(baseChannel)) {
                final Realtime realTime = realTimeCacheList.get(idx);

                return realTime == null ? UnDefType.UNDEF : updateEnergyChannel(baseChannel, realTime);
            }
        } else {
            if (idx >= 0) {
                logger.debug("For channel update the index '{}' could be mapped to a channel. passed channel: {}", idx,
                        channelUid);
            }
        }
        return super.updateChannel(channelUid, deviceState);
    }

    @Override
    protected @Nullable String getChildId(final ChannelUID channelUid) {
        final int idx = channelToIndex(channelUid);

        return idx >= 0 && idx < childIds.size() ? childIds.get(idx) : null;
    }

    private int channelToIndex(final ChannelUID channelUid) {
        final String groupId = channelUid.getGroupId();

        return (groupId != null && groupId.startsWith(CHANNEL_OUTLET_GROUP_PREFIX)
                ? Integer.parseInt(groupId.substring(CHANNEL_OUTLET_GROUP_PREFIX.length()))
                : 0) - 1;
    }

    private @Nullable Realtime refreshCache(final int idx) {
        try {
            final String childId = childIds.get(idx);

            return childId == null ? null
                    : commands.getRealtimeResponse(connection.sendCommand(Commands.getRealtimeWithContext(childId)));
        } catch (final IOException e) {
            return null;
        } catch (final RuntimeException e) {
            logger.debug(
                    "Obtaining realtime data for channel-'{}' unexpectedly crashed. If this keeps happening please report: ",
                    idx, e);
            return null;
        }
    }
}
