/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.core.library.types.DecimalType;
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

    private DecimalType eyeBrightness;

    public SetEyeBrightnessCommand(DecimalType eyeBrightness) {
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
        MadokaValue mv = new MadokaValue(0x33, 1, new byte[] { eyeBrightness.byteValue() });
        return MadokaMessage.createRequest(this, mv);
    }

    @Override
    public int getCommandId() {
        return 17154;
    }

    public DecimalType getEyeBrightness() {
        return eyeBrightness;
    }

    public void setEyeBrightness(DecimalType eyeBrightness) {
        this.eyeBrightness = eyeBrightness;
    }
}
