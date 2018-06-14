/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xmltv.internal.jaxb;

import javax.xml.bind.annotation.XmlRegistry;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.openhab.binding.xmltv.internal.jaxb package.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@XmlRegistry
@NonNullByDefault
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * org.openhab.binding.xmltv.internal.jaxb
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Tv }
     *
     */
    public Tv createTv() {
        return new Tv();
    }

    /**
     * Create an instance of {@link Programme }
     *
     */
    public Programme createProgramme() {
        return new Programme();
    }

    /**
     * Create an instance of {@link MediaChannel }
     *
     */
    public MediaChannel createChannel() {
        return new MediaChannel();
    }

    /**
     * Create an instance of {@link Icon }
     *
     */
    public Icon createIcon() {
        return new Icon();
    }

    /**
     * Create an instance of {@link WithLangType }
     *
     */
    public WithLangType createWithLangType() {
        return new WithLangType();
    }
}
