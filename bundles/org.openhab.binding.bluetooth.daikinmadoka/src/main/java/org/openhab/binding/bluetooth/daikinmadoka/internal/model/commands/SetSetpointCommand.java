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

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Lafois
 *
 */
@NonNullByDefault
public class SetSetpointCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(SetSetpointCommand.class);

    private DecimalType coolingSetpoint;
    private DecimalType heatingSetpoint;

    public SetSetpointCommand(DecimalType coolingSetpoint, DecimalType heatingSetpoint) {
        this.coolingSetpoint = coolingSetpoint;
        this.heatingSetpoint = heatingSetpoint;
    }

    @Override
    public byte[] getRequest() {
        byte[] heatingSetpointBytes = ByteBuffer.allocate(2).putShort((short) (128. * heatingSetpoint.shortValue()))
                .array();
        byte[] coolingSetpointBytes = ByteBuffer.allocate(2).putShort((short) (128. * coolingSetpoint.shortValue()))
                .array();

        MadokaValue mvHeatingSetpoint = new MadokaValue(0x21, 2,
                new byte[] { heatingSetpointBytes[0], heatingSetpointBytes[1] });

        MadokaValue mvCoolingSetpoint = new MadokaValue(0x20, 2,
                new byte[] { coolingSetpointBytes[0], coolingSetpointBytes[1] });

        return MadokaMessage.createRequest(this, mvCoolingSetpoint, mvHeatingSetpoint);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm) {
        if (logger.isDebugEnabled() && mm.getRawMessage() != null) {
            byte @NonNull [] msg = (byte @NonNull []) mm.getRawMessage();
            logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(msg));
        }

        setState(State.SUCCEEDED);
        executor.execute(() -> listener.receivedResponse(this));

        return true;
    }

    @Override
    public int getCommandId() {
        return 16448;
    }

    public DecimalType getCoolingSetpoint() {
        return coolingSetpoint;
    }

    public DecimalType getHeatingSetpoint() {
        return heatingSetpoint;
    }

}
