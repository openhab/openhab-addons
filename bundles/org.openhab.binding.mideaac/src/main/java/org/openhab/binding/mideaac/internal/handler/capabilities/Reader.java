/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.mideaac.internal.handler.capabilities;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Reader} reads the raw capability message and
 * breaks them down for further parsing.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class Reader {
    public final String name;
    public final Predicate<Integer> predicate;

    public Reader(String name, Predicate<Integer> predicate) {
        this.name = name;
        this.predicate = predicate;
    }
}
