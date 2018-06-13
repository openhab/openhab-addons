/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws.datatypes;

/**
 * IHC WSResourceValue data value.
 *
 * @author Pauli Anttila
 * @since 1.5.0
 */
public class WSResourceValue {

    protected int resourceID;

    WSResourceValue() {

    }

    WSResourceValue(int resourceID) {
        this.resourceID = resourceID;
    }

    /**
     * Gets the value of the resource ID property.
     *
     */
    public int getResourceID() {
        return resourceID;
    }

    /**
     * Sets the value of the resource ID property.
     *
     */
    public void setResourceID(int value) {
        this.resourceID = value;
    }
}
