/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
