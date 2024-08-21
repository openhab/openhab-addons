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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents panel layout of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class PanelLayout {

    private @Nullable Layout layout;
    private @Nullable GlobalOrientation globalOrientation;

    public PanelLayout() {
    }

    public PanelLayout(GlobalOrientation globalOrientation, Layout layout) {
        this.globalOrientation = globalOrientation;
        this.layout = layout;
    }

    public @Nullable Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    public @Nullable GlobalOrientation getGlobalOrientation() {
        return globalOrientation;
    }

    public void setGlobalOrientation(GlobalOrientation globalOrientation) {
        this.globalOrientation = globalOrientation;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PanelLayout pl = (PanelLayout) o;

        // For a panel layout to be equal to another panel layouit, all inner data structures must
        // be equal, or they must be null both in this object or the object it is compared with.

        GlobalOrientation go = globalOrientation;
        GlobalOrientation otherGo = pl.getGlobalOrientation();
        boolean goEquals = false;
        if (go == null || otherGo == null) {
            if (go == null && otherGo == null) {
                // If one of the global oriantations are null, the other must also be null
                // for them to be equal
                goEquals = true;
            }
        } else {
            goEquals = go.equals(otherGo);
        }

        if (!goEquals) {
            // No reason to compare layout if global oriantation is different
            return false;
        }

        Layout l = layout;
        Layout otherL = pl.getLayout();

        if (l == null && otherL == null) {
            return true;
        }

        if (l == null || otherL == null) {
            return false;
        }

        return l.equals(otherL);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        GlobalOrientation go = globalOrientation;
        if (go != null) {
            result = prime * result + go.hashCode();
        }

        Layout l = layout;
        if (l != null) {
            result = prime * result + l.hashCode();
        }

        return result;
    }
}
