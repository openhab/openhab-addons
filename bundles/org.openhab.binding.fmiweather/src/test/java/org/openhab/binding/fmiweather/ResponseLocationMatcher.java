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
package org.openhab.binding.fmiweather;

import java.math.BigDecimal;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.openhab.binding.fmiweather.internal.client.Location;

/**
 * Hamcrest matcher for Location objects
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ResponseLocationMatcher extends TypeSafeMatcher<Location> {

    public final String name;
    public final String id;
    @Nullable
    public final BigDecimal latitude;
    @Nullable
    public final BigDecimal longitude;

    public ResponseLocationMatcher(Location template) {
        this(template.name, template.id, template.latitude, template.longitude);
    }

    public ResponseLocationMatcher(String name, String id, @Nullable String latitude, @Nullable String longitude) {
        this(name, id, latitude == null ? null : new BigDecimal(latitude),
                longitude == null ? null : new BigDecimal(longitude));
    }

    public ResponseLocationMatcher(String name, String id, @Nullable BigDecimal latitude,
            @Nullable BigDecimal longitude) {
        super();
        this.name = name;
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void describeTo(@Nullable Description description) {
        if (description == null) {
            return;
        }
        description.appendText("Location(name=\"").appendText(name).appendText("\", id=\"").appendText(id)
                .appendText("\", latitude=").appendText(Objects.toString(latitude)).appendText(", longitude=")
                .appendText(Objects.toString(longitude)).appendText(")");
    }

    @Override
    protected boolean matchesSafely(Location loc) {
        return Objects.deepEquals(name, loc.name) && Objects.deepEquals(id, loc.id)
                && Objects.deepEquals(latitude, loc.latitude) && Objects.deepEquals(longitude, loc.longitude);
    }
}
