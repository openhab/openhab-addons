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
 * The EventFilterConfiguration holds configuration for the Event Filter Item Type.
 *
 * @author Michael Wodniok - Initial contribution
 */
public class EventFilterConfiguration {
    public BigDecimal maxEvents;
    public String datetimeUnit;
    public BigDecimal datetimeStart;
    public BigDecimal datetimeEnd;
    public Boolean datetimeRound;
    public String textEventField;
    public String textEventValue;
    public String textValueType;
}
