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
package org.openhab.binding.internal.kostal.inverter.secondgeneration;

/**
 * The {@link SecondGenerationSourceConfig} class defines constants, which are
 * used in the second generation part of the binding.
 *
 * @author Christian Schneider - Initial contribution
 * @author Örjan Backsell - Added parameters for configuration options Piko1020, Piko New Generation
 *
 */
public class SecondGenerationSourceConfig {
    public String url;
    public String userName;
    public String password;
    public int refreshInterval;
    public String dxsIdConf;
    public String valueConf;
}
