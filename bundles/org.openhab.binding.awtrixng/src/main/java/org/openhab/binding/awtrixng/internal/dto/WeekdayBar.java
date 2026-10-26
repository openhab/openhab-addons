/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.awtrixng.internal.dto;

import java.util.List;

/**
 * The weekday bar of the clock or date app.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class WeekdayBar {
    public Boolean show;
    public Boolean startOnMonday;
    public List<String> weekendDays;
    public String activeColor;
    public String inactiveColor;
    public String weekendActiveColor;
    public String weekendInactiveColor;
}
