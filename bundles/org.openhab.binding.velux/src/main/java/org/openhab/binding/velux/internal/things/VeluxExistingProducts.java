/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductState;
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

    /*
     * Permitted list of product states whose position values shall be accepted.
     */
    private static final List<ProductState> PERMITTED_VALUE_STATES = Arrays.asList(ProductState.EXECUTING,
            ProductState.DONE);

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

    /**
     * Update the product in the existing products database by applying the data from the new product argument. This
     * method may ignore the new product if it was created by certain originating commands, or if the new product has
     * certain actuator states.
     *
     * @param requestingCommand the command that requested the data from the hub and so triggered calling this method.
     * @param newProduct the product containing new data.
     *
     * @return true if the product exists in the database.
     */
    public boolean update(VeluxProduct newProduct) {
        ProductBridgeIndex productBridgeIndex = newProduct.getBridgeProductIndex();
        if (!isRegistered(productBridgeIndex)) {
            logger.warn("update() failed as actuator (with index {}) is not registered.", productBridgeIndex.toInt());
            return false;
        }

        VeluxProduct theProduct = this.get(productBridgeIndex);

        String oldProduct = "";
        if (logger.isDebugEnabled()) {
            oldProduct = theProduct.toString();
        }

        boolean dirty = false;

        // ignore commands with state 'not used'
        boolean ignoreNotUsed = (ProductState.NOT_USED == ProductState.of(newProduct.getState()));

        // specially ignore commands from buggy devices (e.g. Somfy) which have bad data
        boolean ignoreSpecial = theProduct.isSomfyProduct()
                && (Command.GW_OPENHAB_RECEIVEONLY == newProduct.getCreatorCommand())
                && !VeluxProductPosition.isValid(newProduct.getCurrentPosition())
                && !VeluxProductPosition.isValid(newProduct.getTarget());

        if ((!ignoreNotUsed) && (!ignoreSpecial)) {
            int newState = newProduct.getState();
            int theState = theProduct.getState();

            // always update the actuator state, but only set dirty flag if they are not operationally equivalent
            if (theProduct.setState(newState)) {
                dirty |= !ProductState.equivalent(theState, newState);
            }

            // only update the actual position values if the state is permitted
            if (PERMITTED_VALUE_STATES.contains(ProductState.of(newState))) {
                int newValue = newProduct.getCurrentPosition();
                if (VeluxProductPosition.isUnknownOrValid(newValue)) {
                    dirty |= theProduct.setCurrentPosition(newValue);
                }
                newValue = newProduct.getTarget();
                if (VeluxProductPosition.isUnknownOrValid(newValue)) {
                    dirty |= theProduct.setTarget(newValue);
                }
                if (theProduct.supportsVanePosition()) {
                    FunctionalParameters newFunctionalParameters = newProduct.getFunctionalParameters();
                    if (newFunctionalParameters != null) {
                        dirty |= theProduct.setFunctionalParameters(newFunctionalParameters);
                    }
                }
            }
        }

        // update modified product database
        if (dirty) {
            this.dirty = true;
            String uniqueIndex = theProduct.getProductUniqueIndex();
            logger.trace("update(): updating by UniqueIndex {}.", uniqueIndex);
            existingProductsByUniqueIndex.replace(uniqueIndex, theProduct);
            modifiedProductsByUniqueIndex.put(uniqueIndex, theProduct);
        }

        if (logger.isDebugEnabled()) {
            if (dirty) {
                logger.debug("update() theProduct:{} (previous)", oldProduct);
            }
            logger.debug("update() newProduct:{} ({})", newProduct, dirty ? "modifier" : "identical");
            logger.debug("update() theProduct:{} ({})", theProduct, dirty ? "modified" : "unchanged");
        }

        return true;
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
