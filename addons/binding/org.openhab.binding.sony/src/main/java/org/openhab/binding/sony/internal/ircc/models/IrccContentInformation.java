/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccContentInformation.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccContentInformation {

    /** The Constant TITLE. */
    public final static String TITLE = "title"; // browser title or video title

    /** The Constant CLASS. */
    public final static String CLASS = "class"; // "video"

    /** The Constant SOURCE. */
    public final static String SOURCE = "source"; // "DVD"

    /** The Constant MEDIATYPE. */
    public final static String MEDIATYPE = "mediaType"; // "DVD"

    /** The Constant MEDIAFORMAT. */
    public final static String MEDIAFORMAT = "mediaFormat"; // "VIDEO"

    /** The Constant ID. */
    public final static String ID = "id"; // 3CD3N19Q253851813V98704329773844B92D3340A18D15901A2AP7 - matches status

    /** The Constant EDITION. */
    public final static String EDITION = "edition"; // no example

    /** The Constant DESCRIPTION. */
    public final static String DESCRIPTION = "description"; // description of dvd

    /** The Constant GENRE. */
    public final static String GENRE = "genre"; // Action/Adventure

    /** The Constant DURATION. */
    public final static String DURATION = "duration"; // 6160

    /** The Constant RATING. */
    public final static String RATING = "rating"; // G

    /** The Constant DATERELEASE. */
    public final static String DATERELEASE = "dateRelease"; // 2011-011-01

    /** The Constant DIRECTOR. */
    public final static String DIRECTOR = "director"; // Ack - can be multiple ones

    /** The Constant PRODUCER. */
    public final static String PRODUCER = "producer"; // Ack - can be multiple ones

    /** The Constant SCREENWRITER. */
    public final static String SCREENWRITER = "screenWriter"; // Ack - can be multiple ones

    /** The Constant ICONDATA. */
    public final static String ICONDATA = "iconData"; // Base 64

    /** The info items. */
    private Map<String, List<IrccInfoItem>> infoItems = new HashMap<String, List<IrccInfoItem>>();

    /**
     * Instantiates a new ircc content information.
     *
     * @param xml the xml
     */
    public IrccContentInformation(Element xml) {
        final NodeList contents = xml.getElementsByTagName("infoItem");
        for (int i = contents.getLength() - 1; i >= 0; i--) {
            final Node infoItem = contents.item(i);
            final IrccInfoItem item = new IrccInfoItem((Element) infoItem);

            List<IrccInfoItem> items = infoItems.get(item.getName());
            if (items == null) {
                items = new ArrayList<IrccInfoItem>();
                infoItems.put(item.getName(), items);
            }

            items.add(item);
        }
    }

    /**
     * Gets the info item.
     *
     * @param name the name
     * @return the info item
     */
    public List<IrccInfoItem> getInfoItem(String name) {
        return infoItems.get(name);
    }

    /**
     * Gets the info item value.
     *
     * @param name the name
     * @return the info item value
     */
    public String getInfoItemValue(String name) {
        final List<IrccInfoItem> items = getInfoItem(name);
        if (items == null) {
            return null;
        }
        final StringBuilder b = new StringBuilder();
        for (IrccInfoItem i : items) {
            b.append(i.getValue());
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }
}
