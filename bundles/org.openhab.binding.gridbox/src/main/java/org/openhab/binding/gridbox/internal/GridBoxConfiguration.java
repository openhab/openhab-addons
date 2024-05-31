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
package org.openhab.binding.gridbox.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GridBoxConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class GridBoxConfiguration {

    @Nullable
    public String email;

    @Nullable
    public String password;

    public int refreshInterval = 5;

    @Nullable
    public String systemId;

    @Nullable
    public String idToken;
}
