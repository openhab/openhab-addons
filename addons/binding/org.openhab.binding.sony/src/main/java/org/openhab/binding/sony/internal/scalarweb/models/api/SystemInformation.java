/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemInformation.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class SystemInformation {

    /** The product. */
    private final String product;

    /** The region. */
    private final String region;

    /** The language. */
    private final String language;

    /** The model. */
    private final String model;

    /** The serial. */
    private final String serial;

    /** The mac addr. */
    private final String macAddr;

    /** The name. */
    private final String name;

    /** The generation. */
    private final String generation;

    /** The area. */
    private final String area;

    /** The cid. */
    private final String cid;

    /**
     * Instantiates a new system information.
     *
     * @param product the product
     * @param region the region
     * @param language the language
     * @param model the model
     * @param serial the serial
     * @param macAddr the mac addr
     * @param name the name
     * @param generation the generation
     * @param area the area
     * @param cid the cid
     */
    public SystemInformation(String product, String region, String language, String model, String serial,
            String macAddr, String name, String generation, String area, String cid) {
        super();
        this.product = product;
        this.region = region;
        this.language = language;
        this.model = model;
        this.serial = serial;
        this.macAddr = macAddr;
        this.name = name;
        this.generation = generation;
        this.area = area;
        this.cid = cid;
    }

    /**
     * Gets the product.
     *
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * Gets the region.
     *
     * @return the region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public String getModel() {
        return model;
    }

    /**
     * Gets the serial.
     *
     * @return the serial
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Gets the mac addr.
     *
     * @return the mac addr
     */
    public String getMacAddr() {
        return macAddr;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the generation.
     *
     * @return the generation
     */
    public String getGeneration() {
        return generation;
    }

    /**
     * Gets the area.
     *
     * @return the area
     */
    public String getArea() {
        return area;
    }

    /**
     * Gets the cid.
     *
     * @return the cid
     */
    public String getCid() {
        return cid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SystemInformation [product=" + product + ", region=" + region + ", language=" + language + ", model="
                + model + ", serial=" + serial + ", macAddr=" + macAddr + ", name=" + name + ", generation="
                + generation + ", area=" + area + ", cid=" + cid + "]";
    }

}
