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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
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

    private @Nullable DecimalType heatingSetpoint;
    private @Nullable DecimalType coolingSetpoint;

    @Override
    public byte[] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException {
        try {
            Integer iHeatingSetpoint = (int) (mm.getValues().get(0x21).getComputedValue() / 128.);
            Integer iCoolingSetpoint = (int) (mm.getValues().get(0x20).getComputedValue() / 128.);

            this.heatingSetpoint = new DecimalType(iHeatingSetpoint);
            this.coolingSetpoint = new DecimalType(iCoolingSetpoint);

            logger.debug("heatingSetpoint: {}", heatingSetpoint);
            logger.debug("coolingSetpoint: {}", coolingSetpoint);

            setState(State.SUCCEEDED);
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

    public @Nullable DecimalType getHeatingSetpoint() {
        return heatingSetpoint;
    }

    public @Nullable DecimalType getCoolingSetpoint() {
        return coolingSetpoint;
    }

}
