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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.VENDOR;
import static org.openhab.core.thing.Thing.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAError;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * {@link ChannelHelperCapability} give the capability to dispatch incoming data across the channel helpers.
 * This capability is common to all things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ChannelHelperCapability extends Capability {
    private final List<ChannelHelper> channelHelpers;

    public ChannelHelperCapability(CommonInterface handler, List<ChannelHelper> channelHelpers) {
        super(handler);
        this.channelHelpers = channelHelpers;
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        if (firstLaunch && !moduleType.isLogical()) {
            properties.put(PROPERTY_MODEL_ID, moduleType.apiName.isBlank() ? moduleType.name() : moduleType.apiName);
            properties.put(PROPERTY_VENDOR, VENDOR);
        }
    }

    @Override
    public void afterNewData(@Nullable NAObject newData) {
        channelHelpers.forEach(helper -> helper.setNewData(newData));
        handler.getActiveChannels().forEach(channel -> {
            ChannelUID channelUID = channel.getUID();
            String channelID = channelUID.getIdWithoutGroup();
            String groupId = channelUID.getGroupId();
            Configuration channelConfig = channel.getConfiguration();
            for (ChannelHelper helper : channelHelpers) {
                State state = helper.getChannelState(channelID, groupId, channelConfig);
                if (state != null) {
                    handler.updateState(channelUID, state);
                    break;
                }
            }
        });
        super.afterNewData(newData);
    }

    @Override
    protected void updateNAThing(NAThing newData) {
        newData.getFirmware().map(fw -> properties.put(PROPERTY_FIRMWARE_VERSION, fw));
        if (!newData.isReachable()) {
            statusReason = "@text/device-not-connected";
        }
    }

    @Override
    protected void updateErrors(NAError error) {
        if (error.getId().equals(handler.getId())) {
            statusReason = "@text/%s".formatted(error.getCode().message);
        }
    }
}
