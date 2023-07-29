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
package org.openhab.binding.monopriceaudio.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MonopriceAudioThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class MonopriceAudioThingConfiguration {
    public Integer numZones = 1;
    public Integer pollingInterval = 15;
    public @Nullable String serialPort;
    public @Nullable String host;
    public @Nullable Integer port;
    public @Nullable String ignoreZones;
    public Integer initialAllVolume = 1;
    public @Nullable String inputLabel1;
    public @Nullable String inputLabel2;
    public @Nullable String inputLabel3;
    public @Nullable String inputLabel4;
    public @Nullable String inputLabel5;
    public @Nullable String inputLabel6;
}
