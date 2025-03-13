/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DanfossAirUnitConfiguration} class contains configuration parameters for the air unit.
 *
 * @author Ralf Duckstein - Initial contribution
 * @author Robert Bach - heavy refactorings
 */
@NonNullByDefault
public class DanfossAirUnitConfiguration {

    public @Nullable String host;

    public int refreshInterval = 10;

    public long updateUnchangedValuesEveryMillis = 60000;

    public String timeZone = "";
}
