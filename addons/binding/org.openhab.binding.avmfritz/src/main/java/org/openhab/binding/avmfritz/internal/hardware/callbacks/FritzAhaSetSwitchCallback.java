/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import org.openhab.binding.avmfritz.internal.hardware.FritzAhaWebInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Callback implementation for updating switch states Supports reauthorization
 *
 * @author Robert Bausdorf
 *
 */
public class FritzAhaSetSwitchCallback extends FritzAhaReauthCallback {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * Item to update
     */
    private String itemName;

    /**
     * Constructor
     *
     * @param webIface Interface to FRITZ!Box
     * @param ain AIN of the device that should be switched
     * @param switchOn true - switch on, false - switch off
     */
    public FritzAhaSetSwitchCallback(FritzAhaWebInterface webIface, String ain, boolean switchOn) {
        super(WEBSERVICE_PATH, "ain=" + ain + "&switchcmd=" + (switchOn ? "setswitchon" : "setswitchoff"), webIface,
                Method.GET, 1);
        itemName = ain;
    }

    @Override
    public void execute(int status, String response) {
        super.execute(status, response);
        if (isValidRequest()) {
            logger.debug("Received State response {} for item {}", response, itemName);
        }
    }
}
