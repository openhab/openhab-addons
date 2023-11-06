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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * <B>Velux</B> product characteristics: Product State.
 * <P>
 * Combined set of information which describes a current state of a single Velux product.
 * </P>
 * Methods in handle this type of information:
 * <UL>
 * <LI>{@link #VeluxProductState(VeluxProductReference, int, int)} to create a new product state.</LI>
 * <LI>{@link #getActuator()} to retrieve the number representing this actuator/product.</LI>
 * <LI>{@link #getProductReference()} to retrieve reference to a product.</LI>
 * <LI>{@link #getState()} to retrieve the current {@link VeluxProductState.ProductState product state}.</LI>
 * <LI>{@link #getStateAsInt()} to retrieve the current product state.</LI>
 * <LI>{@link #toString} to retrieve a human-readable description of the product state.</LI>
 * </UL>
 *
 *
 * @author Guenther Schreiner - initial contribution.
 */
@NonNullByDefault
public class VeluxProductState {

    // Type definitions

    private class ProductState {

        private int state;

        public int getState() {
            return state;
        }

        private ProductState(int state) {
            this.state = state;
        }
    }

    // Class internal

    private VeluxProductReference productReference;
    private int actuator;
    private ProductState state;

    // Constructor

    public VeluxProductState(VeluxProductReference productReference, int actuator, int state) {
        this.productReference = productReference;
        this.actuator = actuator;
        this.state = new ProductState(state);
    }

    // Class access methods

    public VeluxProductReference getProductReference() {
        return this.productReference;
    }

    public int getActuator() {
        return this.actuator;
    }

    public ProductState getState() {
        return this.state;
    }

    public int getStateAsInt() {
        return this.state.getState();
    }

    @Override
    public String toString() {
        return String.format("State (%s, actuator %d, value %d)", this.productReference, this.actuator, this.state);
    }
}
