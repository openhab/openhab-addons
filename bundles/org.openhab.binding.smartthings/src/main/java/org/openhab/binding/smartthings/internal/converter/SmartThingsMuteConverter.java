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
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Converter class for SmartThings "Color" capability and not the "Color Control" capability.
 * The SmartThings Color capability seems to be a later capability where the hue is in the standard 0 - 360 range and
 * therefore doesn't need to be converted for openHAB
 *
 * @author Bob Raker - Initial contribution
 * @author Laurent Arnal - review code for new API
 */
@NonNullByDefault
public class SmartThingsMuteConverter extends SmartThingsConverter {

    public SmartThingsMuteConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command,
            SmartThingsCapability capa, SmartThingsAttribute attr, String componentKey, String capaKey, String attrKey,
            String targetType, String commandKey) throws SmartThingsException {
        if (command instanceof OnOffType onOffType) {
            String cmdName = SmartThingsBindingConstants.CMD_SET_MUTE;
            Object[] arguments = new Object[1];
            if (onOffType == OnOffType.OFF) {
                arguments[0] = "unmuted";
            } else if (onOffType == OnOffType.ON) {
                arguments[0] = "muted";
            }

            this.pushCommand(componentKey, capaKey, cmdName, arguments);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.smartthings.internal.converter.SmartThingsConverter#convertToOpenHab(java.lang.String,
     * org.openhab.binding.smartthings.internal.SmartThingsStateData)
     */
    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartThings) {
        String value = (String) dataFromSmartThings;
        if (value.isBlank()) {
            return UnDefType.UNDEF;
        }

        if ("muted".equals(value)) {
            return OnOffType.ON;
        }
        if ("unmuted".equals(value)) {
            return OnOffType.OFF;
        }

        return OnOffType.OFF;
    }
}
