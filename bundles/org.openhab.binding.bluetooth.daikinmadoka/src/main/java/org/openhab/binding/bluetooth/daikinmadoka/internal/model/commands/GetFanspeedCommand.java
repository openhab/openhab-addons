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
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.FanSpeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command used to set the FAN speed for all modes (auto/cool/heat...)
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetFanspeedCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetFanspeedCommand.class);

    private @Nullable FanSpeed coolingFanSpeed;
    private @Nullable FanSpeed heatingFanSpeed;

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {

        byte[] valueCoolingFanSpeed = mm.getValues().get(0x20).getRawValue();
        byte[] valueHeatingFanSpeed = mm.getValues().get(0x21).getRawValue();

        if (valueCoolingFanSpeed == null || valueHeatingFanSpeed == null) {
            setState(State.FAILED);
            throw new MadokaParsingException("Incorrect cooling or heating fan speed value");
        }

        this.coolingFanSpeed = FanSpeed.valueOf(valueCoolingFanSpeed[0]);
        this.heatingFanSpeed = FanSpeed.valueOf(valueHeatingFanSpeed[0]);

        logger.debug("coolingFanSpeed: {}", coolingFanSpeed);
        logger.debug("heatingFanSpeed: {}", heatingFanSpeed);

        setState(State.SUCCEEDED);
        executor.execute(() -> listener.receivedResponse(this));
    }

    @Override
    public int getCommandId() {
        return 80;
    }

    public @Nullable FanSpeed getCoolingFanSpeed() {
        return coolingFanSpeed;
    }

    public @Nullable FanSpeed getHeatingFanSpeed() {
        return heatingFanSpeed;
    }
}
