/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class ParentalRatingSetting.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class ParentalRatingSetting {

    /** The rating type age. */
    private final int ratingTypeAge;

    /** The rating type sony. */
    private final String ratingTypeSony;

    /** The rating country. */
    private final String ratingCountry;

    /** The rating custom type TV. */
    private final String[] ratingCustomTypeTV;

    /** The rating custom type mpaa. */
    private final String ratingCustomTypeMpaa;

    /** The rating custom type ca english. */
    private final String ratingCustomTypeCaEnglish;

    /** The rating custom type ca french. */
    private final String ratingCustomTypeCaFrench;

    /** The unrated lock. */
    private final boolean unratedLock;

    /** The field names. */
    private final Set<String> fieldNames;

    /**
     * Instantiates a new parental rating setting.
     *
     * @param ratingTypeAge the rating type age
     * @param ratingTypeSony the rating type sony
     * @param ratingCountry the rating country
     * @param ratingCustomTypeTV the rating custom type TV
     * @param ratingCustomTypeMpaa the rating custom type mpaa
     * @param ratingCustomTypeCaEnglish the rating custom type ca english
     * @param ratingCustomTypeCaFrench the rating custom type ca french
     * @param unratedLock the unrated lock
     */
    public ParentalRatingSetting(int ratingTypeAge, String ratingTypeSony, String ratingCountry,
            String[] ratingCustomTypeTV, String ratingCustomTypeMpaa, String ratingCustomTypeCaEnglish,
            String ratingCustomTypeCaFrench, boolean unratedLock) {
        super();
        this.ratingTypeAge = ratingTypeAge;
        this.ratingTypeSony = ratingTypeSony;
        this.ratingCountry = ratingCountry;
        this.ratingCustomTypeTV = ratingCustomTypeTV;
        this.ratingCustomTypeMpaa = ratingCustomTypeMpaa;
        this.ratingCustomTypeCaEnglish = ratingCustomTypeCaEnglish;
        this.ratingCustomTypeCaFrench = ratingCustomTypeCaFrench;
        this.unratedLock = unratedLock;
        this.fieldNames = new HashSet<String>();
    }

    /**
     * Checks for.
     *
     * @param fieldName the field name
     * @return true, if successful
     */
    private boolean has(String fieldName) {
        return fieldNames.size() == 0 || fieldNames.contains(fieldName);
    }

    /**
     * Gets the rating type age.
     *
     * @return the rating type age
     */
    public int getRatingTypeAge() {
        return ratingTypeAge;
    }

    /**
     * Checks for rating type age.
     *
     * @return true, if successful
     */
    public boolean hasRatingTypeAge() {
        return has("ratingTypeAge");
    }

    /**
     * Gets the rating type sony.
     *
     * @return the rating type sony
     */
    public String getRatingTypeSony() {
        return ratingTypeSony;
    }

    /**
     * Checks for rating type sony.
     *
     * @return true, if successful
     */
    public boolean hasRatingTypeSony() {
        return has("ratingTypeSony");
    }

    /**
     * Gets the rating country.
     *
     * @return the rating country
     */
    public String getRatingCountry() {
        return ratingCountry;
    }

    /**
     * Checks for rating country.
     *
     * @return true, if successful
     */
    public boolean hasRatingCountry() {
        return has("ratingCountry");
    }

    /**
     * Gets the rating custom type TV.
     *
     * @return the rating custom type TV
     */
    public String[] getRatingCustomTypeTV() {
        return ratingCustomTypeTV;
    }

    /**
     * Checks for rating custom type TV.
     *
     * @return true, if successful
     */
    public boolean hasRatingCustomTypeTV() {
        return has("ratingCustomTypeTV");
    }

    /**
     * Gets the rating custom type mpaa.
     *
     * @return the rating custom type mpaa
     */
    public String getRatingCustomTypeMpaa() {
        return ratingCustomTypeMpaa;
    }

    /**
     * Checks for rating custom type mpaa.
     *
     * @return true, if successful
     */
    public boolean hasRatingCustomTypeMpaa() {
        return has("ratingCustomTypeMpaa");
    }

    /**
     * Gets the rating custom type ca english.
     *
     * @return the rating custom type ca english
     */
    public String getRatingCustomTypeCaEnglish() {
        return ratingCustomTypeCaEnglish;
    }

    /**
     * Checks for rating custom type ca english.
     *
     * @return true, if successful
     */
    public boolean hasRatingCustomTypeCaEnglish() {
        return has("ratingCustomTypeCaEnglish");
    }

    /**
     * Gets the rating custom type ca french.
     *
     * @return the rating custom type ca french
     */
    public String getRatingCustomTypeCaFrench() {
        return ratingCustomTypeCaFrench;
    }

    /**
     * Checks for rating custom type ca french.
     *
     * @return true, if successful
     */
    public boolean hasRatingCustomTypeCaFrench() {
        return has("ratingCustomTypeCaFrench");
    }

    /**
     * Checks if is unrated lock.
     *
     * @return true, if is unrated lock
     */
    public boolean isUnratedLock() {
        return unratedLock;
    }

    /**
     * Checks for unrated lock.
     *
     * @return true, if successful
     */
    public boolean hasUnratedLock() {
        return has("unratedLock");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ParentalRatingSetting [ratingTypeAge=" + ratingTypeAge + ", ratingTypeSony=" + ratingTypeSony
                + ", ratingCountry=" + ratingCountry + ", ratingCustomTypeTV=" + Arrays.toString(ratingCustomTypeTV)
                + ", ratingCustomTypeMpaa=" + ratingCustomTypeMpaa + ", ratingCustomTypeCaEnglish="
                + ratingCustomTypeCaEnglish + ", ratingCustomTypeCaFrench=" + ratingCustomTypeCaFrench
                + ", unratedLock=" + unratedLock + "]";
    }
}
