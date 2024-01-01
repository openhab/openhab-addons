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
package org.openhab.binding.smartmeter.internal.conformity.negate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Models the negate bit - namely the OBIS code, whether its in the status bytes and on which position (of the status)
 * it is encoded.
 *
 * @author Matthias Steigenberger - Initial contribution
 *
 */
@NonNullByDefault
public class NegateBitModel {

    private int negatePosition;
    private boolean negateBit;
    private String negateObis;
    private boolean status;

    /**
     *
     * @param negatePosition
     * @param negateBit
     * @param negateObis
     * @param status Whether to get the negate bit from status value or from actual value.
     */
    public NegateBitModel(int negatePosition, boolean negateBit, String negateObis, boolean status) {
        this.negatePosition = negatePosition;
        this.negateBit = negateBit;
        this.negateObis = negateObis;
        this.status = status;
    }

    public int getNegatePosition() {
        return negatePosition;
    }

    public boolean isNegateBit() {
        return negateBit;
    }

    public String getNegateChannelId() {
        return negateObis;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negateBit ? 1231 : 1237);
        result = prime * result + (negateObis.hashCode());
        result = prime * result + negatePosition;
        return result;
    }

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NegateBitModel other = (NegateBitModel) obj;
        if (negateBit != other.negateBit) {
            return false;
        }
        if (!negateObis.equals(other.negateObis)) {
            return false;
        }
        if (negatePosition != other.negatePosition) {
            return false;
        }
        return true;
    }

    public boolean isStatus() {
        return status;
    }
}
