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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating heating modes
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class FritzAhaSetHeatingModeCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaSetHeatingModeCallback.class);

    public static final String BOOST_COMMAND = "sethkrboost";
    public static final String WINDOW_OPEN_COMMAND = "sethkrwindowopen";

    private final String ain;

    /**
     * Constructor
     *
     * @param webInterface connection to FRITZ!Box
     * @param ain AIN of the device
     * @param command the mode to set or deactivate
     * @param endTimestamp the end timestamp in seconds, maximum allowed value is now + 24h in the future, 0 to
     *            deactivate the mode
     */
    public FritzAhaSetHeatingModeCallback(FritzAhaWebInterface webInterface, String ain, String command,
            long endTimestamp) {
        super(WEBSERVICE_PATH, String.format("switchcmd=%s&ain=%s&endtimestamp=%d", command, ain, endTimestamp),
                webInterface, HttpMethod.GET, 1);
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
