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
package org.openhab.binding.vizio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link VizioConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class VizioConfiguration {
    public @Nullable String hostName;
    public Integer port = 7345;
    public @Nullable String authToken;
    public @Nullable String appListJson;
}
