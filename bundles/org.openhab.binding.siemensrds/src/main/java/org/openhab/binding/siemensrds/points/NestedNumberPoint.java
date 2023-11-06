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
package org.openhab.binding.siemensrds.points;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * private class a data point where "value" is a nested JSON numeric element
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NestedNumberPoint extends BasePoint {

    @SerializedName("value")
    protected @Nullable NestedNumberValue inner;

    @Override
    public int asInt() {
        NestedNumberValue inner = this.inner;
        if (inner != null) {
            Number innerValue = inner.value;
            if (innerValue != null) {
                return innerValue.intValue();
            }
        }
        return UNDEFINED_VALUE;
    }

    @Override
    public State getState() {
        NestedNumberValue inner = this.inner;
        if (inner != null) {
            Number innerValue = inner.value;
            if (innerValue != null) {
                return new QuantityType<>(innerValue.doubleValue(), getUnit());
            }
        }
        return UnDefType.NULL;
    }

    @Override
    public int getPresentPriority() {
        NestedNumberValue inner = this.inner;
        return inner != null ? inner.presentPriority : UNDEFINED_VALUE;
    }

    public void setPresentPriority(int value) {
        NestedNumberValue inner = this.inner;
        if (inner != null) {
            inner.presentPriority = value;
        }
    }

    @Override
    public void refreshValueFrom(BasePoint from) {
        super.refreshValueFrom(from);
        if (from instanceof NestedNumberPoint point) {
            NestedNumberValue fromInner = point.inner;
            NestedNumberValue thisInner = this.inner;
            if (thisInner != null && fromInner != null) {
                thisInner.value = fromInner.value;
                thisInner.presentPriority = fromInner.presentPriority;
            }
        }
    }
}
