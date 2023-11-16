/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.russound.internal.rio.models;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Simple model of a RIO Bank and it's attributes. Please note this class is used to serialize/deserialize to JSON.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioBank {
    /**
     * The Bank ID
     */
    private final int id;

    /**
     * The Bank Name
     */
    private final AtomicReference<String> name = new AtomicReference<>(null);

    /**
     * Create the object from the given ID (using the default name of "Bank" + id)
     *
     * @param id a bank identifier between 1 and 6
     * @throws IllegalArgumentException if id is {@literal <} 1 or > 6
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
     * @throws IllegalArgumentException if id is {@literal < 1} or > 6
     */
    public RioBank(int id, @Nullable String name) {
        if (id < 1 || id > 6) {
            throw new IllegalArgumentException("Bank ID can only be between 1 and 6");
        }
        this.id = id;
        this.name.set(name == null || name.isEmpty() ? "Bank " + id : name);
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
    public void setName(@Nullable String bankName) {
        name.set(bankName == null || bankName.isEmpty() ? "Bank " + getId() : bankName);
    }
}
