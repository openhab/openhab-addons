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
package org.openhab.binding.ihc.internal.ws.datatypes;

/**
 * Class for WSUserGroup complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSUserGroup {

    protected String type;

    public WSUserGroup() {
    }

    public WSUserGroup(String type) {
        this.type = type;
    }

    /**
     * Gets the value of the type property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setType(String value) {
        this.type = value;
    }
}
