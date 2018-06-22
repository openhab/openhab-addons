/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.resourcevalues;

/**
 * Java class for WSPhoneNumberValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSPhoneNumberValue extends WSResourceValue {

    protected String number;

    public WSPhoneNumberValue() {
    }

    public WSPhoneNumberValue(int resourceID) {
        super(resourceID);
    }

    public WSPhoneNumberValue(int resourceID, String number) {
        super(resourceID);
        this.number = number;
    }

    /**
     * Gets the value of the number property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getNumber() {
        return number;
    }

    /**
     * Sets the value of the number property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setNumber(String value) {
        this.number = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, number=%s]", super.resourceID, number);
    }
}
