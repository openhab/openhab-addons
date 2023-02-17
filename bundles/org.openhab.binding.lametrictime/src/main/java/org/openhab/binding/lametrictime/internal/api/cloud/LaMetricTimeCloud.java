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
package org.openhab.binding.lametrictime.internal.api.cloud;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.lametrictime.internal.api.cloud.impl.LaMetricTimeCloudImpl;
import org.openhab.binding.lametrictime.internal.api.cloud.model.IconFilter;
import org.openhab.binding.lametrictime.internal.api.cloud.model.Icons;

/**
 * Interface for LaMetric Time cloud.
 *
 * @author Gregory Moyer - Initial contribution
 */
public interface LaMetricTimeCloud {
    public Icons getIcons();

    public Icons getIcons(IconFilter filter);

    public static LaMetricTimeCloud create(CloudConfiguration config) {
        return new LaMetricTimeCloudImpl(config);
    }

    public static LaMetricTimeCloud create(CloudConfiguration config, ClientBuilder clientBuilder) {
        return new LaMetricTimeCloudImpl(config, clientBuilder);
    }
}
