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
package org.openhab.binding.mspa.internal.config;

import static org.openhab.binding.mspa.internal.MSpaConstants.EMPTY;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MSpaOwnerAccountConfiguration} class contains fields mapping for owner-account configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaOwnerAccountConfiguration {

    public String email = EMPTY;
    public String password = EMPTY;
    public String region = EMPTY;
}
