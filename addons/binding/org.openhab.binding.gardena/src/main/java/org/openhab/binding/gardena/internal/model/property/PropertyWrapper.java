/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.property;

import com.google.gson.annotations.SerializedName;

/**
 * Property wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PropertyWrapper {
    @SerializedName("properties")
    private BaseProperty property;

    public PropertyWrapper() {
    }

    public PropertyWrapper(BaseProperty property) {
        this.property = property;
    }

    /**
     * Returns the property.
     */
    public BaseProperty getProperty() {
        return property;
    }

    /**
     * Sets the property.
     */
    public void setProperties(BaseProperty property) {
        this.property = property;
    }
}
