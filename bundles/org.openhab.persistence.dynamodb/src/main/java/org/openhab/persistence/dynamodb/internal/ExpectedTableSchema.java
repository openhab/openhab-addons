/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.dynamodb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Expected revision of the DynamoDB schema
 *
 * NEW: Read and create data using new schemas
 * LEGACY: Read and create data using old schemas, compatible with first version of DynamoDB persistence addon
 * MAYBE_LEGACY: Try to read and create data using old schemas, but fallback to NEW if the old tables do not exist.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public enum ExpectedTableSchema {
    NEW,
    LEGACY,
    MAYBE_LEGACY;

    public boolean isFullyResolved() {
        return this != ExpectedTableSchema.MAYBE_LEGACY;
    }
}
