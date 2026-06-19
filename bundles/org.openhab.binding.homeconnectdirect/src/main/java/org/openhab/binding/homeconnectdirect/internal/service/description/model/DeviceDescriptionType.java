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
package org.openhab.binding.homeconnectdirect.internal.service.description.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Device description type model.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public enum DeviceDescriptionType {
    STATUS_LIST,
    STATUS,
    SETTING_LIST,
    SETTING,
    EVENT_LIST,
    EVENT,
    COMMAND_LIST,
    COMMAND,
    OPTION_LIST,
    OPTION,
    PROGRAM_GROUP,
    PROGRAM,
    PROGRAM_OPTION,
    SELECTED_PROGRAM,
    PROTECTION_PORT,
    ACTIVE_PROGRAM,
    ENUMERATION_TYPE,
    UNKNOWN
}
