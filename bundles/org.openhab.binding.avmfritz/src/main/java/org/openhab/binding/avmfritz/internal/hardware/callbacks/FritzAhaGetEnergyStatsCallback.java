/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.avmfritz.internal.handler.AVMFritzPowerMeterDeviceHandler;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating power meter readings
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class FritzAhaGetEnergyStatsCallback extends FritzAhaReauthCallback {

    private static final String WEBSERVICE_PATH = "net/home_auto_query.lua";

    private final Logger logger = LoggerFactory.getLogger(FritzAhaGetEnergyStatsCallback.class);

    private final AVMFritzPowerMeterDeviceHandler handler;

    private final long deviceId;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param deviceId ID of the device that should be updated
     */
    public FritzAhaGetEnergyStatsCallback(FritzAhaWebInterface webIface, AVMFritzPowerMeterDeviceHandler handler,
            long deviceId) {
        super(WEBSERVICE_PATH, "no_sidrenew=1&command=EnergyStats_10&useajax=1&xhr=1&id=" + deviceId, webIface, GET, 1);
        this.handler = handler;
        this.deviceId = deviceId;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        logger.debug("Received EnergyStats response '{}' for item '{}'", response, deviceId);
        if (isValidRequest()) {
            handler.onEnergyStatsUpdated(response);
        }
    }
}
