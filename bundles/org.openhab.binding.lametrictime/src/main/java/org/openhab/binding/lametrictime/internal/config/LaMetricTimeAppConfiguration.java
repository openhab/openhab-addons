/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Configuration class for LaMetric Time apps.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeAppConfiguration {

    public static final String PACKAGE_NAME = "packageName";
    public static final String WIDGET_ID = "widgetId";
    public static final String ACCESS_TOKEN = "accessToken";

    @Nullable
    public String packageName;

    @Nullable
    public String widgetId;

    @Nullable
    public String accessToken;
}
