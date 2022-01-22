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
package org.openhab.voice.porcupineks.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PorcupineKSConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class PorcupineKSConfiguration {

    /**
     * Api key to use porcupine
     */
    public String apiKey = "";
    /**
     * A higher sensitivity reduces miss rate at cost of increased false alarm rate
     */
    public float sensitivity = 0.5f;
}
