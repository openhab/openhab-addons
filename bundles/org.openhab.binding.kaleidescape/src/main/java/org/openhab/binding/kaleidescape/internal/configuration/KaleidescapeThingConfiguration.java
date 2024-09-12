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
package org.openhab.binding.kaleidescape.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link KaleidescapeThingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class KaleidescapeThingConfiguration {
    public @Nullable String serialPort;
    public @Nullable String host;
    public @Nullable Integer port;
    public @Nullable Integer updatePeriod;
    public boolean volumeEnabled;
    public Integer initialVolume = 0;
    public boolean volumeBasicEnabled;
    public boolean loadHighlightedDetails;
    public boolean loadAlbumDetails;
}
