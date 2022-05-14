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
package org.openhab.voice.rustpotterks.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RustpotterKSConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class RustpotterKSConfiguration {
    public float threshold = 0.5f;
    public float averagedThreshold = 0.0f;
    public float comparatorRef = 0.22f;
    public int comparatorBandSize = 6;
    public float vadSensitivity = 0.5f;
    public int vadDelay = 3;
    public String vadMode = "disabled";
    public boolean eagerMode = true;
}
