/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT6nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */

public class SoulissT6nHandler extends SoulissGenericTypical implements typicalCommonMethods {

    private Logger logger = LoggerFactory.getLogger(SoulissT6nHandler.class);
    private DecimalType _setPointValue = DecimalType.ZERO;

    public SoulissT6nHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType) {
            int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
            byte B2 = (byte) (uu >> 8);
            byte B1 = (byte) uu;
            // setpoint command
            commandSEND(B1, B2);
        }
    }

    @Override
    public void initialize() {

        // status online
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            if (!_setPointValue.equals(_state)) {
                this.updateState(SoulissBindingConstants.T6N_VALUE_CHANNEL, (DecimalType) _state);
                _setPointValue = (DecimalType) _state;
            }
        }
    }
}
