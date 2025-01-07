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
package org.openhab.binding.govee.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link GoveeConfiguration} contains thing values that are used by the Thing Handler
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeConfiguration {

    public String hostname = "";
    public int refreshInterval = 5; // in seconds

    public @Nullable Integer minKelvin;
    public @Nullable Integer maxKelvin;
}
