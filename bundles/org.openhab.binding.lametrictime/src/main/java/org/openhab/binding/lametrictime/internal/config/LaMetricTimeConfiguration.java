/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
 * Configuration class for LaMetric Time device.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class LaMetricTimeConfiguration {

    public static final String HOST = "host";
    public static final String API_KEY = "apiKey";

    public String host;
    public String apiKey;
}
