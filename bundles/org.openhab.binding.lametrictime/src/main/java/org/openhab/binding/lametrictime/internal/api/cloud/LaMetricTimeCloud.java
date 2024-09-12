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
package org.openhab.binding.lametrictime.internal.api.cloud;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lametrictime.internal.api.cloud.dto.IconFilter;
import org.openhab.binding.lametrictime.internal.api.cloud.dto.Icons;
import org.openhab.binding.lametrictime.internal.api.cloud.impl.LaMetricTimeCloudImpl;

/**
 * Interface for LaMetric Time cloud.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public interface LaMetricTimeCloud {
    Icons getIcons();

    Icons getIcons(IconFilter filter);

    static LaMetricTimeCloud create(CloudConfiguration config) {
        return new LaMetricTimeCloudImpl(config);
    }

    static LaMetricTimeCloud create(CloudConfiguration config, ClientBuilder clientBuilder) {
        return new LaMetricTimeCloudImpl(config, clientBuilder);
    }
}
