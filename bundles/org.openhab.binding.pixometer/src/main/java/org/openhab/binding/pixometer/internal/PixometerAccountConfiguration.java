/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

package org.openhab.binding.pixometer.internal;

/**
 * Data class representing the user configurable settings of the api
 *
 * @author Jerome Luckenbach - Initial contribution
 */

public class PixometerAccountConfiguration {

    /**
     * The configured user name
     */
    public String user;

    /**
     * The configured password
     */
    public String password;

    /**
     * Configured refresh rate
     */
    public int refresh;

}
