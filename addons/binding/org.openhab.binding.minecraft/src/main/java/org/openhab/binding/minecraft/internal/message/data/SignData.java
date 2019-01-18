/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal.message.data;

/**
 * Object representing a tracked Minecraft sign.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class SignData {

    private String name;
    private boolean state;

    /**
     * Creates a representation of sign.
     *
     * @param name text on sign.
     * @param state true if powered by redstone else false.
     */
    public SignData(String name, boolean state) {
        this.name = name;
        this.state = state;
    }

    /**
     * The text on sign.
     *
     * @return text name
     */
    public String getName() {
        return name;
    }

    /**
     * The active sign of state.
     *
     * @return true if powered by redstone
     */
    public boolean getState() {
        return state;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SignData other = (SignData) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
