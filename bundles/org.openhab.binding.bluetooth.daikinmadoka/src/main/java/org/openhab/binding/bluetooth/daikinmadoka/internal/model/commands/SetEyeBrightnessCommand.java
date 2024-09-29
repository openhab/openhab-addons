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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command used to set the Blue Eye Brightness
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class SetEyeBrightnessCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(SetEyeBrightnessCommand.class);

    private PercentType eyeBrightness;

    public SetEyeBrightnessCommand(PercentType eyeBrightness) {
        this.eyeBrightness = eyeBrightness;
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        byte[] msg = mm.getRawMessage();
        if (logger.isDebugEnabled() && msg != null) {
            logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(msg));
        }

        setState(State.SUCCEEDED);
        executor.execute(() -> listener.receivedResponse(this));
    }

    @Override
    public byte[][] getRequest() {
        // The values accepted by the device are from 0 to 19 - integers
        byte val = (byte) Math.round(eyeBrightness.intValue() * 0.19);

        MadokaValue mv = new MadokaValue(0x33, 1, new byte[] { val });
        return MadokaMessage.createRequest(this, mv);
    }

    @Override
    public int getCommandId() {
        return 17154;
    }

    public PercentType getEyeBrightness() {
        return eyeBrightness;
    }

    /**
     *
     * @param eyeBrightness a percentage - between 0 and 100
     */
    public void setEyeBrightness(PercentType eyeBrightness) {
        this.eyeBrightness = eyeBrightness;
    }
}
