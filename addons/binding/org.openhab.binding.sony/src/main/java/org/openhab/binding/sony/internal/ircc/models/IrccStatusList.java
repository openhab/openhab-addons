/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.ircc.models;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// TODO: Auto-generated Javadoc
/**
 * The Class IrccStatusList.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class IrccStatusList {

    /** The Constant ST_TEXTINPUT. */
    public static final String ST_TEXTINPUT = "textInput";

    /** The Constant ST_DISC. */
    public static final String ST_DISC = "disc";

    /** The Constant ST_WEBBROWSER. */
    public static final String ST_WEBBROWSER = "webBrowse";

    /** The Constant ST_CURSORDISPLAY. */
    public static final String ST_CURSORDISPLAY = "cursorDisplay";

    /** The Constant ST_VIEWING. */
    public static final String ST_VIEWING = "viewing";

    /** The statuses. */
    private final Map<String, IrccStatus> statuses = new HashMap<String, IrccStatus>();

    /**
     * Instantiates a new ircc status list.
     *
     * @param statusXml the status xml
     */
    public IrccStatusList(Document statusXml) {
        final NodeList statusList = statusXml.getElementsByTagName("status");
        for (int i = statusList.getLength() - 1; i >= 0; i--) {
            final Element status = (Element) statusList.item(i);
            final IrccStatus irccStatus = new IrccStatus(status);
            statuses.put(irccStatus.getName(), irccStatus);
        }
    }

    /**
     * Checks if is text input.
     *
     * @return true, if is text input
     */
    public boolean isTextInput() {
        return statuses.containsKey(ST_TEXTINPUT);
    }

    /**
     * Checks if is web browse.
     *
     * @return true, if is web browse
     */
    public boolean isWebBrowse() {
        return statuses.containsKey(ST_WEBBROWSER);
    }

    /**
     * Gets the viewing.
     *
     * @return the viewing
     */
    public IrccStatus getViewing() {
        return statuses.get(ST_VIEWING);
    }

    /**
     * The Class IrccStatus.
     */
    public class IrccStatus {

        /** The Constant CLASS. */
        // following are for ST_VIEWING
        public static final String CLASS = "class";

        /** The Constant ID. */
        public static final String ID = "id";

        /** The Constant TITLE. */
        public static final String TITLE = "title";

        /** The Constant SOURCE. */
        public static final String SOURCE = "source";

        /** The Constant SOURCE2. */
        public static final String SOURCE2 = "zone2Source";

        /** The Constant DURATION. */
        public static final String DURATION = "duration";

        /** The Constant TYPE. */
        // following are for ST_DISC
        public static final String TYPE = "type";

        /** The Constant MEDIATYPE. */
        public static final String MEDIATYPE = "mediatype";

        /** The Constant MEDIAFORMAT. */
        public static final String MEDIAFORMAT = "mediaformat";

        /** The name. */
        private final String name;

        /** The items. */
        private Map<String, IrccStatusItem> items = new HashMap<String, IrccStatusItem>();

        /**
         * Instantiates a new ircc status.
         *
         * @param status the status
         */
        public IrccStatus(Element status) {
            name = status.getAttribute("name");
            final NodeList itemList = status.getElementsByTagName("statusItem");
            for (int i = itemList.getLength() - 1; i >= 0; i--) {
                final IrccStatusItem item = new IrccStatusItem((Element) itemList.item(i));
                items.put(item.getField(), item);
            }
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
         * Gets the item.
         *
         * @param name the name
         * @return the item
         */
        public IrccStatusItem getItem(String name) {
            return items.get(name);
        }

        /**
         * Gets the item value.
         *
         * @param name the name
         * @return the item value
         */
        public String getItemValue(String name) {
            final IrccStatusItem item = getItem(name);
            return item == null ? null : item.getValue();
        }
    }

    /**
     * The Class IrccStatusItem.
     */
    public class IrccStatusItem {

        /** The field. */
        private final String field;

        /** The value. */
        private final String value;

        /**
         * Instantiates a new ircc status item.
         *
         * @param statusItemXml the status item xml
         */
        public IrccStatusItem(Element statusItemXml) {
            field = statusItemXml.getAttribute("field");
            value = statusItemXml.getAttribute("value");
        }

        /**
         * Gets the field.
         *
         * @return the field
         */
        public String getField() {
            return field;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public String getValue() {
            return value;
        }
    }
}
