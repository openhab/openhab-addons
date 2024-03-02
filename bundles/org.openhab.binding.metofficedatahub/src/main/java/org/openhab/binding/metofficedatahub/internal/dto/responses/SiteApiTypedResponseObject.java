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
package org.openhab.binding.metofficedatahub.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SiteApiTypedResponseObject} is a Java class
 * used as a base DTO where the response definitions can be
 * identified by the type code embedded within the object.
 *
 * @author David Goodyear - Initial contribution
 */
public abstract class SiteApiTypedResponseObject {
    @SerializedName("type")
    private String type;

    public String getType() {
        return type;
    }

    public abstract String getExpectedType();
}
