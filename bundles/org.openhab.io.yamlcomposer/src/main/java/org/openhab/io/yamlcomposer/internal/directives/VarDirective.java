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
package org.openhab.io.yamlcomposer.internal.directives;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a {@code !var} directive used to declare scoped variables during YAML composition traversal.
 * <p>
 * Variable directives mutate the active variable scope for subsequent sibling entries
 * in the current evaluation context (such as maps or list-control items) and produce no direct
 * data entries in the final output.
 * <p>
 * Supported syntaxes:
 * <ul>
 * <li><b>{@link SingleForm}:</b> {@code !var name: value} — assigns a single variable where the
 * name is defined in the tag scalar argument and the value is provided by the entry value.</li>
 * <li><b>{@link MapForm}:</b> {@code !var:\n k1: v1\n k2: v2} — assigns multiple
 * variables from a mapping block.</li>
 * </ul>
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public sealed interface VarDirective extends Directive {

    /**
     * Represents a single-variable declaration using the scalar tag form (e.g., {@code !var name: value}).
     *
     * @param variableName the variable name declared in the tag scalar
     * @param sourceLocation the location in the source file for diagnostics
     */
    record SingleForm(String variableName, String sourceLocation) implements VarDirective {
    }

    /**
     * Represents a multi-variable declaration using a mapping block (e.g., {@code !var:\n k: v}).
     *
     * @param sourceLocation the location in the source file for diagnostics
     */
    record MapForm(String sourceLocation) implements VarDirective {
    }
}
