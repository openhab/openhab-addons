/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal.json.obj;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class holding the Data JSON element
 *
 * @author Moritz 'Morty' Strübe - Initial contribution
 */
@NonNullByDefault
public class JsonResponse {
    public Header header = new Header();
    public Data data = new Data();
    public String status = "Not Set (JSON)";
    @SerializedName("Status code")
    public Integer statusCode = -7337;
}
