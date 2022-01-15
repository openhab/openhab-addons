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
package org.openhab.binding.http.internal.transform;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ValueTransformation} applies a set of transformations to a value
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface ValueTransformation {

    /**
     * applies the value transformation to a value
     *
     * @param value The value
     * @return Optional of string representing the transformed value (empty if transformation not present or failed)
     */
    Optional<String> apply(String value);
}
