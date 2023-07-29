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
package org.openhab.binding.lametrictime.internal.api.cloud.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for icon order.
 *
 * @author Gregory Moyer - Initial contribution
 */
public enum IconOrder {
    @SerializedName("popular")
    POPULAR,
    @SerializedName("newest")
    NEWEST,
    @SerializedName("title")
    TITLE
}
