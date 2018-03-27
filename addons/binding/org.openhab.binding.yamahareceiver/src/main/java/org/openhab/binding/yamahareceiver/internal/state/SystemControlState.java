/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

/**
 * System AVR state (system power, etc)
 *
 * @author David Graeff - Initial contribution
 *
 */
public class SystemControlState implements Invalidateable {
    public boolean power = false;

    // If we lost the connection, invalidate the state.
    public void invalidate() {
        power = false;
    }
}
