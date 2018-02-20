/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.models;

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;

/**
 * Simple model of a RIO Bank and it's attributes. Please note this class is used to serialize/deserialize to JSON.
 *
 * @author Tim Roberts
 *
 */
public class RioBank {
    /**
     * The Bank ID
     */
    private final int id;

    /**
     * The Bank Name
     */
    private final AtomicReference<String> name = new AtomicReference<String>(null);

    /**
     * Create the object from the given ID (using the default name of "Bank" + id)
     *
     * @param id a bank identifier between 1 and 6
     * @throws IllegalArgumentException if id is < 1 or > 6
     */
    public RioBank(int id) {
        this(id, null);
    }

    /**
     * Create the object from the given ID and given name. If the name is empty or null, the name will default to ("Bank
     * " + id)
     *
     * @param id a bank identifier between 1 and 6
     * @param name a possibly null, possibly empty bank name (null or empty will result in a bank name of "Bank "+ id)
     * @throws IllegalArgumentException if id is < 1 or > 6
     */
    public RioBank(int id, String name) {
        if (id < 1 || id > 6) {
            throw new IllegalArgumentException("Bank ID can only be between 1 and 6");
        }
        this.id = id;
        this.name.set(StringUtils.isEmpty(name) ? "Bank " + id : name);
    }

    /**
     * Returns the bank identifier
     *
     * @return the bank identifier between 1 and 6
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the bank name
     *
     * @return a non-null, non-empty bank name
     */
    public String getName() {
        return name.get();
    }

    /**
     * Sets the bank name. If empty or a null, name defaults to "Bank " + getId()
     *
     * @param bankName a possibly null, possibly empty bank name
     */
    public void setName(String bankName) {
        name.set(StringUtils.isEmpty(bankName) ? "Bank " + getId() : bankName);
    }
}
