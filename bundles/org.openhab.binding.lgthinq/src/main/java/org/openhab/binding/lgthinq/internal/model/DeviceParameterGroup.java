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
package org.openhab.binding.lgthinq.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceParameterGroup} class.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class DeviceParameterGroup {

    private final String groupName;
    private final String groupLabel;

    public DeviceParameterGroup(String groupName, String groupLabel) {
        this.groupName = groupName;
        this.groupLabel = groupLabel;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupLabel() {
        return groupLabel;
    }
}
