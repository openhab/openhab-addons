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
package org.openhab.binding.easee.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.command.charger.GetConfiguration;
import org.openhab.binding.easee.internal.command.charger.LatestChargingSession;
import org.openhab.binding.easee.internal.command.charger.State;
import org.openhab.binding.easee.internal.connector.WebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class. This is responsible for periodic polling of values from Easse Cloud API.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeCloudPolling implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Handler for delegation to callbacks.
     */
    private final EaseeHandler handler;

    /**
     * Constructor.
     *
     * @param handler instance of the thing handler
     */
    public EaseeCloudPolling(EaseeHandler handler) {
        this.handler = handler;
    }

    /**
     * Poll the Nibe Uplink webservice one time.
     */
    @Override
    public void run() {
        logger.debug("polling Easee Cloud {}", handler.getConfiguration());
        WebInterface easee = handler.getWebInterface();
        easee.enqueueCommand(new State(handler));
        easee.enqueueCommand(new GetConfiguration(handler));
        easee.enqueueCommand(new LatestChargingSession(handler));
    }
}
