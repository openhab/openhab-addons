/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.persistence.victoriametrics.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants used by this addon
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@NonNullByDefault
public class VictoriaMetricsConstants {
    public static final String TAG_ITEM_NAME = "item";
    public static final String TAG_CATEGORY_NAME = "category";
    public static final String TAG_TYPE_NAME = "type";
    public static final String TAG_LABEL_NAME = "label";
    public static final String TAG_UNIT_NAME = "unit";
    public static final int QUERY_MAX_POINTS = 1000;
    public static final int QUERY_DEFAULT_STEP = 60; // seconds
    public static final long CONNECTION_HEARTBEAT_INTERVAL = 60 * 1000; // 1 minute
}
