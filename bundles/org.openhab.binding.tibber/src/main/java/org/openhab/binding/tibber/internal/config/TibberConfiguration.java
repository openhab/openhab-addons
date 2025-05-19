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

import static org.openhab.binding.tibber.internal.TibberBindingConstants.EMPTY;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TibberConfiguration} class contains fields mapping configuration parameters.
 *
 * @author Stian Kjoglum - Initial contribution
 * @author Bernd Weymann - make config variables public
 */
@NonNullByDefault
public class TibberConfiguration {
    public String token = EMPTY;
    public String homeid = EMPTY;
}
