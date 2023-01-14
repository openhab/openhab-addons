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
package org.openhab.binding.netatmo.internal.handler.capability;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;

/**
 * {@link ChannelHelperCapability} give the capability to dispatch incoming data across the channel helpers.
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
    public void afterNewData(@Nullable NAObject newData) {
        super.afterNewData(newData);
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
    }
}
