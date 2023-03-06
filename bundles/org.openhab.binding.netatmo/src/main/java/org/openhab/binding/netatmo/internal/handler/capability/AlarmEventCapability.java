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
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;

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
    public List<NAObject> updateReadings() {
        return securityCapability.map(cap -> cap.getDeviceLastEvent(handler.getId(), moduleType.apiName))
                .map(event -> List.of((NAObject) event)).orElse(List.of());
    }
}
