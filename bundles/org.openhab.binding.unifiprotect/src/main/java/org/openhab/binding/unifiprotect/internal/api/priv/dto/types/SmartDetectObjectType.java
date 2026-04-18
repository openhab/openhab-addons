/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.types;

import com.google.gson.annotations.SerializedName;

/**
 * Smart detection object types
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum SmartDetectObjectType {
    @SerializedName("person")
    PERSON,

    @SerializedName("vehicle")
    VEHICLE,

    @SerializedName("package")
    PACKAGE,

    @SerializedName("animal")
    ANIMAL,

    @SerializedName("licensePlate")
    LICENSE_PLATE,

    @SerializedName("face")
    FACE,

    @SerializedName("pet")
    PET,

    @SerializedName("bark")
    BARK,

    @SerializedName("smoke")
    SMOKE,

    @SerializedName("cmonx")
    CO_ALARM,

    @SerializedName("siren")
    SIREN,

    @SerializedName("babyc")
    BABY_CRY,

    @SerializedName("speak")
    SPEAK,

    @SerializedName("carAlarm")
    CAR_ALARM,

    @SerializedName("carHorn")
    CAR_HORN,

    @SerializedName("glassBreak")
    GLASS_BREAK
}
