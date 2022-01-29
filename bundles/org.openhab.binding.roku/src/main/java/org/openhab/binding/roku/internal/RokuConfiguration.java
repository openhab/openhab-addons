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
package org.openhab.binding.roku.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RokuConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RokuConfiguration {
    public @Nullable String hostName;
    public Integer port = 8060;
    public Integer refresh = 10;
}
