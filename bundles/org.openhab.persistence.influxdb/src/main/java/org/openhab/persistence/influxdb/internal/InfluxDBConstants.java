/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants used by this addon
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDBConstants {
    public static final String COLUMN_VALUE_NAME_V1 = "value";
    public static final String COLUMN_VALUE_NAME_V2 = "_value";

    public static final String COLUMN_TIME_NAME_V1 = "time";
    public static final String COLUMN_TIME_NAME_V2 = "_time";

    public static final String FIELD_VALUE_NAME = "value";
    public static final String TAG_ITEM_NAME = "item";
    public static final String TAG_CATEGORY_NAME = "category";
    public static final String TAG_TYPE_NAME = "type";
    public static final String TAG_LABEL_NAME = "label";
    public static final String FIELD_MEASUREMENT_NAME = "_measurement";
}
