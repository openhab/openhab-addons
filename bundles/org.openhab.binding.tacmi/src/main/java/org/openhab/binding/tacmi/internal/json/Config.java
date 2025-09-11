/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class holding the configuration of the TA Json binding.
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 *
 */
@NonNullByDefault
public class Config {

    /**
     * host address of the C.M.I.
     */
    public String host = "";

    /**
     * Username
     */
    public String username = "";

    /**
     * Password
     */
    public String password = "";

    /**
     * Node-ID
     */
    public Integer nodeId = 1;

    /**
     * Json-Params
     */
    public String params = "I,O,SG";

    /**
     * API page poll interval
     */
    public int pollInterval = 60;
}
