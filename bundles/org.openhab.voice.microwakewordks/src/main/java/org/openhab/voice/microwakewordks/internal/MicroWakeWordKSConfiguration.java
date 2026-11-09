/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.voice.microwakewordks.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MicroWakeWordKSConfiguration} class contains the service configuration.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MicroWakeWordKSConfiguration {
    /**
     * Average probability above which the wake word counts as detected, {@code null} to use the cutoff of the model.
     */
    public @Nullable Double probabilityCutoff;
}
