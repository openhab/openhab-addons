/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.xmppclient.internal.client;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.jivesoftware.smack.xml.XmlPullParserException;
import org.jxmpp.JxmppContext;

/**
 * XEP-0066: Out of Band Data.
 */
@NonNullByDefault
public final class OobExtension implements ExtensionElement {

    public static final String ELEMENT = "x";
    public static final String NAMESPACE = "jabber:x:oob";
    public static final QName QNAME = new QName(NAMESPACE, ELEMENT);

    private final String url;
    private final @Nullable String desc;

    public OobExtension(String url, @Nullable String desc) {
        if (url.isBlank()) {
            throw new IllegalArgumentException("OOB URL must not be empty");
        }

        this.url = url;
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public @Nullable String getDesc() {
        return desc;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public CharSequence toXML(@Nullable XmlEnvironment xmlEnvironment) {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.rightAngleBracket();
        xml.element("url", url);

        if (desc != null && !desc.isBlank()) {
            xml.element("desc", desc);
        }

        xml.closeElement(this);
        return xml;
    }

    public static final class Provider extends ExtensionElementProvider<OobExtension> {

        @Override
        public OobExtension parse(@Nullable XmlPullParser parser, int initialDepth,
                @Nullable XmlEnvironment xmlEnvironment, @Nullable JxmppContext jxmppContext)
                throws IOException, XmlPullParserException {
            if (parser == null) {
                throw new IOException("XML parser must not be null");
            }

            String url = null;
            @Nullable
            String desc = null;

            while (true) {
                XmlPullParser.Event event = parser.next();

                if (event == XmlPullParser.Event.START_ELEMENT) {
                    switch (parser.getName()) {
                        case "url":
                            url = parser.nextText();
                            break;
                        case "desc":
                            desc = parser.nextText();
                            break;
                        default:
                            break;
                    }
                } else if (event == XmlPullParser.Event.END_ELEMENT && parser.getDepth() == initialDepth) {
                    break;
                }
            }

            if (url == null || url.isBlank()) {
                throw new IOException("OOB extension is missing its URL");
            }

            return new OobExtension(url, desc);
        }
    }
}
