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
 * Class for WSTimerValue complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class WSTimerValue extends WSResourceValue {

    protected long milliseconds;

    public WSTimerValue() {
    }

    public WSTimerValue(int resourceID) {
        super(resourceID);
    }

    public WSTimerValue(int resourceID, long milliseconds) {
        super(resourceID);
        this.milliseconds = milliseconds;
    }

    /**
     * Gets the value of the milliseconds property.
     *
     */
    public long getMilliseconds() {
        return milliseconds;
    }

    /**
     * Sets the value of the milliseconds property.
     *
     */
    public void setMilliseconds(long value) {
        this.milliseconds = value;
    }

    @Override
    public String toString() {
        return String.format("[resourceId=%d, milliseconds=%b]", super.resourceID, milliseconds);
    }
}
