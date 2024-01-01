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
 * Callback implementation for applying templates.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class FritzAhaApplyTemplateCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaApplyTemplateCallback.class);

    private static final String WEBSERVICE_COMMAND = "switchcmd=applytemplate";

    private final String ain;

    /**
     * Constructor
     *
     * @param webInterface web interface to FRITZ!Box
     * @param ain AIN of the template that should be applied
     */
    public FritzAhaApplyTemplateCallback(FritzAhaWebInterface webInterface, String ain) {
        super(WEBSERVICE_PATH, WEBSERVICE_COMMAND + "&ain=" + ain, webInterface, GET, 1);
        this.ain = ain;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        if (isValidRequest()) {
            logger.trace("Received response '{}' for item '{}'", response, ain);
        }
    }
}
