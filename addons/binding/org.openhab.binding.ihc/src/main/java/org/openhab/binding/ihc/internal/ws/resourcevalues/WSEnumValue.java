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
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSEnumValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSEnumValue extends WSResourceValue {

    protected int definitionTypeID;
    protected int enumValueID;
    protected String enumName;

    public WSEnumValue() {
    }

    public WSEnumValue(int resourceID) {
        super(resourceID);
    }

    public WSEnumValue(int resourceID, int definitionTypeID, int enumValueID, String enumName) {
        super(resourceID);
        this.definitionTypeID = definitionTypeID;
        this.enumValueID = enumValueID;
        this.enumName = enumName;
    }

    /**
     * Gets the value of the definitionTypeID property.
     *
     */
    public int getDefinitionTypeID() {
        return definitionTypeID;
    }

    /**
     * Sets the value of the definitionTypeID property.
     *
     */
    public void setDefinitionTypeID(int value) {
        this.definitionTypeID = value;
    }

    /**
     * Gets the value of the enumValueID property.
     *
     */
    public int getEnumValueID() {
        return enumValueID;
    }

    /**
     * Sets the value of the enumValueID property.
     *
     */
    public void setEnumValueID(int value) {
        this.enumValueID = value;
    }

    /**
     * Gets the value of the enumName property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEnumName() {
        return enumName;
    }

    /**
     * Sets the value of the enumName property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEnumName(String value) {
        this.enumName = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, definitionTypeID=%d, enumValueID=%d, enumName=%s]", super.resourceID,
                definitionTypeID, enumValueID, enumName);
    }
}
