/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lametrictime.internal;

import java.util.Map;

import org.syphr.lametrictime.api.local.model.Application;
import org.syphr.lametrictime.api.local.model.Widget;
import org.syphr.lametrictime.api.model.CoreApps;

import com.google.common.collect.Maps;
import com.google.gson.JsonPrimitive;

/**
 * Utility class providing miscellaneous functionality based on the LaMetric Time API to support the binding.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class LaMetricTimeUtil {
    private static final Map<String, String> CORE_APP_LABELS = Maps.newHashMap();
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
