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
package org.openhab.binding.windhagerbiowin.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WindhagerbiowinConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author BenjiU - Initial contribution
 */
@NonNullByDefault
public class WindhagerbiowinConfiguration {

    /**
     * Hostname or IP address of the BioWin webserver.
     */
    public String hostname = "";

    /**
     * Optional port of the BioWin webserver. Defaults to 80.
     */
    public int port = 80;

    /**
     * Username for authenticating against the BioWin webserver.
     */
    public String username = "";

    /**
     * Password for authenticating against the BioWin webserver.
     */
    public String password = "";
}
