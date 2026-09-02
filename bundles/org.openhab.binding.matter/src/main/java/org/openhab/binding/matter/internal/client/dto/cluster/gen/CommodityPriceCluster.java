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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * CommodityPrice
 *
 * @author Dan Cunningham - Initial contribution
 */
public class CommodityPriceCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0095;
    public static final String CLUSTER_NAME = "CommodityPrice";
    public static final String CLUSTER_PREFIX = "commodityPrice";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_TARIFF_UNIT = "tariffUnit";
    public static final String ATTRIBUTE_CURRENCY = "currency";
    public static final String ATTRIBUTE_CURRENT_PRICE = "currentPrice";
    public static final String ATTRIBUTE_PRICE_FORECAST = "priceForecast";

    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * Indicates the unit of measure for all pricing reported by this cluster.
     */
    public TariffUnitEnum tariffUnit; // 0 TariffUnitEnum R V
    /**
     * Indicates the currency for all pricing reported by this cluster. If the current currency is unknown, or cannot be
     * determined, the value shall be null.
     */
    public Currency currency; // 1 currency R V
    /**
     * Indicates the current price. If the current price is unknown, or cannot be determined, the value shall be null.
     * The Description and Components fields shall be omitted in this attribute's value.
     */
    public CommodityPriceStruct currentPrice; // 2 CommodityPriceStruct R V
    /**
     * Indicates the forecast of upcoming price changes. If the forecast is unable to be determined, this list shall be
     * empty.
     * The list entries shall be in time order:
     * - All entries except the last one shall have a non-null PeriodEnd.
     * - For all entries except the first one, PeriodStart shall be greater than the previous entry's PeriodEnd.
     * The Description and Components fields shall be omitted from CommodityPriceStructs in this attribute's value.
     * If the PeriodEnd field is null on the value of the CurrentPrice attribute, then this list shall be empty.
     */
    public List<CommodityPriceStruct> priceForecast; // 3 list R V

    // Structs
    /**
     * This event shall be generated when the value of the CurrentPrice attribute changes.
     */
    public static class PriceChange {
        /**
         * This field shall be the new value of the CurrentPrice attribute.
         */
        public CommodityPriceStruct currentPrice; // CommodityPriceStruct

        public PriceChange(CommodityPriceStruct currentPrice) {
            this.currentPrice = currentPrice;
        }
    }

    /**
     * This represents a component of a given price; it is only used in the Components field.
     */
    public static class CommodityPriceComponentStruct {
        /**
         * This field shall indicate the component price of the commodity per TariffUnit, with the currency indicated by
         * the currency of the Price field of the parent CommodityPriceStruct.
         */
        public BigInteger price; // money
        /**
         * This field shall indicate the source of the price component.
         */
        public TariffPriceTypeEnum source; // TariffPriceTypeEnum
        /**
         * This field shall indicate a description of the pricing plan yielding the value of the Price field. For
         * example, this field may contain the name of the current block of the selected billing plan, or the name of
         * the time of usage tier.
         */
        public String description; // string
        /**
         * This field shall indicate the ID of the associated TariffComponent for this price component. If there is no
         * associated TariffComponent, this field shall be omitted.
         */
        public Integer tariffComponentId; // uint32

        public CommodityPriceComponentStruct(BigInteger price, TariffPriceTypeEnum source, String description,
                Integer tariffComponentId) {
            this.price = price;
            this.source = source;
            this.description = description;
            this.tariffComponentId = tariffComponentId;
        }
    }

    /**
     * This represents a price over a given period.
     */
    public static class CommodityPriceStruct {
        /**
         * This field shall indicate the beginning timestamp in UTC of the period covered by the price indicated in the
         * Price field, or the price level indicated in the Price Level field, or both.
         */
        public Integer periodStart; // epoch-s
        /**
         * This field shall indicate the ending timestamp in UTC of the period covered by the price indicated in the
         * Price field, or the price level indicated in the Price Level field, or both.
         * If this field is null, then the period has no definite end.
         */
        public Integer periodEnd; // epoch-s
        /**
         * This field shall indicate the price of the commodity per TariffUnit.
         */
        public BigInteger price; // money
        /**
         * This field shall indicate the tariff price level.
         */
        public Integer priceLevel; // int16
        /**
         * This field shall indicate a description of the pricing plan yielding the value of the Price field, or the
         * Price Level field. For example, this field may contain the name of the selected billing plan.
         */
        public String description; // string
        /**
         * This field shall indicate a list of the components that comprise the value in the Price field. For example,
         * if a pricing plan has a base price and a surcharge for a given time of day, it may have two entries in the
         * Components field.
         * If this field is not empty, the Price fields in the list shall sum to the value in the Price field.
         */
        public List<CommodityPriceComponentStruct> components; // list

        public CommodityPriceStruct(Integer periodStart, Integer periodEnd, BigInteger price, Integer priceLevel,
                String description, List<CommodityPriceComponentStruct> components) {
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.price = price;
            this.priceLevel = priceLevel;
            this.description = description;
            this.components = components;
        }
    }

    // Bitmaps
    public static class CommodityPriceDetailBitmap {
        public boolean description;
        public boolean components;

        public CommodityPriceDetailBitmap(boolean description, boolean components) {
            this.description = description;
            this.components = components;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * Forecasts upcoming pricing
         */
        public boolean forecasting;

        public FeatureMap(boolean forecasting) {
            this.forecasting = forecasting;
        }
    }

    public CommodityPriceCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 149, "CommodityPrice");
    }

    protected CommodityPriceCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * Upon receipt, this shall generate a GetDetailedPrice Response command.
     */
    public static ClusterCommand getDetailedPriceRequest(CommodityPriceDetailBitmap details) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (details != null) {
            map.put("details", details);
        }
        return new ClusterCommand("getDetailedPriceRequest", map);
    }

    /**
     * Upon receipt, this shall generate a GetDetailedForecast Response command.
     */
    public static ClusterCommand getDetailedForecastRequest(CommodityPriceDetailBitmap details) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (details != null) {
            map.put("details", details);
        }
        return new ClusterCommand("getDetailedForecastRequest", map);
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "featureMap : " + featureMap + "\n";
        str += "tariffUnit : " + tariffUnit + "\n";
        str += "currency : " + currency + "\n";
        str += "currentPrice : " + currentPrice + "\n";
        str += "priceForecast : " + priceForecast + "\n";
        return str;
    }
}
