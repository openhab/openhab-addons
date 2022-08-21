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
package org.openhab.binding.arcam.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ArcamConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamConfiguration {

    public static final String HOSTNAME = "hostname";
    public static final String SERIAL = "serial";
    public static final String NAME = "name";
    public static final String MODEL = "model";

    public @Nullable String hostname;
    public @Nullable String serial;
    public @Nullable String name;
    public @Nullable String model;
}
