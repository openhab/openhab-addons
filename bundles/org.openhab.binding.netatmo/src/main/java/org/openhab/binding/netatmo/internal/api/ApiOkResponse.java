/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api;

/**
 * The {@link ApiResponse} models a response that only holds a status
 * toward the request sent to the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class ApiOkResponse extends ApiResponse<String> {
    public boolean isSuccess() {
        return "ok".equals(getStatus());
    }
}
