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
package org.openhab.binding.vesync.internal.dto.responses;

/**
 * The {@link VeSyncLoginResponse} is a Java class used as a DTO to hold the Vesync's API's login response.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncLoginResponse extends VeSyncResponse {

    public VeSyncUserSession result;

    public VeSyncUserSession getUserSession() {
        return result;
    }

    public String getToken() {
        return (result == null) ? null : result.token;
    }

    public String getAccountId() {
        return (result == null) ? null : result.accountId;
    }

    @Override
    public String toString() {
        return "VesyncLoginResponse [msg=" + getMsg() + ", result=" + result + "]";
    }
}
