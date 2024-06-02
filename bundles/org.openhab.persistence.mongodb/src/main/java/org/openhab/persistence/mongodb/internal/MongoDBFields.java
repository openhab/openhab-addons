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
package org.openhab.persistence.mongodb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class defines constant field names used in MongoDB documents.
 * These field names are used to ensure consistent access to document properties.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public final class MongoDBFields {
    public static final String FIELD_ID = "_id";
    public static final String FIELD_ITEM = "item";
    public static final String FIELD_REALNAME = "realName";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_VALUE = "value";
    public static final String FIELD_UNIT = "unit";
    public static final String FIELD_VALUE_DATA = "value.data";
    public static final String FIELD_VALUE_TYPE = "value.type";

    private MongoDBFields() {
        // Private constructor to prevent instantiation
    }
}
