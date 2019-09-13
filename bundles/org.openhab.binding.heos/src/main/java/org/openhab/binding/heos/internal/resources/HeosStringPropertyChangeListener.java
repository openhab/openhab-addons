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
package org.openhab.binding.heos.internal.resources;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The {@Link HeosStringPropertyChangeListener} provides the possibility
 * to add a listener to an String and get informed about the new value.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosStringPropertyChangeListener {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private String value;

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String newValue) {
        String oldValue = this.value;
        value = newValue;
        pcs.firePropertyChange("value", oldValue, newValue);
        value = null;
    }
}
