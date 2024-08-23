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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link TvSettingsUpdateDTO} class defines the Data Transfer Object
 * for the Philips TV API /menuitems/settings/update endpoint to update settings of the tv, e.g. turning on/off
 * ambilight hue power.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
public class TvSettingsUpdateDTO {

    @JsonProperty
    private List<ValuesDTO> values;

    public TvSettingsUpdateDTO() {
    }

    public TvSettingsUpdateDTO(List<ValuesDTO> values) {
        this.values = values;
    }

    public void setValues(List<ValuesDTO> values) {
        this.values = values;
    }

    public List<ValuesDTO> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "TvSettingsDTO{" + "values = '" + values + '\'' + "}";
    }
}
