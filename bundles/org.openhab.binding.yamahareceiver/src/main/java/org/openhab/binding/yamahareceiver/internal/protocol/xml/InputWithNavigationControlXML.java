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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import java.io.IOException;

import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithNavigationControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
 * @author Tomasz Maruszak - Refactor
 */
public class InputWithNavigationControlXML extends AbstractInputControlXML implements InputWithNavigationControl {

    private final Logger logger = LoggerFactory.getLogger(InputWithNavigationControlXML.class);

    public static final int MAX_PER_PAGE = 8;
    private boolean useAlternativeBackToHomeCmd = false;

    private NavigationControlState state;
    private NavigationControlStateListener observer;

    /**
     * Create a NavigationControl object for altering menu positions and requesting current menu information.
     *
     * @param state We need the current navigation state, because most navigation commands are relative commands and we
     *            offer API with absolute values.
     * @param inputID The input ID like USB or NET_RADIO.
     * @param con The Yamaha communication object to send http requests.
     */
    public InputWithNavigationControlXML(NavigationControlState state, String inputID, AbstractConnection con,
            NavigationControlStateListener observer, DeviceInformationState deviceInformationState) {
        super(LoggerFactory.getLogger(InputWithNavigationControlXML.class), inputID, con, deviceInformationState);

        this.state = state;
        this.observer = observer;
    }

    /**
     * Sends a cursor command to Yamaha.
     *
     * @param command
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    private void navigateCursor(String command) throws IOException, ReceivedMessageParseException {
        comReference.get().send(wrInput("<List_Control><Cursor>" + command + "</Cursor></List_Control>"));
        update();
    }

    /**
     * Navigate back
     *
     * @throws Exception
     */
    @Override
    public void goBack() throws IOException, ReceivedMessageParseException {
        navigateCursor("Back");
    }

    /**
     * Navigate up
     *
     * @throws Exception
     */
    @Override
    public void goUp() throws IOException, ReceivedMessageParseException {
        navigateCursor("Up");
    }

    /**
     * Navigate down
     *
     * @throws Exception
     */
    @Override
    public void goDown() throws IOException, ReceivedMessageParseException {
        navigateCursor("Down");
    }

    /**
     * Navigate left. Not for all zones or functions available.
     *
     * @throws Exception
     */
    @Override
    public void goLeft() throws IOException, ReceivedMessageParseException {
        navigateCursor("Left");
    }

    /**
     * Navigate right. Not for all zones or functions available.
     *
     * @throws Exception
     */
    @Override
    public void goRight() throws IOException, ReceivedMessageParseException {
        navigateCursor("Right");
    }

    /**
     * Select current item. Not for all zones or functions available.
     *
     * @throws Exception
     */
    @Override
    public void selectCurrentItem() throws IOException, ReceivedMessageParseException {
        navigateCursor("Select");
    }

    /**
     * Navigate to root menu
     *
     * @throws Exception
     */
    @Override
    public boolean goToRoot() throws IOException, ReceivedMessageParseException {
        if (useAlternativeBackToHomeCmd) {
            navigateCursor("Return to Home");
            if (state.menuLayer > 0) {
                observer.navigationError("Both going back to root commands failed for your receiver!");
                return false;
            }
        } else {
            navigateCursor("Back to Home");
            if (state.menuLayer > 0) {
                observer.navigationError(
                        "The going back to root command failed for your receiver. Trying to use a different command.");
                useAlternativeBackToHomeCmd = true;
                return goToRoot();
            }
        }
        return true;
    }

    @Override
    public void goToPage(int page) throws IOException, ReceivedMessageParseException {
        int line = (page - 1) * 8 + 1;
        comReference.get().send(wrInput("<List_Control><Jump_Line>" + line + "</Jump_Line></List_Control>"));
        update();
    }

