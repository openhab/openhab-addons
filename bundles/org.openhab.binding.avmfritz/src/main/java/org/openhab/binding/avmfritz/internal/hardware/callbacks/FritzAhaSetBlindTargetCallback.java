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
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import static org.eclipse.jetty.http.HttpMethod.GET;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating blind commands. Supports reauthorization
 *
 * @author Ulrich Mertin - Initial contribution
 */
@NonNullByDefault
public class FritzAhaSetBlindTargetCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaSetBlindTargetCallback.class);

    private final String ain;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param ain AIN of the device that should be switched
     * @param command Blind command to send
     */
    public FritzAhaSetBlindTargetCallback(FritzAhaWebInterface webIface, String ain, BlindCommand command) {
        super(WEBSERVICE_PATH, "switchcmd=setblind&target=" + command.getTarget() + "&ain=" + ain, webIface, GET, 1);
        this.ain = ain;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        if (isValidRequest()) {
            logger.debug("Received response '{}' for item '{}'", response, ain);
        }
    }

    public enum BlindCommand {
        OPEN("open"),
        CLOSE("close"),
        STOP("stop");

        private final String target;

        private BlindCommand(final String target) {
            this.target = target;
        }

        public String getTarget() {
            return target;
        }
    }
}
