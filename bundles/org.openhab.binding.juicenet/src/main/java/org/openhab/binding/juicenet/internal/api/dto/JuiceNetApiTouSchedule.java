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
package org.openhab.binding.juicenet.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link JuiceNetApiTouSchedule } implements DTO for TOU schedule
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class JuiceNetApiTouSchedule {
    public String type = "";
    public JuiceNetApiTouDay weekday = new JuiceNetApiTouDay();
    public JuiceNetApiTouDay weenend = new JuiceNetApiTouDay();
}
