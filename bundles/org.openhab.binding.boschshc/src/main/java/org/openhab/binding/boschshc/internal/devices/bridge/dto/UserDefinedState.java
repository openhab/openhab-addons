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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * Represents a single user-defined state defined on the Bosch Smart Home Controller.
 *
 * Example from Json:
 *
 * <pre>
 * {
 * "@type": "userDefinedState",
 * "id": "23d34fa6-382a-444d-8aae-89c706e22158",
 * "name": "atHome",
 * "state": false
 * }
 * </pre>
 *
 * @author Patrick Gell - Initial contribution
 */
public class UserDefinedState extends BoschSHCServiceState {

    private String id;
    private String name;
    private boolean state;

    public UserDefinedState() {
        super("userDefinedState");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "UserDefinedState{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", state=" + state + ", type='"
                + type + '\'' + '}';
    }

    public static boolean isValid(UserDefinedState obj) {
        return obj != null && obj.id != null;
    }
}
