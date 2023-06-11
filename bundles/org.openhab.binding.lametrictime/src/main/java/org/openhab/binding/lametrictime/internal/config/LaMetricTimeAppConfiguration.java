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
package org.openhab.binding.lametrictime.internal.config;

/**
 * Configuration class for LaMetric Time apps.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class LaMetricTimeAppConfiguration {

    public static final String PACKAGE_NAME = "packageName";
    public static final String WIDGET_ID = "widgetId";
    public static final String ACCESS_TOKEN = "accessToken";

    public String packageName;
    public String widgetId;
    public String accessToken;
}
