/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.resourcevalues;

/**
 * Class for WSBooleanValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSBooleanValue extends WSResourceValue {

    protected boolean value;

    public WSBooleanValue() {
    }

    public WSBooleanValue(int resourceID) {
        super(resourceID);
    }

    public WSBooleanValue(int resourceID, boolean value) {
        super(resourceID);
        this.value = value;
    }

    /**
     * Gets the value of the value property.
     *
     */
    public boolean isValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     */
    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, value=%b]", super.resourceID, value);
    }
}
