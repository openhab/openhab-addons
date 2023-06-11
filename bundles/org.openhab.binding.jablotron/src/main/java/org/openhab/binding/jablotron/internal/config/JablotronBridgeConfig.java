/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.jablotron.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link JablotronBridgeConfig} class defines the bridge configuration
 * object.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronBridgeConfig {
    private String login = "";
    private String password = "";
    private int refresh = 30;
    private String lang = "en";

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getLang() {
        return lang;
    }

    public int getRefresh() {
        return refresh;
    }
}
