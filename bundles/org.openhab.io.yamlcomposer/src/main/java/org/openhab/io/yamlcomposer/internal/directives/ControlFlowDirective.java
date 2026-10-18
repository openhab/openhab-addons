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
 * The {@link ControlFlowDirective} interface represents a structural control-flow directive
 * in the YAML composition process (e.g., conditionals and loops).
 * <p>
 * Unlike scope-modifying directives (such as {@link VarDirective}), control-flow directives
 * manipulate list cardinality or map structure by conditionally including or repeating elements.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public sealed interface ControlFlowDirective
        extends Directive permits IfDirective, ElseIfDirective, ElseDirective, ForDirective {
}
