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
 * Supported syntax:
 * <ul>
 * <li>{@code !var name: value} — assigns a single variable where the variable name is defined in the
 * tag scalar argument and the value is provided by the mapping entry value.</li>
 * </ul>
 *
 * @param variableName the variable name declared in the tag scalar
 * @param sourceLocation the location in the source file for diagnostics
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record VarDirective(String variableName, String sourceLocation) implements Directive {
}
