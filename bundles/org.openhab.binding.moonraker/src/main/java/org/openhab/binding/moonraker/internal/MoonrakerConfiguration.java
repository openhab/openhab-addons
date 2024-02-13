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
package org.openhab.binding.moonraker.internal;

/**
 * The {@link MoonrakerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Arjan Mels - Initial contribution
 */
public class MoonrakerConfiguration {

    /** Host IP address or name */
    public String host;
    /** Host port */
    public int port;
    /** Optional API key */
    public String apikey;
}
