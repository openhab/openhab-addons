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
package org.openhab.binding.pixometer.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Annotation} class is the representing java model for the json result for a annotation from the pixometer
 * api
 *
 * @author Jerome Luckenbach - Initial Contribution
 *
 */
@NonNullByDefault
public class Annotation {

    private @NonNullByDefault({}) Integer id;
    private @NonNullByDefault({}) Object rectangle;
    private @NonNullByDefault({}) String meaning;
    private @NonNullByDefault({}) String text;
    private @NonNullByDefault({}) Integer image;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getRectangle() {
        return rectangle;
    }

    public void setRectangle(Object rectangle) {
        this.rectangle = rectangle;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
        this.image = image;
    }
}
