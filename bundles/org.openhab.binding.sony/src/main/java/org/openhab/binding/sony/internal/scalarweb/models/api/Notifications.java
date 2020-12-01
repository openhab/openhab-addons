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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents all the notifications and whether they are enabled or disabled
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Notifications {
    /** The enabled notifications */
    private final @Nullable List<Notification> enabled;

    /** The disabled notifications */
    private final @Nullable List<Notification> disabled;

    /** Constructs an empty list of notifications */
    public Notifications() {
        this.enabled = new ArrayList<>();
        this.disabled = new ArrayList<>();
    }

    /**
     * Constructs the notification with a specific set enable/disabledf
     * 
     * @param enabled a non-null, possibly empty list of enabled notifications
     * @param disabled a non-null, possibly empty list of disabled notifications
     */
    public Notifications(final List<Notification> enabled, final List<Notification> disabled) {
        Objects.requireNonNull(enabled, "enabled cannot be null");
        Objects.requireNonNull(enabled, "disabled cannot be null");

        // note: if empty - set to null. Sony has a bad habit of ignoring
        // the request if one or the other array is an empty array (both can be empty however - go figure)
        this.enabled = enabled.size() == 0 && disabled.size() > 0 ? null : new ArrayList<>(enabled);
        this.disabled = enabled.size() > 0 && disabled.size() == 0 ? null : new ArrayList<>(disabled);
    }

    /**
     * This list of enabled notifications
     * 
     * @return a non-null, unmodifiable list of notifications
     */
    public List<Notification> getEnabled() {
        final List<Notification> localEnabled = enabled;
        return localEnabled == null ? Collections.emptyList() : Collections.unmodifiableList(localEnabled);
    }

    /**
     * This list of enabled notifications
     * 
     * @return a non-null, unmodifiable list of notifications
     */
    public List<Notification> getDisabled() {
        final List<Notification> localDisabled = disabled;
        return localDisabled == null ? Collections.emptyList() : Collections.unmodifiableList(localDisabled);
    }

    /**
     * Determines if the specified name is enabled (true) or not (false)
     * 
     * @param name a non-null, non-empty name
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled(final String name) {
        Validate.notEmpty(name, "name cannot be empty");

        final List<Notification> localEnabled = enabled;
        return localEnabled != null
                && localEnabled.stream().anyMatch(e -> StringUtils.equalsIgnoreCase(name, e.getName()));
    }

    @Override
    public String toString() {
        return "Notifications [enabled=" + enabled + ", disabled=" + disabled + "]";
    }
}
