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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.ambilight;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link AmbilightModeDTO} class defines the Data Transfer Object
 * for the Philips TV API /ambilight/mode endpoint to retrieve or set the ambilight mode.
 * <p>
 * current (string): One of following values:
 * <p>
 * internal: The internal ambilight algorithm is used to calculate the ambilight colours.
 * <p>
 * manual: The cached ambilight colours are shown.
 * <p>
 * expert: The cached ambilight colours are used as input for the internal ambilight algorithm
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class AmbilightModeDTO {

    @JsonProperty("current")
    private String current = "";

    public AmbilightModeDTO() {
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "AmbilightModeDTO{" + "current = '" + current + '\'' + "}";
    }
}
