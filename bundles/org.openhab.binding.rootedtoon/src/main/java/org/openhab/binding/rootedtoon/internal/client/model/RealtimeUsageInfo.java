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

package org.openhab.binding.rootedtoon.internal.client.model;

/**
 *
 * @author daanmeijer - Initial Contribution
 *
 */
public class RealtimeUsageInfo {
    public RatedValue gas;

    public RatedValue elec;

    public RatedValue elec_solar;

    public RatedValue elec_delivered_nt;

    public RatedValue elec_received_nt;

    public RatedValue elec_delivered_lt;

    public RatedValue elec_received_lt;

    public RatedValue heat;
}