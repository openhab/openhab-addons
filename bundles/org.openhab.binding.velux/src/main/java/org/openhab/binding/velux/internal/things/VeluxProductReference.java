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
package org.openhab.binding.velux.internal.things;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <B>Velux</B> product characteristics: Product Reference.
 * <P>
 * Combined set of information which describes a current state of a single Velux product.
 * </P>
 * Methods in handle this type of information:
 * <UL>
 * <LI>{@link #getProductName()} to retrieve the name representing this actuator/product.</LI>
 * <LI>{@link #getProductType()} to retrieve the type of the product.</LI>
 * <LI>{@link #toString} to retrieve a human-readable description of the product state.</LI>
 * </UL>
 *
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxProductReference {
    private final Logger logger = LoggerFactory.getLogger(VeluxProductReference.class);

    // Class internal

    private final VeluxProductName name;
    private final VeluxProductType typeId;

    // Constructor

    /**
     * Initializes the {@link VeluxProductReference} based on a given {@link VeluxProduct} and its associated type.
     * <P>
     *
     * @param name as {@link VeluxProductName} referencing to a specific actuator/product.
     * @param type as int as handled by {@link VeluxProductType#get(int)}.
     */
    public VeluxProductReference(VeluxProductName name, int type) {
        this.name = name;
        this.typeId = VeluxProductType.get(type);
        if (this.typeId == VeluxProductType.UNDEFTYPE) {
            logger.warn(
                    "Please report this to maintainer of the {} binding: VeluxProductReference({}) has found an unregistered ProductTypeId.",
                    VeluxBindingConstants.BINDING_ID, type);
        }
    }

    // Class access methods

    public VeluxProductName getProductName() {
        return this.name;
    }

    public VeluxProductType getProductType() {
        return this.typeId;
    }

    @Override
    public String toString() {
        return String.format("Prod.ref. \"%s\"/%s", this.name, this.typeId);
    }
}
