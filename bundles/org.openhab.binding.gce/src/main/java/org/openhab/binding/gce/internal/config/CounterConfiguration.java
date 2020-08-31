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
package org.openhab.binding.gce.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * The {@link CounterConfiguration} class holds configuration informations of
 * an ipx800 counter.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CounterConfiguration extends Configuration {
    public int pullInterval = 5000;
}
