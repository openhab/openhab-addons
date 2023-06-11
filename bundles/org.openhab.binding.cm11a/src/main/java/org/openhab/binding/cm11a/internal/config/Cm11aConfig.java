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
package org.openhab.binding.cm11a.internal.config;

/**
 * Configuration constants for Cm11A
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class Cm11aConfig {

    /**
     * Serial port of the CM11A device.
     */
    public String serialPort = null;

    /**
     * refresh rate - I don't think we will need this
     */
    public int refresh = 60;
}
