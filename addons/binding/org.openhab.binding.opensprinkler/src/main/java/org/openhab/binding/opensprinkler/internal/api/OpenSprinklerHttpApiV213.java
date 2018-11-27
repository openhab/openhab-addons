/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal.api;

import org.openhab.binding.opensprinkler.internal.util.Hash;

/**
 * The {@link OpenSprinklerHttpApiV213} class is used for communicating with
 * the OpenSprinkler API for firmware versions 2.1.3 and up.
 *
 * @author Chris Graham - Initial contribution
 */
public class OpenSprinklerHttpApiV213 extends OpenSprinklerHttpApiV210 {
    /**
     * Constructor for the OpenSprinkler API class to create a connection to the OpenSprinkler
     * device for control and obtaining status info.
     *
     * @param hostname Hostname or IP address as a String of the OpenSprinkler device.
     * @param port The port number the OpenSprinkler API is listening on.
     * @param password Admin password for the OpenSprinkler device.
     * @throws Exception
     */
    public OpenSprinklerHttpApiV213(final String hostname, final int port, final String password) throws Exception {
        super(hostname, port, Hash.getMD5Hash(password));
    }
}
