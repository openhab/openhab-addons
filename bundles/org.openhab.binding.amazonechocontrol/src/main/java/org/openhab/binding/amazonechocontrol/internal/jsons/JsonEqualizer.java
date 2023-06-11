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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonEqualizer} encapsulate the GSON data of the get equalizer command
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonEqualizer {
    public @Nullable Integer bass = 0;
    public @Nullable Integer mid = 0;
    public @Nullable Integer treble = 0;

    public JsonEqualizer createClone() {
        JsonEqualizer result = new JsonEqualizer();
        result.bass = this.bass;
        result.mid = this.mid;
        result.treble = this.treble;
        return result;
    }
}
