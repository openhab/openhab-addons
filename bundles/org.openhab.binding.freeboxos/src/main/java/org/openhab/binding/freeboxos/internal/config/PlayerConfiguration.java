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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PlayerConfiguration} is responsible for holding
 * configuration informations needed to access/poll the freebox player
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PlayerConfiguration extends ClientConfiguration {
    public static final String PORT = "port";
    public static final String REMOTE_CODE = "remoteCode";
    public static final String PASSWORD = "password";
    public static final String CALLBACK_URL = "callBackUrl";

    public int port = 24322;
    public String password = "";
    public boolean acceptAllMp3 = true;
    public String remoteCode = "";
    public String callBackUrl = "";
}
