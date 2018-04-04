/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.property;

/**
 * Property wrapper for valid Gardena JSON serialization.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SimplePropertiesWrapper {
    private SimpleProperties properties;

    public SimplePropertiesWrapper() {
    }

    public SimplePropertiesWrapper(SimpleProperties properties) {
        this.properties = properties;
    }

    /**
     * Returns the simple property.
     */
    public SimpleProperties getProperties() {
        return properties;
    }

    /**
     * Sets the simple property.
     */
    public void setProperties(SimpleProperties properties) {
        this.properties = properties;
    }
}
