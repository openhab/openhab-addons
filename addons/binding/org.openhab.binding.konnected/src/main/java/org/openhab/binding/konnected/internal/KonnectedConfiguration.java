/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal;

import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link KonnectedConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedConfiguration extends Configuration {

    /**
     * @param authToken
     *                        Configurable token for authentication of messages from Konnected modules
     *
     * @param hostAddress
     *                        Allows for manual identification of the host ip address of the openHAB server the
     *                        Konnected module
     *                        should send messages to
     */
    public String authToken;
    public String hostAddress;
}
