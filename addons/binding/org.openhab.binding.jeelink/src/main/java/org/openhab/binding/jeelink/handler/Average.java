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
 * Interface for computing an average of Readings.
 *
 * @author Volker Bier - Initial contribution
 */
public interface Average<R extends Reading> {
    /**
     * Adds a new reading to the current average.
     */
    void add(R reading);

    /**
     * @return the current average.
     */
    R getAverage();
}
