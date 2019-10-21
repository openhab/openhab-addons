/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.dto;

/**
 *
 * @author Chris Foot - Initial contribution
 */

public class HiveLoginSession {

    public String id;
    public String username;
    public String userId;
    public String extCustomerLevel;
    public String latestSupportedApiVersion;
    public String sessionId;
}
