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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Common abstraction for placeholders.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public interface Placeholder {
    /**
     * Source location string for logging and diagnostics.
     *
     * @return non-null source location
     */
    String sourceLocation();
}
