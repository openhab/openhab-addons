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
package org.openhab.binding.tibber.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TibberConfiguration} class contains fields mapping configuration parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 * @author Bernd Weymann - make configuration variables public and introduce updateHour
 */
@NonNullByDefault
public class TibberConfiguration {
    public String token = "";
    public String homeid = "";
    public int updateHour = 13;
}
