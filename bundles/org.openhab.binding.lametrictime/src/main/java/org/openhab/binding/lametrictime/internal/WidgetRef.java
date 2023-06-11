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
package org.openhab.binding.lametrictime.internal;

/**
 * This bean represents the information necessary to uniquely reference a widget on the LaMetric Time platform.
 *
 * @author Gregor Moyer - Initial contribution
 */
public class WidgetRef {
    private final String packageName;
    private final String widgetId;

    /**
     * Build a {@link WidgetRef} from the given String.
     *
     * @param widgetRef formatted as <code>package name:widget ID</code>
     * @return the widget reference
     */
    public static WidgetRef fromString(String widgetRef) {
        String[] tokens = widgetRef.split(":");
        if (tokens.length != 2) {
            throw new IllegalArgumentException(
                    "Provided string '" + widgetRef + "' is not in the format '<package name>:<widget ID>'");
        }

        return new WidgetRef(tokens[0], tokens[1]);
    }

    public WidgetRef(String packageName, String widgetId) {
        this.packageName = packageName;
        this.widgetId = widgetId;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getWidgetId() {
        return widgetId;
    }

    @Override
    public String toString() {
        return packageName + ":" + widgetId;
    }
}
