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
