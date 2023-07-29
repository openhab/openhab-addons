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
package org.openhab.binding.lametrictime.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lametrictime.internal.api.dto.CoreApps;
import org.openhab.binding.lametrictime.internal.api.local.dto.Application;
import org.openhab.binding.lametrictime.internal.api.local.dto.Widget;

import com.google.gson.JsonPrimitive;

/**
 * Utility class providing miscellaneous functionality based on the LaMetric Time API to support the binding.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class LaMetricTimeUtil {
    private static final Map<String, String> CORE_APP_LABELS = new HashMap<>();
    static {
        CORE_APP_LABELS.put(CoreApps.clock().getPackageName(), "Clock");
        CORE_APP_LABELS.put(CoreApps.countdown().getPackageName(), "Timer");
        CORE_APP_LABELS.put(CoreApps.radio().getPackageName(), "Radio");
        CORE_APP_LABELS.put(CoreApps.stopwatch().getPackageName(), "Stopwatch");
        CORE_APP_LABELS.put(CoreApps.weather().getPackageName(), "Weather");
    }

    public static String getAppLabel(Application app, Widget widget) {
        Map<String, JsonPrimitive> settings = widget.getSettings();
        if (settings != null && settings.containsKey("_title")) {
            String title = settings.get("_title").getAsString();
            if (title != null && !title.isEmpty()) {
                return title;
            }
        }

        String coreAppLabel = CORE_APP_LABELS.get(app.getPackageName());
        if (coreAppLabel != null) {
            return coreAppLabel;
        }

        return app.getVendor() + "'s App";
    }
}
