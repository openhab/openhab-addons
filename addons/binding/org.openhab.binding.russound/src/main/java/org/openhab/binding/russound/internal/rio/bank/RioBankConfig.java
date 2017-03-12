/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.bank;

/**
 * Configuration class for the {@link RioBankHandler}
 *
 * @author Tim Roberts
 */
public class RioBankConfig {
    /**
     * ID of the bank within the source (should be 1-6)
     */
    private int bank;

    /**
     * Gets the bank identifier
     *
     * @return the bank identifier
     */
    public int getBank() {
        return bank;
    }

    /**
     * Sets the bank identifier
     *
     * @param bank the bank identifier
     */
    public void setBank(int bank) {
        this.bank = bank;
    }
}
