/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

/**
 * This class implements the Yamaha Receiver protocol related to navigation functionally. USB, NET_RADIO, IPOD and
 * other inputs are using the same way of navigating through menus. A menu on Yamaha AVRs
 * is hierarchically organised. Entries are divided into pages with 8 elements per page.
 *
 * The XML nodes <List_Control> and <List_Info> are used.
 *
 * In contrast to other protocol classes an object of this type will store state information,
 * because it caches the received XML information of the updateNavigationState(). This may change
 * in the future.
 *
 * Example:
 *
 * NavigationControl menu = new NavigationControl("NET_RADIO", comObject);
 * menu.goToPath(menuDir);
 * menu.selectItem(stationName);
 *
 * @author David Graeff - Completely refactored class
 * @author Dennis Frommknecht - Initial idea and implementaton
 */
public class InputWithNavigationControl {
    protected final WeakReference<HttpXMLSendReceive> comReference;

    protected final String inputID;

    public static Set<String> supportedInputs = Sets.newHashSet("NET_RADIO", "USB", "DOCK", "iPOD_USB", "PC", "Napster",
            "Pandora", "SIRIUS", "Rhapsody", "iPod", "HD_RADIO");

    public static final int MAX_PER_PAGE = 8;
    private boolean useAlternativeBackToHomeCmd = false;

    /**
     * The current state of the navigation
     */
    public static class State {
        public String menuName = null;
        public int menuLayer = -1;
        public int currentLine = 0;
        public int maxLine = -1;
        public String items[] = new String[MAX_PER_PAGE];

        public String getCurrentItemName() {
            if (currentLine < 1 || currentLine > items.length) {
                return "";
            }
            return items[currentLine - 1];
        }

        public String getAllItemLabels() {
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                if (item != null && item.length() > 0) {
                    sb.append(item);
                    sb.append(',');
                }
            }
            return sb.toString();
        }

        void clearItems() {
            for (int i = 0; i < items.length; ++i) {
                items[i] = null;
            }
        }

