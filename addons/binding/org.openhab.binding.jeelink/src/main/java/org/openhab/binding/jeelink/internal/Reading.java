/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal;

/**
 * Interface for a Reading from a Sensor. Addiionally provided basic arithmetic operations needed
 * for computing average values for readings.
 *
 * @author Volker Bier - Initial contribution
 */
public interface Reading {
    /**
     * @return the sensor ID of the sensor that provided this reading.
     */
    String getSensorId();
}
