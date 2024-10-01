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
package org.openhab.binding.tapocontrol.internal.devices.bridge.dto;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * TapoCloudLoginData Record for sending request
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public record TapoCloudLoginData(@Expose String appType, @Expose String cloudUserName, @Expose String cloudPassword,
        @Expose String terminalUUID) {

    public TapoCloudLoginData(String cloudUserName, String cloudPassword) {
        this(TAPO_APP_TYPE, cloudUserName, cloudPassword, UUID.randomUUID().toString());
    }
}
