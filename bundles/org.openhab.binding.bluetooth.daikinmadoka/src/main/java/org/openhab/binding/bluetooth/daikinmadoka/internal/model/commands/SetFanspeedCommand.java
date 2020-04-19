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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.FAN_SPEED;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class SetFanspeedCommand extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(SetFanspeedCommand.class);

    private FAN_SPEED coolingFanSpeed;
    private FAN_SPEED heatingFanSpeed;

    public SetFanspeedCommand(FAN_SPEED coolingFanSpeed, FAN_SPEED heatingFanSpeed) {
        this.coolingFanSpeed = coolingFanSpeed;
        this.heatingFanSpeed = heatingFanSpeed;
    }

    @Override
    public byte[] getRequest() {
        MadokaValue paramCoolingFanSpeed = new MadokaValue(0x20, 1, new byte[] { (byte) coolingFanSpeed.value() });
        MadokaValue paramHeatingFanSpeed = new MadokaValue(0x21, 1, new byte[] { (byte) heatingFanSpeed.value() });

        return MadokaMessage.createRequest(this, paramCoolingFanSpeed, paramHeatingFanSpeed);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, byte @Nullable [] response) {
        if (response == null) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Got response for {} : {}", this.getClass().getSimpleName(), HexUtils.bytesToHex(response));
        }

        listener.receivedResponse(this);
        setState(State.SUCCEEDED);

        return true;
    }

    @Override
    public int getCommandId() {
        return 16464;
    }

    public FAN_SPEED getCoolingFanSpeed() {
        return coolingFanSpeed;
    }

    public FAN_SPEED getHeatingFanSpeed() {
        return heatingFanSpeed;
    }

}
