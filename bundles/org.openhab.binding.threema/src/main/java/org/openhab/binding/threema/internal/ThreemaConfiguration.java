/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.threema.internal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ThreemaConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Kai K. - Initial contribution
 */
public class ThreemaConfiguration {

    private String gatewayId;
    private String secret;
    private @Nullable List<String> recipientIds;

    public String getGatewayId() {
        return gatewayId;
    }

    public String getSecret() {
        return secret;
    }

    public List<String> getRecipientIds() {
        return Optional.ofNullable(recipientIds).map(Collections::unmodifiableList).orElse(Collections.emptyList());
    }
}
