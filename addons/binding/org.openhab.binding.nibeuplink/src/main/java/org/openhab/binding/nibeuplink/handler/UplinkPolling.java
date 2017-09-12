/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.handler;

import org.openhab.binding.nibeuplink.internal.command.GenericStatusUpdate;
import org.openhab.binding.nibeuplink.internal.command.NibeUplinkCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling worker class. This is responsible for periodic polling of status values.
 *
 * @author afriese - Initial Contribution
 */
public class UplinkPolling implements Runnable {
    /**
     * Logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Handler for delegation to callbacks.
     */
    private final NibeUplinkHandler handler;

    /**
     * Constructor.
     *
     * @param handler
     * @param config
     */
    public UplinkPolling(NibeUplinkHandler handler) {
        this.handler = handler;
    }

    /**
     * Poll the FRITZ!Box websevice one time.
     */
    @Override
    public void run() {
        if (handler.getWebInterface() != null) {
            logger.debug("polling NibeUplink {}", handler.getConfiguration());

            NibeUplinkCommand command = new GenericStatusUpdate(handler);
            try {
                handler.getWebInterface().executeCommand(command);
            } catch (Exception e) {
                logger.error("Caught Error: {}", e.getMessage());
            }

        }
    }
}
