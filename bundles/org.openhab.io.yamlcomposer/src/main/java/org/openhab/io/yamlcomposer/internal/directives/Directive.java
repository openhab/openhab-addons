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
 * The {@link Directive} interface represents a preprocessor directive attached to a key or item
 * during YAML composition.
 * <p>
 * Directives are evaluated during structure traversal (delegated via {@code DirectiveProcessor})
 * after key expressions are evaluated, directly altering how adjacent values or child structures
 * are processed.
 * <p>
 * The hierarchy is divided into two primary categories:
 * <ul>
 * <li>{@link ControlFlowDirective}: Structural directives that alter control flow and unrolling (e.g., {@code !if},
 * {@code !for}).</li>
 * <li>{@link VarDirective}: Scope directives that declare local variables without producing output data nodes.</li>
 * </ul>
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public sealed interface Directive permits ControlFlowDirective, VarDirective {
    String sourceLocation();
}
