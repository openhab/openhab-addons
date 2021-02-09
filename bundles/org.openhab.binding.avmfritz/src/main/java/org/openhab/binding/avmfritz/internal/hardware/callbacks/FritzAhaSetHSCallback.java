/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
 * Callback implementation for updating hud and saturation Supports reauthorization
 *
 * @author Joshua Bacher - Initial contribution Support for DECT!500 bulb
 */
@NonNullByDefault
public class FritzAhaSetHSCallback extends FritzAhaReauthCallback {

    private final Logger logger = LoggerFactory.getLogger(FritzAhaSetHSCallback.class);

    private static final String WEBSERVICE_COMMAND = "switchcmd=setcolor";

    private final String ain;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param ain AIN of the device that should be switched
     * @param level New level
     */
    public FritzAhaSetHSCallback(FritzAhaWebInterface webIface, String ain, Integer hue, Integer saturation) {

        super(WEBSERVICE_PATH,
                WEBSERVICE_COMMAND + "&ain=" + ain + "&hue=" + hue + "&saturation=" + saturation + "&duration=0",
                webIface, GET, 1);
        this.ain = ain;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
    }
}
