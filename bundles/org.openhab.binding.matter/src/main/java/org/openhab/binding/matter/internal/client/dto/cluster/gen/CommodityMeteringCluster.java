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
// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * CommodityMetering
 *
 * @author Dan Cunningham - Initial contribution
 */
public class CommodityMeteringCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0B07;
    public static final String CLUSTER_NAME = "CommodityMetering";
    public static final String CLUSTER_PREFIX = "commodityMetering";
    public static final String ATTRIBUTE_METERED_QUANTITY = "meteredQuantity";
    public static final String ATTRIBUTE_METERED_QUANTITY_TIMESTAMP = "meteredQuantityTimestamp";
    public static final String ATTRIBUTE_TARIFF_UNIT = "tariffUnit";
    public static final String ATTRIBUTE_MAXIMUM_METERED_QUANTITIES = "maximumMeteredQuantities";

    /**
     * The most recent summed value of a commodity delivered to and consumed in the premises. A null value indicates
     * that metering data is currently unavailable.
     */
    public List<MeteredQuantityStruct> meteredQuantity; // 0 list R V
    /**
     * The timestamp in UTC for when the value of the MeteredQuantity attribute was last updated. A null value indicates
     * that metering data is currently unavailable.
     */
    public Integer meteredQuantityTimestamp; // 1 epoch-s R V
    /**
     * Indicates the unit for the Quantity field on all MeteredQuantityStructs in the MeteredQuantity attribute. A null
     * value indicates that metering data is currently unavailable.
     */
    public TariffUnitEnum tariffUnit; // 2 TariffUnitEnum R V
    /**
     * Indicates the maximum number of MeteredQuantityStructs in the MeteredQuantity attribute. A null value indicates
     * that metering data is currently unavailable.
     */
    public Integer maximumMeteredQuantities; // 3 uint16 R V

    // Structs
    /**
     * Provides access to the Electric Metering device's readings.
     */
    public static class MeteredQuantityStruct {
        /**
         * Indicates the specific TariffComponentStructs associated with the metered commodity.
         */
        public List<Integer> tariffComponentIDs; // list
        /**
         * This field indicates the amount of a commodity metered during the associated TariffComponentStructs.
         */
        public BigInteger quantity; // int64

        public MeteredQuantityStruct(List<Integer> tariffComponentIDs, BigInteger quantity) {
            this.tariffComponentIDs = tariffComponentIDs;
            this.quantity = quantity;
        }
    }

    public CommodityMeteringCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 2823, "CommodityMetering");
    }

    protected CommodityMeteringCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "meteredQuantity : " + meteredQuantity + "\n";
        str += "meteredQuantityTimestamp : " + meteredQuantityTimestamp + "\n";
        str += "tariffUnit : " + tariffUnit + "\n";
        str += "maximumMeteredQuantities : " + maximumMeteredQuantities + "\n";
        return str;
    }
}
