/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;

/**
 * The {@link SedifBridgeConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SedifBridgeConfiguration extends Configuration {
    public String username = "";
    public String password = "";

    public boolean seemsValid() {
        return !username.isBlank() && !password.isBlank();
    }
}
