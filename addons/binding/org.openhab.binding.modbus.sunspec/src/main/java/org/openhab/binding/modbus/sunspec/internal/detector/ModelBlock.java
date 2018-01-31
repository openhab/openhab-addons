/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal.detector;

/**
 * Descriptor for a model block found on the device
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
public class ModelBlock {

    /**
     * Base address of this block in 16bit words
     */
    public int address;

    /**
     * Length of this block in 16bit words
     */
    public int length;

    /**
     * Module identifier
     */
    public int moduleID;

    @Override
    public String toString() {
        return String.format("ModelBlock type=%d address=%d length=%d", moduleID, address, length);
    }
}
