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
package org.openhab.binding.networkupstools.internal.nut;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Function interface that can throw a {@link NutException}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 *
 * @param <T> The type returned by the function
 * @param <R> The type of the input value of the function
 */
@FunctionalInterface
@NonNullByDefault
public interface NutFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    @Nullable
    R apply(T t) throws NutException;
}
