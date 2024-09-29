/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.airvisualnode.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration for AirVisual Node.
 *
 * @author Victor Antonovich - Initial contribution
 */
@NonNullByDefault
public class AirVisualNodeConfig {

    public static final String ADDRESS = "address";

    public String address = "";

    public String username = "";

    public String password = "";

    public String share = "";

    public long refresh;

    public boolean isProVersion;
}
