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
package org.openhab.binding.nest.internal.wwn.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The configuration for WWN structures.
 *
 * @author Wouter Born - Initial contribution
 * @author Wouter Born - Add device configuration to allow file based configuration
 */
@NonNullByDefault
public class WWNStructureConfiguration {
    public static final String STRUCTURE_ID = "structureId";
    /** Structure ID which can be retrieved with the Nest API. */
    public String structureId = "";
}
