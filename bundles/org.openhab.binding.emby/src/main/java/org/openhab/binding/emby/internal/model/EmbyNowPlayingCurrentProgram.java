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
package org.openhab.binding.emby.internal.model;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * embyPlayState - part of the model for the json object received from the server
 *
 * @author Zachary Christiansen - Initial Contribution
 *
 */
@NonNullByDefault
public class EmbyNowPlayingCurrentProgram {

    @SerializedName("RunTimeTicks")
    private BigDecimal runTimeTicks = BigDecimal.ZERO;

    /**
     * @return the run time of the item
     */
    BigDecimal getRunTimeTicks() {
        return runTimeTicks;
    }
}
