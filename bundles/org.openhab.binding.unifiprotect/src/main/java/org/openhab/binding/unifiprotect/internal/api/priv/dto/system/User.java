/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.system;

import java.time.Instant;
import java.util.List;

import org.openhab.binding.unifiprotect.internal.api.priv.dto.base.UniFiProtectModel;

/**
 * User model for UniFi Protect
 *
 * @author Dan Cunningham - Initial contribution
 */
public class User extends UniFiProtectModel {

    public List<String> permissions;
    public String lastLoginIp;
    public Instant lastLoginTime;
    public Boolean isOwner;
    public Boolean enableNotifications;
    public Boolean hasAcceptedInvite;
    public List<String> allPermissions;
    public List<String> scopes;
    public UserLocation location;
    public String name;
    public String firstName;
    public String lastName;
    public String email;
    public String localUsername;
    public List<String> groupIds;
    public CloudAccount cloudAccount;
    public UserFeatureFlags featureFlags;

    /**
     * User location for presence detection
     */
    public static class UserLocation {
        public Boolean isAway;
        public Double latitude;
        public Double longitude;
    }

    /**
     * Cloud account information
     */
    public static class CloudAccount {
        public String cloudProviderId;
        public String cloudProviderName;
        public String cloudProviderUserId;
        public String email;
        public String firstName;
        public String lastName;
        public String name;
    }

    /**
     * User feature flags
     */
    public static class UserFeatureFlags {
        public Boolean notificationsV2;
    }
}
