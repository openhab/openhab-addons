/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal.phonebook;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Phonebook} interface is used by phonebook providers to implement phonebooks
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface Phonebook {

    /**
     * get the name of this phonebook
     *
     * @return
     */
    String getName();

    /**
     * lookup a number in this phonebook
     *
     * @param number the number
     * @param matchCount the number of matching digits, counting from far right
     * @return an Optional containing the name associated with this number (empty of not present)
     */
    Optional<String> lookupNumber(String number, int matchCount);
}
