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

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.WebhookEvent;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.UnDefType;

/**
 * {@link AlarmEventCapability} gives the ability to handle Alarm modules events
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class AlarmEventCapability extends HomeSecurityThingCapability {

    public AlarmEventCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
    }

    @Override
    protected void updateWebhookEvent(WebhookEvent event) {
        super.updateWebhookEvent(event);

        final ThingUID thingUid = handler.getThing().getUID();
        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_TYPE),
                toStringType(event.getEventType()));
        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_TIME),
                toDateTimeType(event.getTime()));
        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_SUBTYPE),
                event.getSubTypeDescription().map(d -> toStringType(d)).orElse(UnDefType.NULL));
        final String message = event.getName();
        handler.updateState(new ChannelUID(thingUid, GROUP_LAST_EVENT, CHANNEL_EVENT_MESSAGE),
                message == null || message.isBlank() ? UnDefType.NULL : toStringType(message));
    }

    @Override
    public List<NAObject> updateReadings() {
        return getSecurityCapability().map(cap -> cap.getDeviceLastEvent(handler.getId(), moduleType.apiName))
                .map(event -> List.of((NAObject) event)).orElse(List.of());
    }
}
