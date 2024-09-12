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
package org.openhab.binding.nest.internal.sdm.config;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SDMAccountConfiguration} contains the configuration parameter values for the SDM and Pub/Sub APIs.
 *
 * @author Brian Higginbotham - Initial contribution
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMAccountConfiguration {

    public static final String PUBSUB_AUTHORIZATION_CODE = "pubsubAuthorizationCode";
    public String pubsubAuthorizationCode = "";

    public static final String PUBSUB_CLIENT_ID = "pubsubClientId";
    public String pubsubClientId = "";

    public static final String PUBSUB_CLIENT_SECRET = "pubsubClientSecret";
    public String pubsubClientSecret = "";

    public static final String PUBSUB_PROJECT_ID = "pubsubProjectId";
    public String pubsubProjectId = "";

    public static final String PUBSUB_SUBSCRIPTION_ID = "pubsubSubscriptionId";
    public String pubsubSubscriptionId = "";

    public static final String SDM_AUTHORIZATION_CODE = "sdmAuthorizationCode";
    public String sdmAuthorizationCode = "";

    public static final String SDM_CLIENT_ID = "sdmClientId";
    public String sdmClientId = "";

    public static final String SDM_CLIENT_SECRET = "sdmClientSecret";
    public String sdmClientSecret = "";

    public static final String SDM_PRODUCT_ID = "sdmProductId";
    public String sdmProjectId = "";

    public boolean usePubSub() {
        return Stream.of(pubsubProjectId, pubsubSubscriptionId, pubsubClientId, pubsubClientSecret)
                .noneMatch(String::isBlank);
    }
}
