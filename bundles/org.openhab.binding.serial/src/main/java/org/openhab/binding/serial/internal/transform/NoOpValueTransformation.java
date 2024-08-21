/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.serial.internal.transform;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NoOpValueTransformation} implements a no-op (identity) transformation
 *
 * @author Jan N. Klug - Initial contribution
 * @author Mike Major - Copied from HTTP binding to provide consistent user experience
 */
@NonNullByDefault
public class NoOpValueTransformation implements ValueTransformation {
    private static final NoOpValueTransformation NO_OP_VALUE_TRANSFORMATION = new NoOpValueTransformation();

    @Override
    public Optional<String> apply(final String value) {
        return Optional.of(value);
    }

    /**
     * get the static value transformation for identity
     *
     * @return
     */
    public static ValueTransformation getInstance() {
        return NO_OP_VALUE_TRANSFORMATION;
    }
}
