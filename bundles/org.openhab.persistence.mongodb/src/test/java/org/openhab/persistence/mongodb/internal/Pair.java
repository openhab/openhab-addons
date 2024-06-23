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
package org.openhab.persistence.mongodb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class provides helper methods to create test items.
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return left != null || right != null ? new Pair<>(left, right) : new Pair<>(null, null);
    }
}