        public void invalidate() {
            this.menuName = "N/A";
            this.maxLine = 0;
            this.currentLine = 0;
            this.menuLayer = 0;
        }
    }

    /// Navigation is cached
    private State cache = new State();

    public interface Listener {
        void navigationUpdated(State msg);

        void navigationError(String msg);
    }

    private Listener observer;

    /**
     * Create a NavigationControl object for altering menu positions and requesting current menu information.
     *
     * @param inputID The input ID like USB or NET_RADIO.
     * @param com The Yamaha communication object to send http requests.
     */
    public InputWithNavigationControl(String inputID, HttpXMLSendReceive com, Listener observer) {
        this.comReference = new WeakReference<HttpXMLSendReceive>(com);
        this.inputID = inputID;
        this.observer = observer;
    }

    /**
     * Wraps the XML message with the inputID tags. Example with inputID=NET_RADIO:
     * <NETRADIO>message</NETRADIO>.
     *
     * @param message XML message
     * @return
     */
    private String wrInput(String message) {
        return "<" + inputID + ">" + message + "</" + inputID + ">";
    }

    /**
     * Navigate back
     *
     * @throws Exception
     */
    public void goBack() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<List_Control><Cursor>Back</Cursor></List_Control>"));
        updateNavigationState();
    }

    /**
     * Navigate up
     *
     * @throws Exception
     */
    public void goUp() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<List_Control><Cursor>Up</Cursor></List_Control>"));
        updateNavigationState();
    }

    /**
     * Navigate down
     *
     * @throws Exception
     */
    public void goDown() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<List_Control><Cursor>Down</Cursor></List_Control>"));
        updateNavigationState();
    }

    /**
     * Navigate left. Not for all zones or functions available.
     *
     * @throws Exception
     */
    public void goLeft() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<List_Control><Cursor>Left</Cursor></List_Control>"));
        updateNavigationState();
    }

    /**
     * Navigate right. Not for all zones or functions available.
     *
     * @throws Exception
     */
    public void goRight() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<List_Control><Cursor>Right</Cursor></List_Control>"));
        updateNavigationState();
    }

    /**
     * Select current item. Not for all zones or functions available.
     *
     * @throws Exception
     */
    public void selectCurrentItem() throws IOException, ParserConfigurationException, SAXException {
        comReference.get().postPut(wrInput("<List_Control><Cursor>Select</Cursor></List_Control>"));
        updateNavigationState();
    }

    /**
     * Navigate to root menu
     *
     * @throws Exception
     */
    public boolean goToRoot() throws IOException, ParserConfigurationException, SAXException {
        if (useAlternativeBackToHomeCmd) {
            comReference.get().postPut(wrInput("<List_Control><Cursor>Return to Home</Cursor></List_Control>"));
            updateNavigationState();
            if (getLevel() > 0) {
                observer.navigationError("Both going back to root commands failed for your receiver!");
                return false;
            }
        } else {
            comReference.get().postPut(wrInput("<List_Control><Cursor>Back to Home</Cursor></List_Control>"));
            updateNavigationState();
            if (getLevel() > 0) {
                observer.navigationError(
                        "The going back to root command failed for your receiver. Trying to use a different command.");
                useAlternativeBackToHomeCmd = true;
                return goToRoot();
            }
        }
        return true;
    }

    public void goToPage(int page) throws IOException, ParserConfigurationException, SAXException {
        int line = (page - 1) * 8 + 1;
        comReference.get().postPut(wrInput("<List_Control><Jump_Line>" + line + "</Jump_Line></List_Control>"));
        updateNavigationState();
    }

    public void selectItemFullPath(String fullPath) throws IOException, ParserConfigurationException, SAXException {
        updateNavigationState();

        if (getMenuName() == null) {
            return;
        }

        String[] pathArr = fullPath.split("/");

        // Just a relative menu item.
        if (pathArr.length < 2) {
            if (!selectItem(pathArr[0])) {
                observer.navigationError("Item '" + pathArr[0] + "' doesn't exist in menu " + getMenuName());
            }
            return;
        }

        // Full path info not available, so guess from last path element and number of path elements
        String selectMenuName = pathArr[pathArr.length - 2];
        String selectItemName = pathArr[pathArr.length - 1];
        int selectMenuLevel = pathArr.length - 1;

        boolean sameMenu = getMenuName().equals(selectMenuName) && getLevel() == selectMenuLevel;

        if (sameMenu) {
            if (!selectItem(selectItemName)) {
                observer.navigationError("Item '" + selectItemName + "' doesn't exist in menu " + getMenuName()
                        + " at level " + String.valueOf(cache.menuLayer) + ". Available options are: "
                        + cache.getAllItemLabels());
            }
            return;
        }

        if (getLevel() > 0) {
            if (!goToRoot()) {
                return;
            }
        }

        for (String pathElement : pathArr) {
            if (!selectItem(pathElement)) {
                observer.navigationError("Item '" + pathElement + "' doesn't exist in menu " + getMenuName()
                        + " at level " + String.valueOf(cache.menuLayer) + ". Available options are: "
                        + cache.getAllItemLabels());
                return;
            }
        }
    }

    /**
     * Get the menu name.
     * Operates on a cached XML node! Call refreshMenuState for up-to-date information.
     *
     * @return The menu name
     */
    public String getMenuName() {
        return cache.menuName;
    }

    /**
     * Get the menu level.
     * Operates on a cached XML node! Call refreshMenuState for up-to-date information.
     *
     * @return The menu level. -1 if unknown. 0 equals root menu.
     */
    public int getLevel() {
        return cache.menuLayer;
    }

    /**
     * Get the page number.
     * Operates on a cached XML node! Call refreshMenuState for up-to-date information.
     *
     * @return The page number. Each page contains 8 items.
     */
    public int getCurrentItemNumber() {
        return cache.currentLine;
    }

    /**
     * Get the page numbers.
     * Operates on a cached XML node! Call refreshMenuState for up-to-date information.
     *
     * @return The page numbers. Each page contains 8 items.
     */
    public int getNumberOfItems() {
        return cache.maxLine;
    }

    /**
     * Finds an item on the current page. A page contains up to 8 items.
     * Operates on a cached XML node! Call refreshMenuState for up-to-date information.
     *
     * @return Return the item index [1,8] or -1 if not found.
     */
    private int findItemOnCurrentPage(String itemName) {
        for (int i = 0; i < MAX_PER_PAGE; i++) {
            if (itemName.equals(cache.items[i])) {
                return i + 1;
            }
        }

        return -1;
    }

    private boolean selectItem(String name) throws IOException, ParserConfigurationException, SAXException {
        final int pageCount = (int) Math.ceil(cache.maxLine / (double) MAX_PER_PAGE);
        final int currentPage = (int) Math.floor((cache.currentLine - 1) / (double) MAX_PER_PAGE);

        HttpXMLSendReceive com = comReference.get();
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            // Start with the current page and then go to the end and start at page 1 again
            int realPage = (currentPage + pageIndex) % pageCount;

            if (currentPage != realPage) {
                goToPage(pageIndex);
            }

            int index = findItemOnCurrentPage(name);
            if (index > 0) {
                com.postPut(wrInput(
                        "<List_Control><Direct_Sel>Line_" + String.valueOf(index) + "</Direct_Sel></List_Control>"));
                updateNavigationState();
                return true;
            }
        }

        return false;
    }

    /**
     * Refreshes the menu state and caches the List_Info node from the response. This method may take
     * some time because it retries the request for up to MENU_MAX_WAITING_TIME or the menu state reports
     * "Ready", whatever comes first.
     *
     * @throws Exception
     */
    public void updateNavigationState() throws IOException, ParserConfigurationException, SAXException {
        int totalWaitingTime = 0;

        Document doc;
        Node currentMenu;

        HttpXMLSendReceive com = comReference.get();
        while (true) {
            String response = com.post(wrInput("<List_Info>GetParam</List_Info>"));
            doc = com.xml(response);
            if (doc.getFirstChild() == null) {
                throw new SAXException("<Play_Control>GetParam failed: " + response);
            }

            currentMenu = HttpXMLSendReceive.getNode(doc.getFirstChild(), "List_Info");

            if (currentMenu == null) {
                throw new SAXException("<List_Info>: GetParam response invalid!");
            }

            Node nodeMenuState = HttpXMLSendReceive.getNode(currentMenu, "Menu_Status");

            if (nodeMenuState == null || nodeMenuState.getTextContent().equals("Ready")) {
                break;
            }

            totalWaitingTime += ZoneControl.MENU_RETRY_DELAY;
            if (totalWaitingTime > ZoneControl.MENU_MAX_WAITING_TIME) {
                throw new IOException("Menu still not ready after " + ZoneControl.MENU_MAX_WAITING_TIME + "ms");
            }

            try {
                Thread.sleep(ZoneControl.MENU_RETRY_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        cache.clearItems();

        Node node = HttpXMLSendReceive.getNode(currentMenu, "Menu_Name");
        if (node == null) {
            throw new SAXException("Menu_Name child in parent node missing! ");
        }

        cache.menuName = node.getTextContent();

        node = HttpXMLSendReceive.getNode(currentMenu, "Menu_Layer");
        if (node == null) {
            throw new SAXException("Menu_Layer child in parent node missing!");
        }

        cache.menuLayer = Integer.parseInt(node.getTextContent()) - 1;

        node = HttpXMLSendReceive.getNode(currentMenu, "Cursor_Position/Current_Line");
        if (node == null) {
            throw new SAXException("Cursor_Position/Current_Line child in parent node missing!");
        }

        int currentLine = Integer.parseInt(node.getTextContent());
        cache.currentLine = currentLine;

        node = HttpXMLSendReceive.getNode(currentMenu, "Cursor_Position/Max_Line");
        if (node == null) {
            throw new SAXException("Cursor_Position/Max_Line child in parent node missing!");
        }

        int maxLines = Integer.parseInt(node.getTextContent());
        cache.maxLine = maxLines;

        for (int i = 1; i < 8; ++i) {
            node = HttpXMLSendReceive.getNode(currentMenu, "Current_List/Line_" + i + "/Txt");
            cache.items[i - 1] = node != null ? node.getTextContent() : null;
        }

        if (observer != null) {
            observer.navigationUpdated(cache);
        }
    }
}
