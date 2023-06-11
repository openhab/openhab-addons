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
package org.openhab.binding.homewizard.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HomeWizardConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public class HomeWizardConfiguration {

    /**
     * IP Address or host for the P1 Meter
     */
    public String ipAddress = "";

    /**
     * Refresh delay in seconds
     */
    public Integer refreshDelay = 5;
}
