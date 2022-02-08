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
package org.openhab.binding.lgthinq.lgservices.model.washer;

import static org.openhab.binding.lgthinq.lgservices.model.washer.WasherDryer.EMPTY_WASHER_DRYER;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link Data}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class Data {
    public static final Data EMPTY_DATA = new Data();
    @JsonProperty("washerDryer")
    private WasherDryer washerdryer = EMPTY_WASHER_DRYER;

    public void setWasherDryer(WasherDryer washerdryer) {
        this.washerdryer = washerdryer;
    }

    public WasherDryer getWasherDryer() {
        return washerdryer;
    }
}
