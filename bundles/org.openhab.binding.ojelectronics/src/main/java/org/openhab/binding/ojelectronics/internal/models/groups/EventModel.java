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
package org.openhab.binding.ojelectronics.internal.models.groups;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Model for events
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class EventModel {

    public int scheduleType;

    public String clock = "";

    public int temperature;

    public boolean active;

    public boolean eventIsOnNextDay;
}
