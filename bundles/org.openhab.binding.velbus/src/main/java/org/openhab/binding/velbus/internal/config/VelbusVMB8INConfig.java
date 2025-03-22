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
package org.openhab.binding.velbus.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VelbusVMB8INConfig} class represents the configuration of a Velbus VMB8IN module.
 *
 * @author Daniel Rosengarten - Initial contribution
 */
@NonNullByDefault
public class VelbusVMB8INConfig extends VelbusSensorConfig {
    public String counter1Unit = "";
    public String counter2Unit = "";
    public String counter3Unit = "";
    public String counter4Unit = "";
    public String counter5Unit = "";
    public String counter6Unit = "";
    public String counter7Unit = "";
    public String counter8Unit = "";
}
