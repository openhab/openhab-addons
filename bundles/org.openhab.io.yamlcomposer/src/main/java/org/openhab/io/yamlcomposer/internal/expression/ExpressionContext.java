/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.yamlcomposer.internal.expression;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public record ExpressionContext( //
        Map<String, @Nullable Object> variables, //
        Consumer<String> envVarCallback, //
        Runnable itemAccessCallback, //
        Runnable thingAccessCallback, //
        Supplier<Map<String, Map<String, @Nullable Object>>> itemsSupplier, //
        Supplier<Map<String, Map<String, @Nullable Object>>> thingsSupplier //
) {

    /**
     * Returns a new ExpressionContext sharing the same callbacks and suppliers,
     * but with a new variable map (useful for loop or conditional scopes).
     */
    public ExpressionContext withVariables(Map<String, @Nullable Object> newVariables) {
        return new ExpressionContext(newVariables, envVarCallback, itemAccessCallback, thingAccessCallback,
                itemsSupplier, thingsSupplier);
    }
}
