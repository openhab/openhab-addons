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
package org.openhab.binding.tacmi.internal.schema;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TACmiSchemaConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Niessner - Initial contribution
 */
@NonNullByDefault
public class TACmiSchemaConfiguration {

    /**
     * host address of the C.M.I.
     */
    public String host = "";

    /**
     * Username
     */
    public String username = "";

    /**
     * Password
     */
    public String password = "";

    /**
     * ID of API schema page
     */
    public int schemaId;

    /**
     * API page poll intervall
     */
    public int pollInterval;
}
