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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ForDirective} record represents a structural directive in the YAML composition process
 * that allows for iteration over a collection or map, supporting arbitrary list unpacking and destructuring.
 *
 * @param variables the list of iteration variable names
 *            (e.g., [item] for simple iteration, [item, index] to include an index for lists,
 *            [key, value] for maps, or multi-element tuples)
 * @param target the target collection, map, or expression to iterate over
 * @param filterCondition the optional condition expression used to filter items during iteration, or {@code null}
 * @param sourceLocation the location in the source file where the directive originated
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public record ForDirective(List<String> variables, @Nullable Object target, @Nullable String filterCondition,
        String sourceLocation) implements ControlFlowDirective {
}
