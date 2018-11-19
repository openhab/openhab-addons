/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smartmeter.internal;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Listener which can be notified whenever new values are read form a meter device.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public interface MeterValueListener {

    /**
     * Called whenever some (reading-) error occurred.
     *
     * @param e The Exception that was thrown.
     */
    public void errorOccurred(Throwable e);

    /**
     * Called whenever some value was added or changed for a meter device.
     *
     * @param value The changed value.
     */
    public <Q extends Quantity<Q>> void valueChanged(MeterValue<Q> value);

    /**
     * Called whenever some value was removed from the meter device (not available anymore).
     *
     * @param value The removed value.
     */
    public <Q extends Quantity<Q>> void valueRemoved(MeterValue<Q> value);
}
