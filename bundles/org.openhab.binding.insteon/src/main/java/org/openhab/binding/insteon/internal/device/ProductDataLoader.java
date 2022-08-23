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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the device products from an xml file.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ProductDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(ProductDataLoader.class);
    private static ProductDataLoader productDataLoader = new ProductDataLoader();
    private Map<String, ProductData> products = new HashMap<>();

    /**
     * Finds the product data for a given dev/sub category or product key
     *
     * @param deviceCategory device category to match
     * @param subCategory device subcategory to match
     * @param productKey product key to match
     * @return product data matching provided parameters
     */
    public ProductData getProductData(String deviceCategory, String subCategory, @Nullable String productKey) {
        String productId = getProductId(deviceCategory, subCategory);
        ProductData productIdData = products.get(productId);
        ProductData productKeyData = products.get(productKey);
        ProductData deviceCategoryData = products.get(deviceCategory);
        ProductData productData = new ProductData();
        // use product data matching product id, product key or device category
        if (productIdData != null) {
            productData.update(productIdData);
        } else if (productKeyData != null) {
            productData.update(productKeyData);
        } else if (deviceCategoryData != null) {
            productData.update(deviceCategoryData);
        }

        boolean mismatch = false;
        if (!deviceCategory.equals(productData.getDeviceCategory())) {
            productData.setDeviceCategory(deviceCategory);
            mismatch = true;
        }
        if (!subCategory.equals(productData.getSubCategory())) {
            productData.setSubCategory(subCategory);
            mismatch = true;
        }
        if (productKey != null && !productKey.equals(productData.getProductKey())) {
            productData.setProductKey(productKey);
            mismatch = true;
        }
        if (mismatch) {
            logger.warn("product mismatch for devCat:{} subCat:{} productKey:{} in device products xml file",
                    deviceCategory, subCategory, productKey);
        }

        return productData;
    }

    /**
     * Finds the product data for a given dev/sub category
     *
     * @param deviceCategory device category to match
     * @param subCategory device subcategory to match
     * @return product data matching provided parameters
     */
    public ProductData getProductData(String deviceCategory, String subCategory) {
        return getProductData(deviceCategory, subCategory, null);
    }

    /**
     * Returns product id based on dev/sub category
     *
     * @param deviceCategory device category to use
     * @param subCategory device subcategory to use
     * @return product id
     */
    public String getProductId(String deviceCategory, @Nullable String subCategory) {
        return deviceCategory + (subCategory == null ? "" : subCategory.substring(2));
    }

    /**
     * Returns known products
     *
     * @return currently known products
     */
    public Map<String, ProductData> getProducts() {
        return products;
    }

    /**
     * Reads the device products from input stream and stores them in memory for
     * later access.
     *
     * @param stream the input stream from which to read
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public void loadDeviceProductsXML(InputStream stream)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        // see https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
        dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbFactory.setXIncludeAware(false);
        dbFactory.setExpandEntityReferences(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(stream);
        doc.getDocumentElement().normalize();
        Node root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();
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
        String deviceCategory = element.hasAttribute("devCat") ? element.getAttribute("devCat") : null;
        String subCategory = element.hasAttribute("subCat") ? element.getAttribute("subCat") : null;
        String productKey = element.hasAttribute("productKey") ? element.getAttribute("productKey") : null;
        if (deviceCategory == null) {
            throw new SAXException("product data in device_products file has no device category!");
        }

        String productId = getProductId(deviceCategory, subCategory);
        if (products.containsKey(productId)) {
            logger.warn("overwriting previous definition of product {}", products.get(productId));
        }

        ProductData productData = ProductData.makeInsteonProduct(deviceCategory, subCategory, productKey);
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
        if (element.hasAttribute("firstRecord")) {
            parseFirstRecord(element, productData);
        }
        products.put(productId, productData);
        if (productKey != null) {
            products.put(productKey, productData);
        }
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
        if (DeviceTypeLoader.instance().getDeviceType(deviceType) == null) {
            logger.warn("unknown device type {} for devCat:{} subCat:{} productKey:{} in device products xml file",
                    deviceType, productData.getDeviceCategory(), productData.getSubCategory(),
                    productData.getProductKey());
        } else {
            productData.setDeviceType(deviceType);
        }
    }

    /**
     * Parses product first record element attribute
     *
     * @param element element to parse
     * @param productData product data to update
     * @throws SAXException
     */
    private void parseFirstRecord(Element element, ProductData productData) throws SAXException {
        String firstRecord = element.getAttribute("firstRecord");
        if (firstRecord == null) {
            return; // undefined first record
        }
        try {
            productData.setFirstRecordOffset(ByteUtils.hexStringToInteger(firstRecord));
        } catch (NumberFormatException e) {
            logger.warn("invalid first record {} for devCat:{} subCat:{} productKey:{} in device products xml file",
                    firstRecord, productData.getDeviceCategory(), productData.getSubCategory(),
                    productData.getProductKey());
        }
    }

    /**
     * Helper function for debugging
     */
    private void logProducts() {
        products.entrySet().stream().map(product -> String.format("%s->%s", product.getKey(), product.getValue()))
                .forEach(logger::debug);
    }

    /**
     * Singleton instance function
     *
     * @return ProductDataLoader singleton reference
     */
    public static synchronized ProductDataLoader instance() {
        if (productDataLoader.getProducts().isEmpty()) {
            InputStream input = ProductDataLoader.class.getResourceAsStream("/device_products.xml");
            try {
                if (input != null) {
                    productDataLoader.loadDeviceProductsXML(input);
                } else {
                    logger.warn("Resource stream is null, cannot read xml file.");
                }
            } catch (ParserConfigurationException e) {
                logger.warn("parser config error when reading device products xml file: ", e);
            } catch (SAXException e) {
                logger.warn("SAX exception when reading device products xml file: ", e);
            } catch (IOException e) {
                logger.warn("I/O exception when reading device products xml file: ", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("loaded {} products: ", productDataLoader.getProducts().size());
                productDataLoader.logProducts();
            }
        }
        return productDataLoader;
    }
}
