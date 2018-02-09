/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.util;

import java.util.Objects;

/**
 * Generic pair object.
 *
 * @author Mattias Markehed
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
