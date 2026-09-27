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
package org.openhab.io.yamlcomposer.internal.dynamic;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a discrete change event emitted by a dynamic source provider.
 *
 * @param source name of the source (e.g., "things")
 * @param oldEntity map representation of the entity before change, or null if added
 * @param newEntity map representation of the entity after change, or null if removed
 */
@NonNullByDefault
public record EntityChange(String source, @Nullable Map<String, @Nullable Object> oldEntity,
        @Nullable Map<String, @Nullable Object> newEntity) {
}
