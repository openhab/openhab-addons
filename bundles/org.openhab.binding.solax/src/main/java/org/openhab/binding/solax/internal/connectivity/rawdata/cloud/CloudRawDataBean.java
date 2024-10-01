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
package org.openhab.binding.solax.internal.connectivity.rawdata.cloud;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import org.apache.directory.api.util.Strings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.cloud.CloudInverterData;
import org.openhab.binding.solax.internal.util.GsonSupplier;

import com.google.gson.Gson;

/**
 * The {@link CloudRawDataBean} is used as a storage for mapping the raw data collected from the cloud to this object.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class CloudRawDataBean implements CloudInverterData {

    public static final String QUERY_SUCCESS = "Query success!";
    public static final String ERROR = "error";

    private static final DateTimeFormatter CUSTOM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private boolean success;
    private @NonNullByDefault({}) String exception;
    private @NonNullByDefault({}) Result result;
    private int code;
    private @NonNullByDefault({}) String rawData;

    // For JSON serialization / deserialization purposes
    public CloudRawDataBean() {
    }

    public CloudRawDataBean(boolean isSuccess) {
        this.success = isSuccess;
        this.exception = isSuccess ? QUERY_SUCCESS : ERROR;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public @Nullable String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public @Nullable Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    // Inner Implementation / DTO of the CloudInverterData starts here

    @Override
    public String getInverterSerialNumber() {
        String inverterSN = result.getInverterSN();
        return inverterSN != null ? inverterSN : Strings.EMPTY_STRING;
    }

    @Override
    public String getWifiSerialNumber() {
        String serialNumber = result.getSn();
        return serialNumber != null ? serialNumber : Strings.EMPTY_STRING;
    }

    @Override
    public String getOverallResult() {
        // Why the ternary operator does not get taken into account by the JDT and the maven build?
        String result = exception;
        if (result == null) {
            result = "Retrieved message from the cloud API is null. Something wrong happened.";
        }
        return result;
    }

    @Override
    public double getInverterOutputPower() {
        return notNullResult(() -> result.getAcPower());
    }

    @Override
    public double getYieldToday() {
        return notNullResult(() -> result.getYieldToday());
    }

    @Override
    public double getYieldTotal() {
        return notNullResult(() -> result.getYieldTotal());
    }

    @Override
    public double getFeedInPower() {
        return notNullResult(() -> result.getFeedInPower());
    }

    @Override
    public double getFeedInEnergy() {
        return notNullResult(() -> result.getFeedInEnergy());
    }

    @Override
    public double getConsumeEnergy() {
        return notNullResult(() -> result.getConsumeEnergy());
    }

    @Override
    public double getFeedInPowerM2() {
        return notNullResult(() -> result.getFeedInPowerM2());
    }

    @Override
    public double getBatteryLevel() {
        return notNullResult(() -> result.getSoc());
    }

    @Override
    public double getEPSPowerR() {
        return notNullResult(() -> result.getPeps1());
    }

    @Override
    public double getEPSPowerS() {
        return notNullResult(() -> result.getPeps2());
    }

    @Override
    public double getEPSPowerT() {
        return notNullResult(() -> result.getPeps3());
    }

    @Override
    public InverterType getInverterType() {
        return InverterType.fromIndex(result.getInverterType());
    }

    @Override
    public ZonedDateTime getUploadTime(ZoneId zoneId) {
        String uploadTime = result.getUploadTime();
        if (uploadTime != null) {
            return ZonedDateTime.of(LocalDateTime.parse(uploadTime, CUSTOM_DATE_FORMATTER), zoneId);
        }

        return ZonedDateTime.of(LocalDateTime.MIN, zoneId);
    }

    @Override
    public double getBatteryPower() {
        return notNullResult(() -> result.getBatPower());
    }

    @Override
    public double getPowerPv1() {
        return notNullResult(() -> result.getPowerDc1());
    }

    @Override
    public double getPowerPv2() {
        return notNullResult(() -> result.getPowerDc2());
    }

    @Override
    public double getPowerPv3() {
        return notNullResult(() -> result.getPowerDc3());
    }

    @Override
    public double getPowerPv4() {
        return notNullResult(() -> result.getPowerDc4());
    }

    @Override
    public short getInverterWorkModeCode() {
        return (short) result.getInverterStatus();
    }

    @Override
    public int getBatteryStatus() {
        return result.getBatStatus();
    }

    @Override
    public double getPVTotalPower() {
        return getPowerPv1() + getPowerPv2() + getPowerPv3() + getPowerPv4();
    }

    public static CloudRawDataBean fromJson(String json) {
        if (json.isEmpty()) {
            return new CloudRawDataBean(false);
        }

        Gson gson = GsonSupplier.getInstance();
        CloudRawDataBean deserializedObject = gson.fromJson(json, CloudRawDataBean.class);
        if (deserializedObject == null) {
            return new CloudRawDataBean(false);
        }
        deserializedObject.setRawData(json);
        return deserializedObject;
    }

    public void setRawData(String json) {
        this.rawData = json;
    }

    @Override
    public @Nullable String getRawData() {
        return rawData;
    }

    public boolean isError() {
        String exception = getException();
        return ERROR.equals(exception);
    }

    private double notNullResult(Supplier<@Nullable Double> supplier) {
        Double returnValue = supplier.get();
        return returnValue != null ? returnValue : Integer.MIN_VALUE;
    }

    public boolean isValid() {
        return getResult() != null && getRawData() != null;
    }

    @Override
    public String toString() {
        return "CloudRawDataBean [success=" + success + ", exception=" + exception + ", result=" + result + ", code="
                + code + ", rawData=" + rawData + "]";
    }
}
