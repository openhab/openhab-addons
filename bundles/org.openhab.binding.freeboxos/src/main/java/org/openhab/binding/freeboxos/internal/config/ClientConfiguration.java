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
package org.openhab.binding.freeboxos.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ClientConfiguration} is responsible for holding configuration informations for a controllable client of
 * the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ClientConfiguration extends HostConfiguration {
    public static final String ID = "id";

    public int id = 1;
}
