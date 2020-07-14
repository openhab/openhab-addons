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

package org.openhab.binding.tr064.internal.dto.phonebook;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the org.openhab.binding.tr064.internal.dto.phonebook package.
 * <p>
 * An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups. Factory methods for each of these are
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Phonebooks_QNAME = new QName("", "phonebooks");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * org.openhab.binding.tr064.internal.dto.phonebook
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PhonebooksType }
     * 
     */
    public PhonebooksType createPhonebooksType() {
        return new PhonebooksType();
    }

    /**
     * Create an instance of {@link PersonType }
     * 
     */
    public PersonType createPersonType() {
        return new PersonType();
    }

    /**
     * Create an instance of {@link NumberType }
     * 
     */
    public NumberType createNumberType() {
        return new NumberType();
    }

    /**
     * Create an instance of {@link TelephonyType }
     * 
     */
    public TelephonyType createTelephonyType() {
        return new TelephonyType();
    }

    /**
     * Create an instance of {@link ContactType }
     * 
     */
    public ContactType createContactType() {
        return new ContactType();
    }

    /**
     * Create an instance of {@link PhonebookType }
     * 
     */
    public PhonebookType createPhonebookType() {
        return new PhonebookType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PhonebooksType }{@code >}
     * 
     * @param value
     *            Java instance representing xml element's value.
     * @return
     *         the new instance of {@link JAXBElement }{@code <}{@link PhonebooksType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "phonebooks")
    public JAXBElement<PhonebooksType> createPhonebooks(PhonebooksType value) {
        return new JAXBElement<PhonebooksType>(_Phonebooks_QNAME, PhonebooksType.class, null, value);
    }
}
