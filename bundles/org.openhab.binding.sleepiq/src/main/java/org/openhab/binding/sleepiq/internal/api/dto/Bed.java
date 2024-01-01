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
package org.openhab.binding.sleepiq.internal.api.dto;

import java.time.ZonedDateTime;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Bed} holds the bed response from the sleepiq API.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Bed {
    private ZonedDateTime registrationDate;
    private String sleeperRightId;
    private String base;
    private Long returnRequestStatus;
    private String size;
    private String name;
    private String serial;
    @SerializedName("isKidsBed")
    private Boolean kidsBed;
    private Boolean dualSleep;
    private String bedId;
    private Long status;
    private String sleeperLeftId;
    private String version;
    private String accountId;
    private String timezone;
    private String model;
    private ZonedDateTime purchaseDate;
    private String macAddress;
    private String sku;
    @SerializedName("zipcode")
    private String zipCode;
    private String reference;

    public ZonedDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(ZonedDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Bed withRegistrationDate(ZonedDateTime registrationDate) {
        setRegistrationDate(registrationDate);
        return this;
    }

    public String getSleeperRightId() {
        return sleeperRightId;
    }

    public void setSleeperRightId(String sleeperRightId) {
        this.sleeperRightId = sleeperRightId;
    }

    public Bed withSleeperRightId(String sleeperRightId) {
        setSleeperRightId(sleeperRightId);
        return this;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Bed withBase(String base) {
        setBase(base);
        return this;
    }

    public Long getReturnRequestStatus() {
        return returnRequestStatus;
    }

    public void setReturnRequestStatus(Long returnRequestStatus) {
        this.returnRequestStatus = returnRequestStatus;
    }

    public Bed withReturnRequestStatus(Long returnRequestStatus) {
        setReturnRequestStatus(returnRequestStatus);
        return this;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Bed withSize(String size) {
        setSize(size);
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bed withName(String name) {
        setName(name);
        return this;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Bed withSerial(String serial) {
        setSerial(serial);
        return this;
    }

    public Boolean isKidsBed() {
        return kidsBed;
    }

    public void setKidsBed(Boolean kidsBed) {
        this.kidsBed = kidsBed;
    }

    public Bed withKidsBed(Boolean kidsBed) {
        setKidsBed(kidsBed);
        return this;
    }

    public Boolean isDualSleep() {
        return dualSleep;
    }

    public void setDualSleep(Boolean dualSleep) {
        this.dualSleep = dualSleep;
    }

    public Bed withDualSleep(Boolean dualSleep) {
        setDualSleep(dualSleep);
        return this;
    }

    public String getBedId() {
        return bedId;
    }

    public void setBedId(String bedId) {
        this.bedId = bedId;
    }

    public Bed withBedId(String bedId) {
        setBedId(bedId);
        return this;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Bed withStatus(Long status) {
        setStatus(status);
        return this;
    }

    public String getSleeperLeftId() {
        return sleeperLeftId;
    }

    public void setSleeperLeftId(String sleeperLeftId) {
        this.sleeperLeftId = sleeperLeftId;
    }

    public Bed withSleeperLeftId(String sleeperLeftId) {
        setSleeperLeftId(sleeperLeftId);
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Bed withVersion(String version) {
        setVersion(version);
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Bed withAccountId(String accountId) {
        setAccountId(accountId);
        return this;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Bed withTimezone(String timezone) {
        setTimezone(timezone);
        return this;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Bed withModel(String model) {
        setModel(model);
        return this;
    }

    public ZonedDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(ZonedDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Bed withPurchaseDate(ZonedDateTime purchaseDate) {
        setPurchaseDate(purchaseDate);
        return this;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Bed withMacAddress(String macAddress) {
        setMacAddress(macAddress);
        return this;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Bed withSku(String sku) {
        setSku(sku);
        return this;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Bed withZipCode(String zipCode) {
        setZipCode(zipCode);
        return this;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Bed withReference(String reference) {
        setReference(reference);
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bedId == null) ? 0 : bedId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Bed)) {
            return false;
        }
        Bed other = (Bed) obj;
        if (bedId == null) {
            if (other.bedId != null) {
                return false;
            }
        } else if (!bedId.equals(other.bedId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Bed [registrationDate=");
        builder.append(registrationDate);
        builder.append(", sleeperRightId=");
        builder.append(sleeperRightId);
        builder.append(", base=");
        builder.append(base);
        builder.append(", returnRequestStatus=");
        builder.append(returnRequestStatus);
        builder.append(", size=");
        builder.append(size);
        builder.append(", name=");
        builder.append(name);
        builder.append(", serial=");
        builder.append(serial);
        builder.append(", kidsBed=");
        builder.append(kidsBed);
        builder.append(", dualSleep=");
        builder.append(dualSleep);
        builder.append(", bedId=");
        builder.append(bedId);
        builder.append(", status=");
        builder.append(status);
        builder.append(", sleeperLeftId=");
        builder.append(sleeperLeftId);
        builder.append(", version=");
        builder.append(version);
        builder.append(", accountId=");
        builder.append(accountId);
        builder.append(", timezone=");
        builder.append(timezone);
        builder.append(", model=");
        builder.append(model);
        builder.append(", purchaseDate=");
        builder.append(purchaseDate);
        builder.append(", macAddress=");
        builder.append(macAddress);
        builder.append(", sku=");
        builder.append(sku);
        builder.append(", zipCode=");
        builder.append(zipCode);
        builder.append(", reference=");
        builder.append(reference);
        builder.append("]");
        return builder.toString();
    }
}
