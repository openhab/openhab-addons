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
package org.openhab.binding.smartthings.internal.dto;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Mapping object for data returned from smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsDeviceData {
    @Nullable
    public String capability;
    @Nullable
    public String attribute;
    @Nullable
    public String name;
    @Nullable
    public String id;

    // This is a hack that seems to make the null checking work
    public String getNonNullId() {
        if (id == null) {
            return "";
        } else {
            /* Note for code reviewer about the following retun statement:
                Code analysis says There is no need for a @NonNull annotation because it is set as default. Only @Nullable should be used
                But without it I get: Null type mismatch (type annotations): required '@NonNull String' but this expression has type '@Nullable String'
                Better suggestions?
            */
            return (@NonNull String) id; 
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("capability :").append(capability);
        sb.append(", attribute :").append(attribute);
        sb.append(", name: ").append(name);
        sb.append(", id: ").append(id);
        return sb.toString();
    }
}
