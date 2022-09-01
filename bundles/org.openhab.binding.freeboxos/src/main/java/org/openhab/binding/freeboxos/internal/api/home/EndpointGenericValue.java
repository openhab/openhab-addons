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
package org.openhab.binding.freeboxos.internal.api.home;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EndpointGenericValue} is a Java class used to map the
 * structure used by the home API
 *
 * @param <T> Endpoint value type
 *
 * @author ben12 - Initial contribution
 */
@NonNullByDefault
public class EndpointGenericValue<T> {

    private T value;

    public EndpointGenericValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
