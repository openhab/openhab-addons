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
package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 mirek schema.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class MirekSchema {
    private static final int MIN = 153;
    private static final int MAX = 500;

    private @SerializedName("mirek_minimum") int mirekMinimum = MIN;
    private @SerializedName("mirek_maximum") int mirekMaximum = MAX;

    public int getMirekMinimum() {
        return mirekMinimum;
    }

    public int getMirekMaximum() {
        return mirekMaximum;
    }
}
