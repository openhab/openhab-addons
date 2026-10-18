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
 * The {@link ElseDirective} record represents a structural directive in the YAML composition process
 * that enables conditional inclusion of nodes based on an evaluated expression.
 *
 * @param sourceLocation the location in the source file where the directive originated
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record ElseDirective(String sourceLocation) implements ControlFlowDirective {
}
