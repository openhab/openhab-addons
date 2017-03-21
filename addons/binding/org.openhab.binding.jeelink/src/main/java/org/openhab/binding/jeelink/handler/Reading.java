/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

/**
 * Interface for a Reading from a Sensor. Addiionally provided basic arithmetic operations needed
 * for computing average values for readings.
 *
 * @author Volker Bier - Initial contribution
 */
public interface Reading<R extends Reading> {
    /**
     * @return the sensor ID of the sensor that provided this reading.
     */
    String getSensorId();

    /////////////////////////////////////////////////////////////////////////
    // Methods needed for computing the rolling average
    /////////////////////////////////////////////////////////////////////////

    /**
     * This adds the given readings values to the current ones (only the parts of
     * the reading that are needed for averaging).
     *
     * @return the combined reading
     */
    R add(R add);

    /**
     * This substracts the given readings values to the current ones (only the parts of
     * the reading that are needed for averaging).
     *
     * @return the combined reading
     */
    R substract(R remove);

    /**
     * This divides the current readings values by the given number (only the parts of the reading that
     * are needed for averaging).
     *
     * @return the combined reading
     */
    R divide(float number);

    /**
     * This multiplies the current readings values by the given number (only the parts of the reading that
     * are needed for averaging).
     *
     * @return the combined reading
     */
    R multiply(float number);
}
