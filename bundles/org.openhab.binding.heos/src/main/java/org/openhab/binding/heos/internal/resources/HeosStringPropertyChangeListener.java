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
package org.openhab.binding.heos.internal.resources;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosStringPropertyChangeListener} provides the possibility
 * to add a listener to a String and get informed about the new value.
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosStringPropertyChangeListener {
    private final Logger logger = LoggerFactory.getLogger(HeosStringPropertyChangeListener.class);

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        pcs.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void setValue(String newValue) {
        logger.debug("Firing property change: {} {}", newValue, Thread.currentThread());
        pcs.firePropertyChange("value", null, newValue);
    }
}
