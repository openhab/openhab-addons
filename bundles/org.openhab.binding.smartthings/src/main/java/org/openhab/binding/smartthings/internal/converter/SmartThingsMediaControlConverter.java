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
package org.openhab.binding.smartthings.internal.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Converter for media controls that combine playback state and track navigation.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class SmartThingsMediaControlConverter extends SmartThingsDefaultConverter {
    private static final String CAPABILITY_MEDIA_TRACK_CONTROL = "mediaTrackControl";
    private static final String COMMAND_NEXT_TRACK = "nextTrack";
    private static final String COMMAND_PREVIOUS_TRACK = "previousTrack";

    public SmartThingsMediaControlConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command,
            SmartThingsCapability capa, SmartThingsAttribute attr, String componentKey, String capaKey, String attrKey,
            String targetType, String commandKey) throws SmartThingsException {
        if (NextPreviousType.NEXT.equals(command)) {
            pushCommand(componentKey, CAPABILITY_MEDIA_TRACK_CONTROL, COMMAND_NEXT_TRACK, null);
            return;
        }
        if (NextPreviousType.PREVIOUS.equals(command)) {
            pushCommand(componentKey, CAPABILITY_MEDIA_TRACK_CONTROL, COMMAND_PREVIOUS_TRACK, null);
            return;
        }

        super.convertToSmartThingsInternal(thing, channelUid, command, capa, attr, componentKey, capaKey, attrKey,
                targetType, commandKey);
    }
}
