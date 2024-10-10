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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonResourceLoader;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The {@link ProductDataRegistry} represents the product data registry
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ProductDataRegistry extends InsteonResourceLoader {
    private static final ProductDataRegistry PRODUCT_DATA_REGISTRY = new ProductDataRegistry();
    private static final String RESOURCE_NAME = "/device-products.xml";

    private Map<Integer, ProductData> products = new HashMap<>();

    private ProductDataRegistry() {
        super(RESOURCE_NAME);
    }

    /**
     * Returns the product data for a given dev/sub category
     *
     * @param deviceCategory device category to match
     * @param subCategory device subcategory to match
     * @return product data if matching provided parameters, otherwise null
     */
    public @Nullable ProductData getProductData(int deviceCategory, int subCategory) {
        int productId = getProductId(deviceCategory, subCategory);
        if (!products.containsKey(productId)) {
            logger.warn("unknown product for devCat:{} subCat:{} in device products xml file",
                    HexUtils.getHexString(deviceCategory), HexUtils.getHexString(subCategory));
            // fallback to matching product id using device category only
            productId = getProductId(deviceCategory, ProductData.SUB_CATEGORY_UNKNOWN);
        }
        return products.get(productId);
    }

    /**
     * Returns product id based on dev/sub category
     *
     * @param deviceCategory device category to use
     * @param subCategory device subcategory to use
     * @return product key
     */
    private int getProductId(int deviceCategory, int subCategory) {
        return deviceCategory << 8 | subCategory;
    }

    /**
     * Returns known products
     *
     * @return currently known products
     */
    public Map<Integer, ProductData> getProducts() {
        return products;
    }

    /**
     * Initializes product data registry
     */
    @Override
    protected void initialize() {
        super.initialize();

        logger.debug("loaded {} products", products.size());
        if (logger.isTraceEnabled()) {
            products.values().stream().map(String::valueOf).forEach(logger::trace);
        }
    }

    /**
     * Parses product data document
     *
     * @param element element to parse
     * @throws SAXException
     */
    @Override
    protected void parseDocument(Element element) throws SAXException {
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                if ("product".equals(nodeName)) {
                    parseProduct(child);
                }
            }
        }
    }

    /**
     * Parses product node
     *
     * @param element element to parse
     * @throws SAXException
     */
    private void parseProduct(Element element) throws SAXException {
        int deviceCategory = getHexAttributeAsInteger(element, "devCat", ProductData.DEVICE_CATEGORY_UNKNOWN);
        int subCategory = getHexAttributeAsInteger(element, "subCat", ProductData.SUB_CATEGORY_UNKNOWN);
        int productKey = getHexAttributeAsInteger(element, "productKey", 0);
        int firstRecord = getHexAttributeAsInteger(element, "firstRecord", 0);
        if (deviceCategory == ProductData.DEVICE_CATEGORY_UNKNOWN) {
            throw new SAXException("invalid product with no device category in device products xml file");
        }
        int productId = getProductId(deviceCategory, subCategory);
        if (products.containsKey(productId)) {
            logger.warn("overwriting previous definition of product {}", products.get(productId));
        }

        ProductData productData = new ProductData();
        productData.setDeviceCategory(deviceCategory);
        productData.setSubCategory(subCategory);
        productData.setProductKey(productKey);
        productData.setFirstRecordLocation(firstRecord);

        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element) node;
                String nodeName = child.getNodeName();
                String textContent = child.getTextContent();
                if ("description".equals(nodeName)) {
                    productData.setDescription(textContent);
                } else if ("model".equals(nodeName)) {
                    productData.setModel(textContent);
                } else if ("vendor".equals(nodeName)) {
                    productData.setVendor(textContent);
                } else if ("device-type".equals(nodeName)) {
                    parseDeviceType(child, productData);
                }
            }
        }
        products.put(productId, productData);
    }

    /**
     * Parses product device type element
     *
     * @param element element to parse
     * @param productData product data to update
     * @throws SAXException
     */
    private void parseDeviceType(Element element, ProductData productData) throws SAXException {
        String deviceType = element.getTextContent();
        if (deviceType == null) {
            return; // undefined device type
        }
        if (DeviceTypeRegistry.getInstance().getDeviceType(deviceType) == null) {
            throw new SAXException("invalid device type " + deviceType + " in device products xml file");
        }
        productData.setDeviceType(deviceType);
    }

    /**
     * Singleton instance function
     *
     * @return ProductDataRegistry singleton reference
     */
    public static synchronized ProductDataRegistry getInstance() {
        if (PRODUCT_DATA_REGISTRY.getProducts().isEmpty()) {
            PRODUCT_DATA_REGISTRY.initialize();
        }
        return PRODUCT_DATA_REGISTRY;
    }
}
