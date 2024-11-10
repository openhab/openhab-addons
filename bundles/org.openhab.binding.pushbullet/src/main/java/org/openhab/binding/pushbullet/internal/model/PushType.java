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
package org.openhab.binding.pushbullet.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the push type.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum PushType {
    @SerializedName("note")
    NOTE,
    @SerializedName("link")
    LINK,
    @SerializedName("file")
    FILE
}
