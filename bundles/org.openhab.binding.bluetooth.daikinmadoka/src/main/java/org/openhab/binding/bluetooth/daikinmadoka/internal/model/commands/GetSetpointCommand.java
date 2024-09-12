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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command returns the setpoint, whatever is the current mode.
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class GetSetpointCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetSetpointCommand.class);

    private @Nullable QuantityType<Temperature> heatingSetpoint;
    private @Nullable QuantityType<Temperature> coolingSetpoint;

    @Override
    public byte[][] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        MadokaValue heatValue = mm.getValues().get(0x21);
        MadokaValue coolValue = mm.getValues().get(0x20);
        if (heatValue == null || coolValue == null) {
            String message = "heatingSetpoint or coolingSetpoint is null when handling the response";
            setState(State.FAILED);
            throw new MadokaParsingException(message);
        }

        Integer iHeatingSetpoint = (int) (heatValue.getComputedValue() / 128.);
        Integer iCoolingSetpoint = (int) (coolValue.getComputedValue() / 128.);

        this.heatingSetpoint = new QuantityType<>(iHeatingSetpoint, SIUnits.CELSIUS);
        this.coolingSetpoint = new QuantityType<>(iCoolingSetpoint, SIUnits.CELSIUS);

        logger.debug("heatingSetpoint: {}", heatingSetpoint);
        logger.debug("coolingSetpoint: {}", coolingSetpoint);

        setState(State.SUCCEEDED);
        try {
            executor.execute(() -> listener.receivedResponse(this));
        } catch (Exception e) {
            setState(State.FAILED);
            throw new MadokaParsingException(e);
        }
    }

    @Override
    public int getCommandId() {
        return 64;
    }

    public @Nullable QuantityType<Temperature> getHeatingSetpoint() {
        return heatingSetpoint;
    }

    public @Nullable QuantityType<Temperature> getCoolingSetpoint() {
        return coolingSetpoint;
    }
}
