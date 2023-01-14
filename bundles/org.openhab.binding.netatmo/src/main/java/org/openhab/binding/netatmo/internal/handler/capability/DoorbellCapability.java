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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.UnDefType;

/**
 * {@link DoorbellCapability} give to handle Welcome Doorbell specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class DoorbellCapability extends CameraCapability {
    private final ThingUID thingUid;

    public DoorbellCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
        thingUid = handler.getThing().getUID();
    }

    @Override
    public void updateWebhookEvent(WebhookEvent event) {
        super.updateWebhookEvent(event);

        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_TYPE),
                toStringType(event.getEventType()));
        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_TIME),
                toDateTimeType(event.getTime()));
        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_SNAPSHOT),
                toRawType(event.getSnapshotUrl()));
        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_SNAPSHOT_URL),
                toStringType(event.getSnapshotUrl()));
        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_VIGNETTE),
                toRawType(event.getVignetteUrl()));
        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_VIGNETTE_URL),
                toStringType(event.getVignetteUrl()));

        String message = event.getName();
        handler.updateState(new ChannelUID(thingUid, GROUP_SUB_EVENT, CHANNEL_EVENT_MESSAGE),
                message == null || message.isBlank() ? UnDefType.NULL : toStringType(message));
    }
}
