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
package org.openhab.binding.netatmo.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BodyResponse} models a response returned by API call containing
 * a list of elements.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class BodyResponse<T extends NAObject> {
    @SerializedName(value = "home")
    private @Nullable T element;

    public @Nullable T getElement() {
        return element;
    }
}
