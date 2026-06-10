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
package org.openhab.io.yamlcomposer.internal.placeholders;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SubstitutionPlaceholder} represents a deferred string interpolation constructed from a <code>!sub</code>
 * node to be processed by the {@link org.openhab.io.yamlcomposer.internal.YamlComposer}.
 *
 * <p>
 * It preserves the raw scalar value and the optional variable name that defines delimiter pattern for
 * interpolation.
 *
 * @param value The raw string value containing variable interpolation patterns
 * @param patternName The variable name containing delimiter specification in the form <begin>..<end>
 * @param sourceLocation Description of the source location for logging purposes
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public record SubstitutionPlaceholder(String value, @Nullable String patternName,
        @NonNull String sourceLocation) implements Placeholder {
}
