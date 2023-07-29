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
package org.openhab.transform.rollershutterposition.internal.config;

import static org.openhab.transform.rollershutterposition.internal.RollerShutterPositionConstants.DEFAULT_PRECISION;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RollerShutterPositionConfig} class contains the parameters for RollerShutterPosition
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class RollerShutterPositionConfig {
    public float uptime; // uptime in seconds (set by param)
    public float downtime; // downtime in seconds (set by param)
    public int precision = DEFAULT_PRECISION; // minimum movement
}
