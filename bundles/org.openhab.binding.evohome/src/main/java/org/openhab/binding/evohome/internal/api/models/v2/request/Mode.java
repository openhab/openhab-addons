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
package org.openhab.binding.evohome.internal.api.models.v2.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for the mode
 *
 * @author Jasper van Zuijlen - Initial contribution
 *
 */
public class Mode {

    Mode(String mode) {
        systemMode = mode;
        timeUntil = null;
        permanent = true;
    }

    Mode(String mode, int day, int month, int year) {
        systemMode = mode;
        timeUntil = String.format("%s-%s-%sT00:00:00Z", year, month, day);
        permanent = false;
    }

    @SerializedName("systemMode")
    private String systemMode;

    @SerializedName("timeUntil")
    private String timeUntil;

    @SerializedName("permanent")
    private boolean permanent;
}
