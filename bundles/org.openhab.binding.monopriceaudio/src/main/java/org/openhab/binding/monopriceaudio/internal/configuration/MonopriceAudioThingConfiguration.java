/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * The {@link MonopriceAudioThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class MonopriceAudioThingConfiguration {

    public @NonNullByDefault({}) Integer numZones;
    public @NonNullByDefault({}) Integer pollingInterval;
    public @NonNullByDefault({}) String serialPort;
    public @NonNullByDefault({}) String host;
    public @NonNullByDefault({}) Integer port;
    public @NonNullByDefault({}) String ignoreZones;
    public @NonNullByDefault({}) Integer initialAllVolume;
    public @NonNullByDefault({}) String inputLabel1;
    public @NonNullByDefault({}) String inputLabel2;
    public @NonNullByDefault({}) String inputLabel3;
    public @NonNullByDefault({}) String inputLabel4;
    public @NonNullByDefault({}) String inputLabel5;
    public @NonNullByDefault({}) String inputLabel6;

}
