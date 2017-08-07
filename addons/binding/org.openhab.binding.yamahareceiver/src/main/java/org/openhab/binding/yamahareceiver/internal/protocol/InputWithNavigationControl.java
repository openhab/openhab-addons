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
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * The navigation control protocol interface
 *
 * @author David Graeff - Initial contribution
 */

public interface InputWithNavigationControl extends IStateUpdateable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    public static Set<String> supportedInputs = Sets.newHashSet("NET_RADIO", "USB", "DOCK", "iPOD_USB", "PC", "Napster",
            "Pandora", "SIRIUS", "Rhapsody", "iPod", "HD_RADIO");

    /**
     * Navigate back
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public void goBack() throws ReceivedMessageParseException, IOException;

    /**
     * Navigate up
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public void goUp() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate down
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public void goDown() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate left. Not for all zones or functions available.
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public void goLeft() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate right. Not for all zones or functions available.
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public void goRight() throws IOException, ReceivedMessageParseException;

    /**
     * Select current item. Not for all zones or functions available.
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public void selectCurrentItem() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate to root menu
     *
     * @throws ReceivedMessageParseException, IOException
     */
    public boolean goToRoot() throws IOException, ReceivedMessageParseException;

    /**
     * Navigate to the given page. The Yamaha protocol separates list of items into pages.
     *
     * @param page The page, starting with 1.
     *
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public void goToPage(int page) throws IOException, ReceivedMessageParseException;

    /**
     * Provide a full path to the menu and menu item
     *
     * @param fullPath
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    public void selectItemFullPath(String fullPath) throws IOException, ReceivedMessageParseException;
}
