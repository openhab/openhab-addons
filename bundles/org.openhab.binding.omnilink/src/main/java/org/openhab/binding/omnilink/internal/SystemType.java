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
package org.openhab.binding.omnilink.internal;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SystemType} enum defines the two supported system types which can
 * interface with the binding
 *
 * @author Craig Hamilton - Initial contribution
 */
@NonNullByDefault
public enum SystemType {
    OMNI(16, 30, 38),
    LUMINA(36, 37);

    private final Set<Integer> modelNumbers;

    SystemType(Integer... modelNumbers) {
        this.modelNumbers = Set.of(modelNumbers);
    }

    public static Optional<SystemType> getType(int modelNumber) {
        return Arrays.stream(values()).filter(s -> s.modelNumbers.contains(modelNumber)).findFirst();
    }
}
