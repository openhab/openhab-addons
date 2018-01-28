/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.config;

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
