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
package org.openhab.persistence.victoriametrics.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants used by this addon
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 * @author Michael Weger - Add VictoriaMetrics API constants
 */
@NonNullByDefault
public class VictoriaMetricsConstants {
    public static final String LABEL_METRIC_NAME = "__name__";
    public static final String TAG_ITEM_NAME = "item";
    public static final String TAG_CATEGORY_NAME = "category";
    public static final String TAG_TYPE_NAME = "type";
    public static final String TAG_LABEL_NAME = "label";
    public static final String TAG_UNIT_NAME = "unit";
    public static final String TAG_COMPONENT_NAME = "component";

    public static final String PATH_HEALTH = "/health";
    public static final String PATH_IMPORT = "/api/v1/import/prometheus";
    public static final String PATH_EXPORT = "/api/v1/export";
    public static final String PATH_LABEL_VALUES = "/api/v1/label/%s/values";
    public static final String PATH_DELETE_SERIES = "/api/v1/admin/tsdb/delete_series";
}
