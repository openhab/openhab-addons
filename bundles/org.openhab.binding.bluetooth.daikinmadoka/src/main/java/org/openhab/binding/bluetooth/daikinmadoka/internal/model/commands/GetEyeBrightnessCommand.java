/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command used to get the blue Eye brightness level
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetEyeBrightnessCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetEyeBrightnessCommand.class);

    private @Nullable PercentType eyeBrightness;

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        MadokaValue mValue = mm.getValues().get(0x33);
        if (mValue == null) {
            String message = "eye brightness is null when handling the response";
            setState(State.FAILED);
            throw new MadokaParsingException(message);
        }

        byte[] bEyeBrightness = mValue.getRawValue();
        if (bEyeBrightness == null) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect eye brightness value");
        }

        Integer iEyeBrightness = Integer.valueOf(bEyeBrightness[0]);
        // The values accepted by the device are from 0 to 19 - integers so conversion needed for Dimmer channel
        eyeBrightness = new PercentType((int) Math.round(iEyeBrightness / 0.19));

        logger.debug("Eye Brightness: {}", eyeBrightness);

        setState(State.SUCCEEDED);
        try {
            executor.execute(() -> listener.receivedResponse(this));
        } catch (Exception e) {
            setState(State.FAILED);
            throw new MadokaParsingException(e);
        }
    }

    @Override
    public byte[][] getRequest() {
        // We can call the function without parameters - but it will return all the display parameters, which makes a 3
        // chunks return message. As such, specifying requested value 0x33 (eyeBrightness)
        MadokaValue mv = new MadokaValue(0x33, 1, new byte[] { (byte) 0x00 });
        return MadokaMessage.createRequest(this, mv);
    }

    @Override
    public int getCommandId() {
        return 770;
    }

    public @Nullable PercentType getEyeBrightness() {
        return eyeBrightness;
    }
}
