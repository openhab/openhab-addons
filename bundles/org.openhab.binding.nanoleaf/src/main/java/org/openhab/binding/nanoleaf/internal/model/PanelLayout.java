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

        GlobalOrientation go = globalOrientation;
        GlobalOrientation otherGo = pl.getGlobalOrientation();
        boolean goEquals = false;
        if (go == null || otherGo == null) {
            if (go == null && otherGo == null) {
                goEquals = true;
            }
        } else {
            goEquals = go.equals(otherGo);
        }

        Layout l = layout;
        Layout otherL = pl.getLayout();
        boolean lEquals = false;
        if (l == null || otherL == null) {
            if (l == null && otherL == null) {
                lEquals = true;
            }
        } else {
            lEquals = l.equals(otherL);
        }

        return goEquals && lEquals;
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
