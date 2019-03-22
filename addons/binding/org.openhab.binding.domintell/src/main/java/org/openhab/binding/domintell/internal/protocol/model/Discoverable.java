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
package org.openhab.binding.domintell.internal.protocol.model;

/**
 * The {@link Discoverable} base class is used by modules and groups for discovery support.
 *
 * @author Gabor Bicskekei - Initial contribution
 */
public abstract class Discoverable {
    /**
     * True if a handler was already created
     */
    private boolean discovered;

    public abstract boolean isDiscoverable();

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }
}
