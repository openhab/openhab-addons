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
package org.openhab.binding.heos.internal.json;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Object used for the initial JSON parsing of the result
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosOption {
    public int id;
    @SerializedName("scid")
    public @Nullable Integer criteriaId;
    public @Nullable String name;
}
