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
package org.openhab.binding.jeelink.internal.config;

/**
 * Configuration for a Handler that is able to buffer values.
 *
 * @author Volker Bier - Initial contribution
 */
public class BufferedSensorConfig extends JeeLinkSensorConfig {
    public int updateInterval;
    public int bufferSize;
}
