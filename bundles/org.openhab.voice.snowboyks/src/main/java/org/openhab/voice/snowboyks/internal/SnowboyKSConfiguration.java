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
package org.openhab.voice.snowboyks.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SnowboyKSConfiguration} class contains fields mapping service configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class SnowboyKSConfiguration {
    /**
     * Change audio gain, values less than one decrease and higher increase.
     */
    public float audioGain = 1.0f;
    /**
     * A higher sensitivity reduces miss rate at cost of increased false alarm rate
     */
    public String sensitivitiesString = "0.5";
    /**
     * Enables Audio Frontend
     */
    public boolean applyFrontend = false;
}
