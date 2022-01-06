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
package org.openhab.binding.dominoswiss.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DominoswissConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
@NonNullByDefault
public class DominoswissConfiguration {

    /**
     * Server ip address
     */
    public String ipAddress = "localhost";
    /**
     * Server web port for REST calls
     */
    public int port = 1318;

    /**
     * Language for TTS has to be fix to EN as only English commands are allowed
     */
    public final String language = "EN";
}
