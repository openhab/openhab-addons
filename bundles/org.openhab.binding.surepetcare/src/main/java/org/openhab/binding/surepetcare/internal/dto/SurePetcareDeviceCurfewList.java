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
package org.openhab.binding.surepetcare.internal.dto;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;

/**
 * The {@link SurePetcareDeviceCurfewList} class is used to serialize a list of curfew parameters.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareDeviceCurfewList extends ArrayList<SurePetcareDeviceCurfew> {

    private static final long serialVersionUID = -6947992959305282143L;

    /**
     * Return the list element with the given index. If the list if too short, it will grow automatically to the given
     * index.
     *
     * @return element with given index
     */
    @Override
    public SurePetcareDeviceCurfew get(int index) {
        while (size() <= index) {
            // grow list to required size
            add(new SurePetcareDeviceCurfew());
        }
        return super.get(index);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for (SurePetcareDeviceCurfew c : this) {
            builder.append("(").append(c).append(")");
        }
        builder.append("]]");
        return builder.toString();
    }

    /**
     * Creates a list of curfews with enabled ones at the front of the list and disabled ones at the back.
     *
     * @return new ordered list.
     */
    public SurePetcareDeviceCurfewList order() {
        SurePetcareDeviceCurfewList orderedList = new SurePetcareDeviceCurfewList();
        // remove any disabled curfews from the list
        for (SurePetcareDeviceCurfew curfew : this) {
            if (curfew.enabled) {
                orderedList.add(curfew);
            }
        }
        // now fill up the list with empty disabled slots.
        for (int i = orderedList.size(); i < SurePetcareConstants.FLAP_MAX_NUMBER_OF_CURFEWS; i++) {
            orderedList.add(new SurePetcareDeviceCurfew());
        }
        return orderedList;
    }

    /**
     * Trims the list of curfews and removes any disabled ones.
     *
     * @return the new compact list of curfews.
     */
    public SurePetcareDeviceCurfewList compact() {
        SurePetcareDeviceCurfewList compactList = new SurePetcareDeviceCurfewList();
        // remove any disabled curfews from the list
        for (SurePetcareDeviceCurfew curfew : this) {
            if (curfew.enabled) {
                compactList.add(curfew);
            }
        }
        return compactList;
    }
}
