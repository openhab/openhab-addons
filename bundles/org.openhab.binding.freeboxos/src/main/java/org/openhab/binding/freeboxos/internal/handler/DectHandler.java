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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Config;
import org.openhab.binding.freeboxos.internal.api.rest.PhoneManager.Status;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * The {@link DectHandler} is responsible for handling DECT specifics of the Telephony API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class DectHandler extends FxsHandler {

    public DectHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateConfigChannels(Config config) {
        super.updateConfigChannels(config);
        updateChannelOnOff(DECT_ACTIVE, config.dectEnabled());
        updateChannelOnOff(ALTERNATE_RING, config.dectRingOnOff());
    }

    @Override
    protected void updateStatusChannels(Status status) {
        super.updateStatusChannels(status);
        updateIfActive(GAIN_RX, new PercentType(status.gainRx()));
        updateIfActive(GAIN_TX, new PercentType(status.gainTx()));
    }

    @Override
    protected boolean internalHandleCommand(String channelId, Command command) throws FreeboxException {
        PhoneManager phoneManager = getManager(PhoneManager.class);
        if (command instanceof OnOffType) {
            boolean status = OnOffType.ON.equals(command);
            if (RINGING.equals(channelId)) {
                phoneManager.ringDect(status);
                return true;
            } else if (DECT_ACTIVE.equals(channelId)) {
                phoneManager.setStatus(status);
                return true;
            } else if (ALTERNATE_RING.equals(channelId)) {
                phoneManager.alternateRing(status);
                return true;
            }
        }
        if (command instanceof PercentType percentCommand) {
            if (GAIN_RX.equals(channelId)) {
                phoneManager.setGainRx(getClientId(), percentCommand.intValue());
                updateIfActive(GAIN_RX, percentCommand);
                return true;
            } else if (GAIN_TX.equals(channelId)) {
                phoneManager.setGainTx(getClientId(), percentCommand.intValue());
                updateIfActive(GAIN_RX, percentCommand);
                return true;
            }
        }
        return super.internalHandleCommand(channelId, command);
    }
}
