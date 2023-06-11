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

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THis command is in charge of changing the AC setpoint
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public class SetSetpointCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(SetSetpointCommand.class);

    private QuantityType<Temperature> coolingSetpoint;
    private QuantityType<Temperature> heatingSetpoint;

    public SetSetpointCommand(QuantityType<Temperature> coolingSetpoint, QuantityType<Temperature> heatingSetpoint) {
        this.coolingSetpoint = coolingSetpoint;
        this.heatingSetpoint = heatingSetpoint;
    }

    @Override
    public byte[][] getRequest() {
        byte[] heatingSetpointBytes = ByteBuffer.allocate(2).putShort((short) (128. * heatingSetpoint.shortValue()))
                .array();
        byte[] coolingSetpointBytes = ByteBuffer.allocate(2).putShort((short) (128. * coolingSetpoint.shortValue()))
                .array();

        MadokaValue mvHeatingSetpoint = new MadokaValue(0x21, 2, heatingSetpointBytes);

        MadokaValue mvCoolingSetpoint = new MadokaValue(0x20, 2, coolingSetpointBytes);

        return MadokaMessage.createRequest(this, mvCoolingSetpoint, mvHeatingSetpoint);
    }

    @Override
    public void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm) {
        byte[] msg = mm.getRawMessage();
        if (logger.isDebugEnabled() && msg != null) {
            logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(msg));
        }

        setState(State.SUCCEEDED);
        executor.execute(() -> listener.receivedResponse(this));
    }

    @Override
    public int getCommandId() {
        return 16448;
    }

    public QuantityType<Temperature> getCoolingSetpoint() {
        return coolingSetpoint;
    }

    public QuantityType<Temperature> getHeatingSetpoint() {
        return heatingSetpoint;
    }
}
