/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import static org.eclipse.jetty.http.HttpMethod.GET;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating colortemperature. Supports reauthorization
 *
 * @author Christoph Sommer - Initial contribution
 */
@NonNullByDefault
public class FritzAhaSetColorTemperatureCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaSetColorTemperatureCallback.class);

    private final String ain;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param ain AIN of the device that should be switched
     * @param temperature Color Temperature in Kelvin (typ.: 2700 to 6500)
     * @param duration Duration of the change in 100ms. 0 immediately.
     */
    public FritzAhaSetColorTemperatureCallback(FritzAhaWebInterface webIface, String ain, int temperature,
            int duration) {
        super(WEBSERVICE_PATH,
                "switchcmd=setcolortemperature&temperature=" + temperature + "&duration=" + duration + "&ain=" + ain,
                webIface, GET, 1);
        this.ain = ain;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        if (isValidRequest()) {
            logger.debug("Received response '{}' for item '{}'", response, ain);
        }
    }
}
