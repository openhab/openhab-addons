/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.cloud;

import javax.ws.rs.client.ClientBuilder;

import org.openhab.binding.lametrictime.api.cloud.impl.LaMetricTimeCloudImpl;
import org.openhab.binding.lametrictime.api.cloud.model.IconFilter;
import org.openhab.binding.lametrictime.api.cloud.model.Icons;

public interface LaMetricTimeCloud
{
    public Icons getIcons();

    public Icons getIcons(IconFilter filter);

    public static LaMetricTimeCloud create(CloudConfiguration config)
    {
        return new LaMetricTimeCloudImpl(config);
    }

    public static LaMetricTimeCloud create(CloudConfiguration config, ClientBuilder clientBuilder)
    {
        return new LaMetricTimeCloudImpl(config, clientBuilder);
    }
}
