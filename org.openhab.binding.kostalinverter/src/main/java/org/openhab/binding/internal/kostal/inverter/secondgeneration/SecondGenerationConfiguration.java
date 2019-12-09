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
 * The {@link KostalInverterPikoNewGenConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 *
 */
public class SecondGenerationConfiguration {

    public static final long REFRESHINTERVAL = 60;

    public String dxsEntriesCfgFile;
    public String dxsEntriesCfgFileExt;
    public String dxsEntriesCfgFileExtExt;
    public Object url;
    public String username;
    public String password;
    public String dxsIdConf;
    public String valueConf;
}
