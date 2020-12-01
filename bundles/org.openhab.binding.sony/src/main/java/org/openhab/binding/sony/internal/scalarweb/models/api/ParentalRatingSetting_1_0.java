/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the parent rating settings and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ParentalRatingSetting_1_0 {

    /** The age limit for the rating type */
    private @Nullable Integer ratingTypeAge;

    /** The Sony rating type. */
    private @Nullable String ratingTypeSony;

    /** The country rating */
    private @Nullable String ratingCountry;

    /** The custom rating type */
    private @Nullable String @Nullable [] ratingCustomTypeTV;

    /** The MPAA rating type. */
    private @Nullable String ratingCustomTypeMpaa;

    /** The Canada Rating type (english) */
    private @Nullable String ratingCustomTypeCaEnglish;

    /** The Canada Rating type (french) */
    private @Nullable String ratingCustomTypeCaFrench;

    /** The unrated lock */
    private @Nullable Boolean unratedLock;

    /**
     * Constructor used for deserialization only
     */
    public ParentalRatingSetting_1_0() {
    }

    /**
     * Gets the rating age limit
     *
     * @return the rating age limit
     */
    public @Nullable Integer getRatingTypeAge() {
        return ratingTypeAge;
    }

    /**
     * Gets the Sony rating type
     *
     * @return the Sony rating type
     */
    public @Nullable String getRatingTypeSony() {
        return ratingTypeSony;
    }

    /**
     * Gets the rating country.
     *
     * @return the rating country
     */
    public @Nullable String getRatingCountry() {
        return ratingCountry;
    }

    /**
     * Gets the TV rating type
     *
     * @return the TV Rating type
     */
    public @Nullable String @Nullable [] getRatingCustomTypeTV() {
        return ratingCustomTypeTV;
    }

    /**
     * Gets the MPAA rating type
     *
     * @return the MPAA rating type
     */
    public @Nullable String getRatingCustomTypeMpaa() {
        return ratingCustomTypeMpaa;
    }

    /**
     * Gets the Canada (english) rating type
     *
     * @return the Canada (english) rating type
     */
    public @Nullable String getRatingCustomTypeCaEnglish() {
        return ratingCustomTypeCaEnglish;
    }

    /**
     * Gets the Canada (french) rating type
     *
     * @return the Canada (french) rating type
     */
    public @Nullable String getRatingCustomTypeCaFrench() {
        return ratingCustomTypeCaFrench;
    }

    /**
     * Checks if is unrated is locked
     *
     * @return true, if locked - false otherwise
     */
    public @Nullable Boolean isUnratedLock() {
        return unratedLock;
    }

    @Override
    public String toString() {
        return "ParentalRatingSetting_1_0 [ratingTypeAge=" + ratingTypeAge + ", ratingTypeSony=" + ratingTypeSony
                + ", ratingCountry=" + ratingCountry + ", ratingCustomTypeTV=" + Arrays.toString(ratingCustomTypeTV)
                + ", ratingCustomTypeMpaa=" + ratingCustomTypeMpaa + ", ratingCustomTypeCaEnglish="
                + ratingCustomTypeCaEnglish + ", ratingCustomTypeCaFrench=" + ratingCustomTypeCaFrench
                + ", unratedLock=" + unratedLock + "]";
    }
}
