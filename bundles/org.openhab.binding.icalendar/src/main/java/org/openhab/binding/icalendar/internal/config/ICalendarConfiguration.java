/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * The {@link ICalendarConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - Support for authorizationCode
 */
public class ICalendarConfiguration {
    public String authorizationCode;
    public Integer maxSize;
    public String password;
    public BigDecimal refreshTime;
    public String url;
    public String username;
}
