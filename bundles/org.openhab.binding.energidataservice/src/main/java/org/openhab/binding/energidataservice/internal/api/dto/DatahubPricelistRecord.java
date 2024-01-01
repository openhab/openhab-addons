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
package org.openhab.binding.energidataservice.internal.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Record as part of {@link DatahubPricelistRecords} from Energi Data Service.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public record DatahubPricelistRecord(@SerializedName("ValidFrom") LocalDateTime validFrom,
        @SerializedName("ValidTo") LocalDateTime validTo, @SerializedName("ChargeTypeCode") String chargeTypeCode,
        @SerializedName("Price1") BigDecimal price1, @SerializedName("Price2") BigDecimal price2,
        @SerializedName("Price3") BigDecimal price3, @SerializedName("Price4") BigDecimal price4,
        @SerializedName("Price5") BigDecimal price5, @SerializedName("Price6") BigDecimal price6,
        @SerializedName("Price7") BigDecimal price7, @SerializedName("Price8") BigDecimal price8,
        @SerializedName("Price9") BigDecimal price9, @SerializedName("Price10") BigDecimal price10,
        @SerializedName("Price11") BigDecimal price11, @SerializedName("Price12") BigDecimal price12,
        @SerializedName("Price13") BigDecimal price13, @SerializedName("Price14") BigDecimal price14,
        @SerializedName("Price15") BigDecimal price15, @SerializedName("Price16") BigDecimal price16,
        @SerializedName("Price17") BigDecimal price17, @SerializedName("Price18") BigDecimal price18,
        @SerializedName("Price19") BigDecimal price19, @SerializedName("Price20") BigDecimal price20,
        @SerializedName("Price21") BigDecimal price21, @SerializedName("Price22") BigDecimal price22,
        @SerializedName("Price23") BigDecimal price23, @SerializedName("Price24") BigDecimal price24) {

    @Override
    public LocalDateTime validTo() {
        return Objects.isNull(validTo) ? LocalDateTime.MAX : validTo;
    }

    @Override
    public BigDecimal price2() {
        return Objects.requireNonNullElse(price2, price1());
    }

    @Override
    public BigDecimal price3() {
        return Objects.requireNonNullElse(price3, price1());
    }

    @Override
    public BigDecimal price4() {
        return Objects.requireNonNullElse(price4, price1());
    }

    @Override
    public BigDecimal price5() {
        return Objects.requireNonNullElse(price5, price1());
    }

    @Override
    public BigDecimal price6() {
        return Objects.requireNonNullElse(price6, price1());
    }

    @Override
    public BigDecimal price7() {
        return Objects.requireNonNullElse(price7, price1());
    }

    @Override
    public BigDecimal price8() {
        return Objects.requireNonNullElse(price8, price1());
    }

    @Override
    public BigDecimal price9() {
        return Objects.requireNonNullElse(price9, price1());
    }

    @Override
    public BigDecimal price10() {
        return Objects.requireNonNullElse(price10, price1());
    }

    @Override
    public BigDecimal price11() {
        return Objects.requireNonNullElse(price11, price1());
    }

    @Override
    public BigDecimal price12() {
        return Objects.requireNonNullElse(price12, price1());
    }

    @Override
    public BigDecimal price13() {
        return Objects.requireNonNullElse(price13, price1());
    }

    @Override
    public BigDecimal price14() {
        return Objects.requireNonNullElse(price14, price1());
    }

    @Override
    public BigDecimal price15() {
        return Objects.requireNonNullElse(price15, price1());
    }

    @Override
    public BigDecimal price16() {
        return Objects.requireNonNullElse(price16, price1());
    }

    @Override
    public BigDecimal price17() {
        return Objects.requireNonNullElse(price17, price1());
    }

    @Override
    public BigDecimal price18() {
        return Objects.requireNonNullElse(price18, price1());
    }

    @Override
    public BigDecimal price19() {
        return Objects.requireNonNullElse(price19, price1());
    }

    @Override
    public BigDecimal price20() {
        return Objects.requireNonNullElse(price20, price1());
    }

    @Override
    public BigDecimal price21() {
        return Objects.requireNonNullElse(price21, price1());
    }

    @Override
    public BigDecimal price22() {
        return Objects.requireNonNullElse(price22, price1());
    }

    @Override
    public BigDecimal price23() {
        return Objects.requireNonNullElse(price23, price1());
    }

    @Override
    public BigDecimal price24() {
        return Objects.requireNonNullElse(price24, price1());
    }

    /**
     * Get {@link Map} of tariffs with hour start as key.
     *
     * @return map with hourly tariffs
     */
    public Map<LocalTime, BigDecimal> getTariffMap() {
        Map<LocalTime, BigDecimal> tariffMap = new HashMap<>();

        tariffMap.put(LocalTime.of(0, 0), price1());
        tariffMap.put(LocalTime.of(1, 0), price2());
        tariffMap.put(LocalTime.of(2, 0), price3());
        tariffMap.put(LocalTime.of(3, 0), price4());
        tariffMap.put(LocalTime.of(4, 0), price5());
        tariffMap.put(LocalTime.of(5, 0), price6());
        tariffMap.put(LocalTime.of(6, 0), price7());
        tariffMap.put(LocalTime.of(7, 0), price8());
        tariffMap.put(LocalTime.of(8, 0), price9());
        tariffMap.put(LocalTime.of(9, 0), price10());
        tariffMap.put(LocalTime.of(10, 0), price11());
        tariffMap.put(LocalTime.of(11, 0), price12());
        tariffMap.put(LocalTime.of(12, 0), price13());
        tariffMap.put(LocalTime.of(13, 0), price14());
        tariffMap.put(LocalTime.of(14, 0), price15());
        tariffMap.put(LocalTime.of(15, 0), price16());
        tariffMap.put(LocalTime.of(16, 0), price17());
        tariffMap.put(LocalTime.of(17, 0), price18());
        tariffMap.put(LocalTime.of(18, 0), price19());
        tariffMap.put(LocalTime.of(19, 0), price20());
        tariffMap.put(LocalTime.of(20, 0), price21());
        tariffMap.put(LocalTime.of(21, 0), price22());
        tariffMap.put(LocalTime.of(22, 0), price23());
        tariffMap.put(LocalTime.of(23, 0), price24());

        return tariffMap;
    }
}
