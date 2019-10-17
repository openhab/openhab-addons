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

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;

// import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The {@link SoulissT5nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT5nHandler extends SoulissGenericHandler {

    // private Logger logger = LoggerFactory.getLogger(SoulissT5nHandler.class);
    float fVal;

    public SoulissT5nHandler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setState(PrimitiveType state) {
        if (state != null) {
            this.updateState(SoulissBindingConstants.T5N_VALUE_CHANNEL, (DecimalType) state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        throw new NotImplementedException();
    }

    public void setFloatValue(float valueOf) {
        super.setLastStatusStored();
        if (fVal != valueOf) {
            try {
                this.setState(DecimalType.valueOf(Float.toString(valueOf)));
                fVal = valueOf;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte getRawState() {
        throw new NotImplementedException();
    }

    public float getFloatState() {
        return fVal;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }
}