    @Override
    public void selectItemFullPath(String fullPath) throws IOException, ReceivedMessageParseException {
        update();

        if (state.menuName == null) {
            return;
        }

        String[] pathArr = fullPath.split("/");

        // Just a relative menu item.
        if (pathArr.length < 2) {
            if (!selectItem(pathArr[0])) {
                observer.navigationError("Item '" + pathArr[0] + "' doesn't exist in menu " + state.menuName);
            }
            return;
        }

        // Full path info not available, so guess from last path element and number of path elements
        String selectMenuName = pathArr[pathArr.length - 2];
        String selectItemName = pathArr[pathArr.length - 1];
        int selectMenuLevel = pathArr.length - 1;

        boolean sameMenu = state.menuName.equals(selectMenuName) && state.menuLayer == selectMenuLevel;

        if (sameMenu) {
            if (!selectItem(selectItemName)) {
                observer.navigationError("Item '" + selectItemName + "' doesn't exist in menu " + state.menuName
                        + " at level " + String.valueOf(state.menuLayer) + ". Available options are: "
                        + state.getAllItemLabels());
            }
            return;
        }

        if (state.menuLayer > 0) {
            if (!goToRoot()) {
                return;
            }
        }

        for (String pathElement : pathArr) {
            if (!selectItem(pathElement)) {
                observer.navigationError("Item '" + pathElement + "' doesn't exist in menu " + state.menuName
                        + " at level " + String.valueOf(state.menuLayer) + ". Available options are: "
                        + state.getAllItemLabels());
                return;
            }
        }
    }

    /**
     * Finds an item on the current page. A page contains up to 8 items.
     * Operates on a cached XML node! Call refreshMenuState for up-to-date information.
     *
     * @return Return the item index [1,8] or -1 if not found.
     */
    private int findItemOnCurrentPage(String itemName) {
        for (int i = 0; i < MAX_PER_PAGE; i++) {
            if (itemName.equals(state.items[i])) {
                return i + 1;
            }
        }

        return -1;
    }

    private boolean selectItem(String name) throws IOException, ReceivedMessageParseException {
        final int pageCount = (int) Math.ceil(state.maxLine / (double) MAX_PER_PAGE);
        final int currentPage = (int) Math.floor((state.currentLine - 1) / (double) MAX_PER_PAGE);

        AbstractConnection com = comReference.get();
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            // Start with the current page and then go to the end and start at page 1 again
            int realPage = (currentPage + pageIndex) % pageCount;

            if (currentPage != realPage) {
                goToPage(pageIndex);
            }

            int index = findItemOnCurrentPage(name);
            if (index > 0) {
                com.send(wrInput(
                        "<List_Control><Direct_Sel>Line_" + String.valueOf(index) + "</Direct_Sel></List_Control>"));
                update();
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
    @Override
    public void update() throws IOException, ReceivedMessageParseException {
        int totalWaitingTime = 0;

        Document doc;
        Node currentMenu;

        AbstractConnection com = comReference.get();
        while (true) {
            String response = com.sendReceive(wrInput("<List_Info>GetParam</List_Info>"));
            doc = XMLUtils.xml(response);
            if (doc.getFirstChild() == null) {
                throw new ReceivedMessageParseException("<List_Info>GetParam failed: " + response);
            }

            currentMenu = XMLUtils.getNodeOrFail(doc.getFirstChild(), "List_Info");

            Node nodeMenuState = XMLUtils.getNode(currentMenu, "Menu_Status");
            if (nodeMenuState == null || "Ready".equals(nodeMenuState.getTextContent())) {
                break;
            }

            totalWaitingTime += YamahaReceiverBindingConstants.MENU_RETRY_DELAY;
            if (totalWaitingTime > YamahaReceiverBindingConstants.MENU_MAX_WAITING_TIME) {
                logger.info("Menu still not ready after " + YamahaReceiverBindingConstants.MENU_MAX_WAITING_TIME
                        + "ms. The menu state will be out of sync.");
                // ToDo: this needs to redesigned to allow for some sort of async update
                // Note: there is not really that much we can do here.
                return;
            }

            try {
                Thread.sleep(YamahaReceiverBindingConstants.MENU_RETRY_DELAY);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }

        state.clearItems();

        Node node = XMLUtils.getNodeOrFail(currentMenu, "Menu_Name");
        state.menuName = node.getTextContent();

        node = XMLUtils.getNodeOrFail(currentMenu, "Menu_Layer");
        state.menuLayer = Integer.parseInt(node.getTextContent()) - 1;

        node = XMLUtils.getNodeOrFail(currentMenu, "Cursor_Position/Current_Line");
        int currentLine = Integer.parseInt(node.getTextContent());
        state.currentLine = currentLine;

        node = XMLUtils.getNodeOrFail(currentMenu, "Cursor_Position/Max_Line");
        int maxLines = Integer.parseInt(node.getTextContent());
        state.maxLine = maxLines;

        for (int i = 1; i < 8; ++i) {
            state.items[i - 1] = XMLUtils.getNodeContentOrDefault(currentMenu, "Current_List/Line_" + i + "/Txt",
                    (String) null);
        }

        if (observer != null) {
            observer.navigationUpdated(state);
        }
    }
}
