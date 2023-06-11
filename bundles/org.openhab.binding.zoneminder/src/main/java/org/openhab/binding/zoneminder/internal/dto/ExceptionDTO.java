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
package org.openhab.binding.zoneminder.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ExceptionDTO} is responsible for
 *
 * @author Mark Hilbush - Initial contribution
 */
public class ExceptionDTO {

    /**
     * Class where the error occurred
     */
    @SerializedName("class")
    public String clazz;

    /**
     * Error code
     */
    @SerializedName("code")
    public String code;

    /**
     * Error message
     */
    @SerializedName("message")
    public String message;
}
