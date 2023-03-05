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
package org.openhab.binding.dwdpollenflug.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for data per pollen type
 * 
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollentypeJSON {
    public String today = "-1";

    public String tomorrow = "-1";

    @SerializedName("dayafter_to")
    public String dayAfterTomorrow = "-1";
}
