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
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author blafois
 *
 */
public class GetIndoorOutoorTemperatures extends BRC1HCommand {

    private final Logger logger = LoggerFactory.getLogger(GetIndoorOutoorTemperatures.class);

    private DecimalType indoorTemperature;
    private DecimalType outdoorTemperature;

    @Override
    public byte[] getRequest() {
        return MadokaMessage.createRequest(this);
    }

    @Override
    public boolean handleResponse(Executor executor, ResponseListener listener, byte @Nullable [] response) {
        if (response == null) {
            return false;
        }

        try {

            MadokaMessage mm = MadokaMessage.parse(response);

            Integer iIndoorTemperature = Integer.valueOf(mm.getValues().get(0x40).getRawValue()[0]);
            Integer iOutdoorTemperature = Integer.valueOf(mm.getValues().get(0x41).getRawValue()[0]);

            if (iOutdoorTemperature == -1) {
                iOutdoorTemperature = null;
            } else {
                if (iOutdoorTemperature < 0) {
                    iOutdoorTemperature = ((iOutdoorTemperature + 256) - 128) * -1;
                }
            }

            if (iIndoorTemperature != null) {
                indoorTemperature = new DecimalType(iIndoorTemperature);
            }

            if (iOutdoorTemperature != null) {
                outdoorTemperature = new DecimalType(iOutdoorTemperature);
            }

            logger.debug("Indoor Temp: {}", indoorTemperature);
            logger.debug("Outdoor Temp: {}", outdoorTemperature);

            listener.receivedResponse(this);
            setState(State.SUCCEEDED);
            return true;
        } catch (Exception e) {
            logger.error("Error while parsing response", e);
            setState(State.FAILED);
        }
        return false;
    }

    public DecimalType getIndoorTemperature() {
        return indoorTemperature;
    }

    public DecimalType getOutdoorTemperature() {
        return outdoorTemperature;
    }

    @Override
    public int getCommandId() {
        return 272;
    }

}
