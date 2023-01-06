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

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating heating values Supports reauthorization
 *
 * @author Christoph Weitkamp - Initial contribution
 * @author Christoph Weitkamp - Added support for AVM FRITZ!DECT 300 and Comet
 *         DECT
 */
@NonNullByDefault
public class FritzAhaSetHeatingTemperatureCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaSetHeatingTemperatureCallback.class);

    private static final String WEBSERVICE_COMMAND = "switchcmd=sethkrtsoll";

    private final String ain;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param ain AIN of the device that should be switched
     * @param temperature New temperature
     */
    public FritzAhaSetHeatingTemperatureCallback(FritzAhaWebInterface webIface, String ain, BigDecimal temperature) {
        super(WEBSERVICE_PATH, WEBSERVICE_COMMAND + "&ain=" + ain + "&param=" + temperature, webIface, GET, 1);
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
