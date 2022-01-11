/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.neato.internal.classes;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Robot} is the internal class for Neato Robot. Information is retrieved from the web service call.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class Robot {

    private String serial;
    private Object prefix;
    private String name;
    private String model;
    @SerializedName("secret_key")
    private String secretKey;
    private List<Object> traits = null;
    @SerializedName("purchased_at")
    private Object purchasedAt;
    @SerializedName("linked_at")
    private String linkedAt;
    @SerializedName("proof_of_purchase_url")
    private Object proofOfPurchaseUrl;
    @SerializedName("proof_of_purchase_url_valid_for_seconds")
    private Integer proofOfPurchaseUrlValidForSeconds;
    @SerializedName("proof_of_purchase_generated_at")
    private Object proofOfPurchaseGeneratedAt;
    @SerializedName("mac_address")
    private String macAddress;
    @SerializedName("created_at")
    private String createdAt;

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Object getPrefix() {
        return prefix;
    }

    public void setPrefix(Object prefix) {
        this.prefix = prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Object getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(Object purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public String getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(String linkedAt) {
        this.linkedAt = linkedAt;
    }

    public List<Object> getTraits() {
        return traits;
    }

    public void setTraits(List<Object> traits) {
        this.traits = traits;
    }

    public Object getProofOfPurchaseUrl() {
        return proofOfPurchaseUrl;
    }

    public void setProofOfPurchaseUrl(Object proofOfPurchaseUrl) {
        this.proofOfPurchaseUrl = proofOfPurchaseUrl;
    }

    public Integer getProofOfPurchaseUrlValidForSeconds() {
        return proofOfPurchaseUrlValidForSeconds;
    }

    public void setProofOfPurchaseUrlValidForSeconds(Integer proofOfPurchaseUrlValidForSeconds) {
        this.proofOfPurchaseUrlValidForSeconds = proofOfPurchaseUrlValidForSeconds;
    }

    public Object getProofOfPurchaseGeneratedAt() {
        return proofOfPurchaseGeneratedAt;
    }

    public void setProofOfPurchaseGeneratedAt(Object proofOfPurchaseGeneratedAt) {
        this.proofOfPurchaseGeneratedAt = proofOfPurchaseGeneratedAt;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean discoveryInformationPresent() {
        return serial != null && secretKey != null && name != null && model != null;
    }
}
