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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.application;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link LaunchAppDTO} and {@link LaunchAppDTO}
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */

public class IntentDTO {

    @JsonProperty("component")
    private ComponentDTO component;

    @JsonProperty("action")
    private String action = "";

    @JsonProperty("extras")
    private ExtrasDTO extras;

    public IntentDTO() {
    }

    public IntentDTO(ComponentDTO component, ExtrasDTO extras) {
        this.component = component;
        this.extras = extras;
    }

    public void setComponent(ComponentDTO component) {
        this.component = component;
    }

    public ComponentDTO getComponent() {
        return component;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setExtras(ExtrasDTO extras) {
        this.extras = extras;
    }

    public ExtrasDTO getExtras() {
        return extras;
    }

    @Override
    public String toString() {
        return "Intent{" + "component = '" + component + '\'' + ",action = '" + action + '\'' + ",extras = '" + extras
                + '\'' + "}";
    }
}
