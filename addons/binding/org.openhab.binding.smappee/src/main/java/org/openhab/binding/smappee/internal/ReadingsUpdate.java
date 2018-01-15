/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smappee.internal;

/**
 * Interface for readings updates, what should be implemented to be a smappee handler.
 *
 * @author Niko Tanghe - Initial contribution
 */
public interface ReadingsUpdate {

    /**
     * log a new smappee device reading.
     *
     * @param devicereadings the devicereadings to set as a new state.
     */
    void newState(SmappeeDeviceReading devicereadings);
}