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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class HomeState {
    @SerializedName("presence")
    private PresenceState presence = null;

    @SerializedName("name")
    private String name = null;

    @SerializedName("presenceLocked")
    private Boolean presenceLocked = null;

    @SerializedName("showHomePresenceSwitchButton")
    private Boolean showHomePresenceSwitchButton = null;

    public HomeState presence(PresenceState presence) {
        this.presence = presence;
        return this;
    }

    public PresenceState getPresence() {
        return presence;
    }

    public void setPresence(PresenceState presence) {
        this.presence = presence;
    }

    public String getName() {
        return name;
    }

    public Boolean isPresenceLocked() {
        return presenceLocked;
    }

    public Boolean isShowHomePresenceSwitchButton() {
        return showHomePresenceSwitchButton;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HomeState homeState = (HomeState) o;
        return Objects.equals(this.presence, homeState.presence) && Objects.equals(this.name, homeState.name)
                && Objects.equals(this.presenceLocked, homeState.presenceLocked)
                && Objects.equals(this.showHomePresenceSwitchButton, homeState.showHomePresenceSwitchButton);
    }

    @Override
    public int hashCode() {
        return Objects.hash(presence, name, presenceLocked, showHomePresenceSwitchButton);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HomeState {\n");

        sb.append("    presence: ").append(toIndentedString(presence)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    presenceLocked: ").append(toIndentedString(presenceLocked)).append("\n");
        sb.append("    showHomePresenceSwitchButton: ").append(toIndentedString(showHomePresenceSwitchButton))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
