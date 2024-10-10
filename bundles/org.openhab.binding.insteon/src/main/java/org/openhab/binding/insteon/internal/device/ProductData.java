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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * The {@link ProductData} represents a device product data
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ProductData {
    public static final int DEVICE_CATEGORY_UNKNOWN = 0xFF;
    public static final int SUB_CATEGORY_UNKNOWN = 0xFF;

    private int deviceCategory = DEVICE_CATEGORY_UNKNOWN;
    private int subCategory = SUB_CATEGORY_UNKNOWN;
    private int productKey = 0;
    private @Nullable String description;
    private @Nullable String model;
    private @Nullable String vendor;
    private @Nullable String deviceType;
    private int firstRecord = 0;
    private int firmware = 0;
    private int hardware = 0;

    public int getDeviceCategory() {
        return deviceCategory;
    }

    public int getSubCategory() {
        return subCategory;
    }

    public int getProductKey() {
        return productKey;
    }

    public @Nullable String getProductId() {
        return deviceCategory == DEVICE_CATEGORY_UNKNOWN || subCategory == SUB_CATEGORY_UNKNOWN ? null
                : HexUtils.getHexString(deviceCategory) + " " + HexUtils.getHexString(subCategory);
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
        return DeviceTypeRegistry.getInstance().getDeviceType(deviceType);
    }

    public int getFirstRecordLocation() {
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

    public byte[] getRecordData() {
        return new byte[] { (byte) deviceCategory, (byte) subCategory, (byte) firmware };
    }

    public void setDeviceCategory(int deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public void setSubCategory(int subCategory) {
        this.subCategory = subCategory;
    }

    public void setProductKey(int productKey) {
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

    public void setFirstRecordLocation(int firstRecord) {
        this.firstRecord = firstRecord;
    }

    public void setFirmwareVersion(int firmware) {
        this.firmware = firmware;
    }

    public void setHardwareVersion(int hardware) {
        this.hardware = hardware;
    }

    public boolean update(ProductData productData) {
        boolean deviceTypeUpdated = false;
        // update device and sub category if unknown
        if (deviceCategory == DEVICE_CATEGORY_UNKNOWN && subCategory == SUB_CATEGORY_UNKNOWN) {
            deviceCategory = productData.deviceCategory;
            subCategory = productData.subCategory;
        }
        // update device type if not defined already
        if (deviceType == null) {
            deviceType = productData.deviceType;
            deviceTypeUpdated = productData.deviceType != null;
        }
        // update remaining properties if defined in given product data
        if (productData.productKey != 0) {
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
        return deviceTypeUpdated;
    }

    @Override
    public String toString() {
        List<String> properties = new ArrayList<>();
        if (deviceCategory != DEVICE_CATEGORY_UNKNOWN) {
            properties.add("deviceCategory:" + HexUtils.getHexString(deviceCategory));
        }
        if (subCategory != SUB_CATEGORY_UNKNOWN) {
            properties.add("subCategory:" + HexUtils.getHexString(subCategory));
        }
        if (productKey != 0) {
            properties.add("productKey:" + HexUtils.getHexString(productKey, 6));
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
            properties.add("firstRecord:" + HexUtils.getHexString(firstRecord));
        }
        if (firmware != 0) {
            properties.add("firmwareVersion:" + HexUtils.getHexString(firmware));
        }
        if (hardware != 0) {
            properties.add("hardwareVersion:" + HexUtils.getHexString(hardware));
        }
        return properties.isEmpty() ? "undefined product data" : String.join("|", properties);
    }

    /**
     * Factory method for creating a ProductData for an Insteon product
     *
     * @param deviceCategory the Insteon device category
     * @param subCategory the Insteon device subcategory
     * @return the product data
     */
    public static ProductData makeInsteonProduct(int deviceCategory, int subCategory) {
        ProductData productData = new ProductData();
        productData.setDeviceCategory(deviceCategory);
        productData.setSubCategory(subCategory);
        ProductData resourceData = ProductDataRegistry.getInstance().getProductData(deviceCategory, subCategory);
        if (resourceData != null) {
            productData.update(resourceData);
        }
        return productData;
    }

    /**
     * Factory method for creating a ProductData for a X10 product
     *
     * @param deviceType the X10 device type
     * @return the product data
     */
    public static ProductData makeX10Product(String deviceType) {
        ProductData productData = new ProductData();
        productData.setDeviceType(deviceType);
        productData.setDescription(deviceType.replace("_", " "));
        return productData;
    }
}
