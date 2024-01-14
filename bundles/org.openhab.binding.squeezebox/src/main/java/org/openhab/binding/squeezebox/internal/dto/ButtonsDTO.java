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
package org.openhab.binding.squeezebox.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ButtonsDTO} contains information about the forward, rewind, repeat,
 * and shuffle buttons, including any custom definitions, such as replacing repeat
 * and shuffle with like and unlike, respectively.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ButtonsDTO {

    /**
     * Indicates if forward button is enabled/disabled,
     * or if there is a custom button definition.
     */
    @SerializedName("fwd")
    public ButtonDTO forward;

    /**
     * Indicates if rewind button is enabled/disabled,
     * or if there is a custom button definition.
     */
    @SerializedName("rew")
    public ButtonDTO rewind;

    /**
     * Indicates if repeat button is enabled/disabled,
     * or if there is a custom button definition.
     */
    @SerializedName("repeat")
    public ButtonDTO repeat;

    /**
     * Indicates if shuffle button is enabled/disabled,
     * or if there is a custom button definition.
     */
    @SerializedName("shuffle")
    public ButtonDTO shuffle;
}
