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
package org.openhab.binding.smartmeter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DlmsMeterConfiguration} class contains fields mapping configuration parameters
 * for the IEC 62056-21 optical reader head for DLMS/COSEM meters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsMeterConfiguration {

    public String port = "/dev/ttyUSB0";
    public Integer refresh = 60;
}
