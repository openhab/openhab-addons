/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.rio.bank;

/**
 * Configuration class for the {@link RioBankHandler}
 *
 * @author Tim Roberts
 * @version $Id: $Id
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
