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
package org.openhab.binding.tplinksmarthome.internal.model;

import com.google.gson.annotations.Expose;

/**
 * Class to be extended by state classes that support context. i.e. has multiple children that can be controlled.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class ContextState {

    public static class Context {
        @Expose
        private String[] childIds = new String[1];

        public void setChildId(String childId) {
            this.childIds[0] = childId;
        }

        @Override
        public String toString() {
            return " child_ids:[" + childIds[0] + "]";
        }
    }

    @Expose
    private Context context;

    public void setChildId(String childId) {
        context = new Context();
        context.setChildId(childId);
    }

    @Override
    public String toString() {
        return context == null ? "" : context.toString();
    }
}
