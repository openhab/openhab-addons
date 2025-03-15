/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ResponseContract} holds informations about the contract
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class ResponseContract {
    public Customer customer;

    public class Customer {
        @SerializedName("customer_id")
        public String customerId;

        @SerializedName("usage_points")
        public UsagePoints[] usagePoint;
    }

    public class UsagePoints {
        @SerializedName("usage_point")
        public UsagePoint usagePoint;
        public Contract contracts;
    }
}
