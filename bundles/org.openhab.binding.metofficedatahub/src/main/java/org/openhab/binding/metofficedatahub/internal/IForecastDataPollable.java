/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.metofficedatahub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This defines the behaviours for forecast polling.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public interface IForecastDataPollable {

    /**
     * When called this provides the implementation to do a poll for hourly data, and process it.
     */
    void pollHourlyForecast();

    /**
     * When called this provides the implementation to do a poll for daily data, and process it.
     */
    void pollDailyForecast();
}
