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
package org.openhab.binding.velux.internal.things;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combined set of product informations provided by the <B>Velux</B> bridge,
 * which can be used for later interactions.
 * <P>
 * The following class access methods exist:
 * <UL>
 * <LI>{@link VeluxExistingProducts#isRegistered} for querying existence of a {@link VeluxProduct},</LI>
 * <LI>{@link VeluxExistingProducts#register} for storing a {@link VeluxProduct},</LI>
 * <LI>{@link VeluxExistingProducts#update} for updating/storing of a {@link VeluxProduct},</LI>
 * <LI>{@link VeluxExistingProducts#get} for retrieval of a {@link VeluxProduct},</LI>
 * <LI>{@link VeluxExistingProducts#values} for retrieval of all {@link VeluxProduct}s,</LI>
 * <LI>{@link VeluxExistingProducts#getNoMembers} for retrieval of the number of all {@link VeluxProduct}s,</LI>
 * <LI>{@link VeluxExistingProducts#toString} for a descriptive string representation.</LI>
 * </UL>
 *
 * @see VeluxProduct
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxExistingProducts {
    private final Logger logger = LoggerFactory.getLogger(VeluxExistingProducts.class);

    // Type definitions, class-internal variables

    private Map<String, VeluxProduct> existingProductsByUniqueIndex;
    private Map<Integer, String> bridgeIndexToUniqueIndex;
    private Map<String, VeluxProduct> modifiedProductsByUniqueIndex;
    private int memberCount;

    /*
     * Value to flag any changes towards the getter.
     */
    private boolean dirty;

    // Constructor methods

    public VeluxExistingProducts() {
        logger.trace("VeluxExistingProducts(constructor) called.");
        existingProductsByUniqueIndex = new ConcurrentHashMap<>();
        bridgeIndexToUniqueIndex = new ConcurrentHashMap<>();
        modifiedProductsByUniqueIndex = new ConcurrentHashMap<>();
        memberCount = 0;
        dirty = true;
        logger.trace("VeluxExistingProducts(constructor) done.");
    }

    // Class access methods

    public boolean isRegistered(String productUniqueIndex) {
        boolean result = existingProductsByUniqueIndex.containsKey(productUniqueIndex);
        logger.trace("isRegistered(String {}) returns {}.", productUniqueIndex, result);
        return result;
    }

    public boolean isRegistered(VeluxProduct product) {
        boolean result = existingProductsByUniqueIndex.containsKey(product.getProductUniqueIndex());
        logger.trace("isRegistered(VeluxProduct {}) returns {}.", product, result);
        return result;
    }

    public boolean isRegistered(ProductBridgeIndex bridgeProductIndex) {
        boolean result = bridgeIndexToUniqueIndex.containsKey(bridgeProductIndex.toInt());
        logger.trace("isRegistered(ProductBridgeIndex {}) returns {}.", bridgeProductIndex, result);
        return result;
    }

    public boolean register(VeluxProduct newProduct) {
        logger.trace("register({}) called.", newProduct);
        if (isRegistered(newProduct)) {
            return false;
        }
        logger.trace("register() registering new product {}.", newProduct);

        String uniqueIndex = newProduct.getProductUniqueIndex();
        logger.trace("register() registering by UniqueIndex {}", uniqueIndex);
        existingProductsByUniqueIndex.put(uniqueIndex, newProduct);

        logger.trace("register() registering by ProductBridgeIndex {}", newProduct.getBridgeProductIndex().toInt());
        bridgeIndexToUniqueIndex.put(newProduct.getBridgeProductIndex().toInt(), uniqueIndex);

        logger.trace("register() registering set of modifications by UniqueIndex {}", uniqueIndex);
        modifiedProductsByUniqueIndex.put(uniqueIndex, newProduct);

        memberCount++;
        dirty = true;
        return true;
    }

    public boolean update(ProductBridgeIndex bridgeProductIndex, int productState, int productPosition,
            int productTarget) {
        logger.debug("update(bridgeProductIndex={},productState={},productPosition={},productTarget={}) called.",
                bridgeProductIndex.toInt(), productState, productPosition, productTarget);
        if (!isRegistered(bridgeProductIndex)) {
            logger.warn("update() failed as actuator (with index {}) is not registered.", bridgeProductIndex.toInt());
            return false;
        }
        VeluxProduct thisProduct = this.get(bridgeProductIndex);
        dirty |= thisProduct.setState(productState);
        dirty |= thisProduct.setCurrentPosition(productPosition);
        dirty |= thisProduct.setTarget(productTarget);
        if (dirty) {
            String uniqueIndex = thisProduct.getProductUniqueIndex();
            logger.trace("update(): updating by UniqueIndex {}.", uniqueIndex);
            existingProductsByUniqueIndex.replace(uniqueIndex, thisProduct);
            modifiedProductsByUniqueIndex.put(uniqueIndex, thisProduct);
        }
        logger.trace("update() successfully finished (dirty={}).", dirty);
        return true;
    }

    public boolean update(VeluxProduct currentProduct) {
        logger.trace("update(currentProduct={}) called.", currentProduct);
        return update(currentProduct.getBridgeProductIndex(), currentProduct.getState(),
                currentProduct.getCurrentPosition(), currentProduct.getTarget());
    }

    public VeluxProduct get(String productUniqueIndex) {
        logger.trace("get({}) called.", productUniqueIndex);
        return existingProductsByUniqueIndex.getOrDefault(productUniqueIndex, VeluxProduct.UNKNOWN);
    }

    public VeluxProduct get(ProductBridgeIndex bridgeProductIndex) {
        logger.trace("get({}) called.", bridgeProductIndex);
        String unique = bridgeIndexToUniqueIndex.get(bridgeProductIndex.toInt());
        return unique != null ? existingProductsByUniqueIndex.getOrDefault(unique, VeluxProduct.UNKNOWN)
                : VeluxProduct.UNKNOWN;
    }

    public VeluxProduct[] values() {
        return existingProductsByUniqueIndex.values().toArray(new VeluxProduct[0]);
    }

    public VeluxProduct[] valuesOfModified() {
        return modifiedProductsByUniqueIndex.values().toArray(new VeluxProduct[0]);
    }

    public int getNoMembers() {
        logger.trace("getNoMembers() returns {}.", memberCount);
        return memberCount;
    }

    public boolean isDirty() {
        logger.trace("isDirty() returns {}.", dirty);
        return dirty;
    }

    public void resetDirtyFlag() {
        logger.trace("resetDirtyFlag() called.");
        modifiedProductsByUniqueIndex = new ConcurrentHashMap<>();
        dirty = false;
    }

    public String toString(boolean showSummary, String delimiter) {
        StringBuilder sb = new StringBuilder();

        if (showSummary) {
            sb.append(memberCount).append(" members: ");
        }
        for (VeluxProduct product : this.values()) {
            sb.append(product).append(delimiter);
        }
        if (sb.lastIndexOf(delimiter) > 0) {
            sb.deleteCharAt(sb.lastIndexOf(delimiter));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(true, VeluxBindingConstants.OUTPUT_VALUE_SEPARATOR);
    }
}
