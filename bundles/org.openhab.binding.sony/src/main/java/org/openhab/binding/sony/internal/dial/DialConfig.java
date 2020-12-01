/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.dial;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.AbstractConfig;

/**
 * Configuration class for the {@link DialHandler}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class DialConfig extends AbstractConfig {
    /** The access code */
    private @Nullable String accessCode;

    /**
     * Gets the access code
     *
     * @return the access code
     */
    public @Nullable String getAccessCode() {
        return accessCode;
    }

    /**
     * Sets the access code.
     *
     * @param accessCode the new access code
     */
    public void setAccessCode(final String accessCode) {
        this.accessCode = accessCode;
    }

    @Override
    public Map<String, Object> asProperties() {
        final Map<String, Object> props = super.asProperties();
        conditionallyAddProperty(props, "accessCode", accessCode);
        return props;
    }
}
