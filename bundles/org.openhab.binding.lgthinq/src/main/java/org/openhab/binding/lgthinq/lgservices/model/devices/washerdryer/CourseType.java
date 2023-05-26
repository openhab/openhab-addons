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
package org.openhab.binding.lgthinq.lgservices.model.devices.washerdryer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CourseType}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public enum CourseType {
    // TODO - review DownloadCourse value, in remote start debugging
    COURSE("Course"),
    SMART_COURSE("SmartCourse"),
    DOWNLOADED_COURSE("DownloadedCourse"),
    UNDEF("Undefined");

    private final String value;

    CourseType(String s) {
        value = s;
    }

    public String getValue() {
        return value;
    }
}
