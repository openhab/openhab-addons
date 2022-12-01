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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.ByteUtils;

/**
 * Class that represents device product data
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ProductData {
    private @Nullable String deviceCategory;
    private @Nullable String subCategory;
    private @Nullable String productKey;
    private @Nullable String description;
    private @Nullable String model;
    private @Nullable String vendor;
    private @Nullable String deviceType;
    private int firstRecord = 0;
    private int firmware = 0;
    private int hardware = 0;

    public @Nullable String getDeviceCategory() {
        return deviceCategory;
    }

    public @Nullable String getSubCategory() {
        return subCategory;
    }

    public @Nullable String getProductId() {
        return deviceCategory == null || subCategory == null ? null : deviceCategory + " " + subCategory;
    }

    public @Nullable String getProductKey() {
        return productKey;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable String getModel() {
        return model;
    }

    public @Nullable String getVendor() {
        return vendor;
    }

    public @Nullable DeviceType getDeviceType() {
        return DeviceTypeLoader.instance().getDeviceType(deviceType);
    }

    public int getFirstRecordOffset() {
        return firstRecord;
    }

    public int getFirmwareVersion() {
        return firmware;
    }

    public int getHardwareVersion() {
        return hardware;
    }

    public @Nullable String getLabel() {
        List<String> properties = new ArrayList<>();
        if (vendor != null) {
            properties.add("" + vendor);
        }
        if (model != null) {
            properties.add("" + model);
        }
        if (description != null) {
            properties.add("" + description);
        }
        return properties.isEmpty() ? null : String.join(" ", properties);
    }

    public void setDeviceCategory(@Nullable String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public void setSubCategory(@Nullable String subCategory) {
        this.subCategory = subCategory;
    }

    public void setProductKey(@Nullable String productKey) {
        this.productKey = productKey;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public void setModel(@Nullable String model) {
        this.model = model;
    }

    public void setVendor(@Nullable String vendor) {
        this.vendor = vendor;
    }

    public void setDeviceType(@Nullable String deviceType) {
        this.deviceType = deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType.getName();
    }

    public void setFirstRecordOffset(int firstRecord) {
        this.firstRecord = firstRecord;
    }

    public void setFirmwareVersion(int firmware) {
        this.firmware = firmware;
    }

    public void setHardwareVersion(int hardware) {
        this.hardware = hardware;
    }

    public void update(ProductData productData) {
        // update device and sub category if not defined already
        if (deviceCategory == null && subCategory == null) {
            deviceCategory = productData.deviceCategory;
            subCategory = productData.subCategory;
        }
        // update device type if not defined already
        if (deviceType == null) {
            deviceType = productData.deviceType;
        }
        // update remaining properties if defined in given product data
        if (productData.productKey != null) {
            productKey = productData.productKey;
        }
        if (productData.description != null) {
            description = productData.description;
        }
        if (productData.model != null) {
            model = productData.model;
        }
        if (productData.vendor != null) {
            vendor = productData.vendor;
        }
        if (productData.firstRecord != 0) {
            firstRecord = productData.firstRecord;
        }
        if (productData.firmware != 0) {
            firmware = productData.firmware;
        }
        if (productData.hardware != 0) {
            hardware = productData.hardware;
        }
    }

    @Override
    public String toString() {
        List<String> properties = new ArrayList<>();
        if (deviceCategory != null) {
            properties.add("deviceCategory:" + deviceCategory);
        }
        if (subCategory != null) {
            properties.add("subCategory:" + subCategory);
        }
        if (productKey != null) {
            properties.add("productKey:" + productKey);
        }
        if (description != null) {
            properties.add("description:" + description);
        }
        if (model != null) {
            properties.add("model:" + model);
        }
        if (vendor != null) {
            properties.add("vendor:" + vendor);
        }
        if (deviceType != null) {
            properties.add("deviceType:" + deviceType);
        }
        if (firstRecord != 0) {
            properties.add("firstRecord:" + ByteUtils.getHexString(firstRecord));
        }
        if (firmware != 0) {
            properties.add("firmwareVersion:" + ByteUtils.getHexString(firmware));
        }
        if (hardware != 0) {
            properties.add("hardwareVersion:" + ByteUtils.getHexString(hardware));
        }
        return properties.isEmpty() ? "undefined product data" : String.join("|", properties);
    }

    /**
     * Factory method for getting a ProductData for Insteon product
     *
     * @param deviceCategory the Insteon device category
     * @param subCategory the Insteon device subcategory
     * @param productKey the Insteon product key
     * @return the product data
     */
    public static ProductData makeInsteonProduct(String deviceCategory, @Nullable String subCategory,
            @Nullable String productKey) {
        ProductData productData = new ProductData();
        productData.setDeviceCategory(deviceCategory);
        productData.setSubCategory(subCategory);
        productData.setProductKey(productKey);
        return productData;
    }

    /**
     * Factory method for getting a ProductData for X10 product
     *
     * @param deviceType the X10 device type
     * @return the product data
     */
    public static ProductData makeX10Product(DeviceType deviceType) {
        ProductData productData = new ProductData();
        productData.setDeviceType(deviceType);
        productData.setDescription(deviceType.getName().replace("_", " "));
        return productData;
    }
}
