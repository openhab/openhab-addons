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
package org.openhab.binding.linky.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkyBridgeWebConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public class LinkyBridgeWebConfiguration extends LinkyBridgeConfiguration {
    public static final String INTERNAL_AUTH_ID = "internalAuthId";

    public String username = "";
    public String password = "";
    public String internalAuthId = "";

    @Override
    public boolean seemsValid() {
        return !username.isBlank() && !password.isBlank() && !internalAuthId.isBlank();
    }
}
