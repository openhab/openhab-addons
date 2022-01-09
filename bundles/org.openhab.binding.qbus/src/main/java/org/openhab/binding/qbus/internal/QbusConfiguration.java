/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

package org.openhab.binding.qbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class {@link QbusConfiguration} Configuration Class
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusConfiguration {
    public @Nullable String addr;
    public @Nullable Integer port;
    public @Nullable String sn;
    public @Nullable Integer serverCheck;
}
