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
package org.openhab.binding.tr064.internal.phonebook;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link PhonebookProvider} interface provides methods to lookup a phone number from a phonebook
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface PhonebookProvider {

    Optional<Phonebook> getPhonebookByName(String name);

    Collection<Phonebook> getPhonebooks();

    ThingUID getUID();

    String getFriendlyName();
}
