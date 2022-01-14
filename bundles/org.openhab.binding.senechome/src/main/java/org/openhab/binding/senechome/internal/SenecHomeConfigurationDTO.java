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
package org.openhab.binding.senechome.internal;

/**
 * The {@link SenecHomeConfigurationDTO} class contains fields mapping thing configuration parameters.
 *
 * @author Steven Schwarznau - Initial contribution
 */
public class SenecHomeConfigurationDTO {
    public String hostname;
    public int refreshInterval = 15;
    public int limitationTresholdValue = 95;
    public int limitationDuration = 120;
}
