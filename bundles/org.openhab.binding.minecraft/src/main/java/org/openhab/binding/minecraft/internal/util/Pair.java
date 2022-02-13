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
package org.openhab.binding.minecraft.internal.util;

import java.util.Objects;

/**
 * Generic pair object.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class Pair<T, R> {

    public final T first;
    public final R second;

    public Pair(T first, R second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) obj;
        return Objects.equals(p.first, first) && Objects.equals(p.second, second);
    }
}
