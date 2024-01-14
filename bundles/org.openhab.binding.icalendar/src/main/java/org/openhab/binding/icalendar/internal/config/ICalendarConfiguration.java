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
package org.openhab.binding.icalendar.internal.config;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ICalendarConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Support for authorizationCode
 * @author Michael Wodniok - Added Nullable annotations for conformity
 */
@NonNullByDefault
public class ICalendarConfiguration {
    @Nullable
    public String authorizationCode;
    @Nullable
    public BigDecimal maxSize;
    @Nullable
    public String password;
    @Nullable
    public BigDecimal refreshTime;
    @Nullable
    public String url;
    @Nullable
    public String username;
}
