/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal;

import java.util.Collection;

import org.openhab.binding.bosesoundtouch.internal.exceptions.NoInternetRadioPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoStoredMusicPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.OperationModeNotAvailableException;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;

/**
 * The {@link ContentItemMaker} class makes ContentItems for sources
 *
 * @author Thomas Traunbauer
 */
public class ContentItemMaker {

    private PresetContainer presetContainer;
    private CommandExecutor commandExecutor;

    /**
     * Creates a new instance of this class
     */
    public ContentItemMaker(CommandExecutor commandExecutor, PresetContainer presetContainer) {
        this.commandExecutor = commandExecutor;
        this.presetContainer = presetContainer;
    }

    /**
     * Returns a valid ContentItem, to switch to
     *
     * @param operationModeType
     *
     * @throws OperationModeNotAvailableException if OperationMode is not supported yet or on this device
     * @throws NoInternetRadioPresetFoundException if OperationMode is INTERNET_RADIO and no PRESET is defined
     * @throws NoStoredMusicPresetFoundException if OperationMode is STORED_MUSIC and no PRESET is defined
     */
    public ContentItem getContentItem(OperationModeType operationModeType) throws OperationModeNotAvailableException,
            NoInternetRadioPresetFoundException, NoStoredMusicPresetFoundException {
        switch (operationModeType) {
            case OFFLINE:
            case OTHER:
            case STANDBY:
                throw new OperationModeNotAvailableException();
            case AMAZON:
                return getAmazon();
            case AUX:
                return getAUX();
            case AUX1:
                return getAUX1();
            case AUX2:
                return getAUX2();
            case AUX3:
                return getAUX3();
            case BLUETOOTH:
                return getBluetooth();
            case DEEZER:
                return getDeezer();
            case HDMI1:
                return getHDMI();
            case INTERNET_RADIO:
                return getInternetRadio();
            case PANDORA:
                return getPandora();
            case SIRIUSXM:
                return getSiriusxm();
            case SPOTIFY:
                return getSpotify();
            case STORED_MUSIC:
                return getStoredMusic();
            case TV:
                return getTV();

        }
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getAmazon() throws OperationModeNotAvailableException {
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getAUX() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isAUXAvailable()) {
            contentItem = new ContentItem();
            contentItem.setSource("AUX");
            contentItem.setSourceAccount("AUX");
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

    private ContentItem getAUX1() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isAUX1Available()) {
            contentItem = new ContentItem();
            contentItem.setSource("AUX");
            contentItem.setSourceAccount("AUX1");
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

    private ContentItem getAUX2() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isAUX2Available()) {
            contentItem = new ContentItem();
            contentItem.setSource("AUX");
            contentItem.setSourceAccount("AUX2");
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

    private ContentItem getAUX3() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isAUX3Available()) {
            contentItem = new ContentItem();
            contentItem.setSource("AUX");
            contentItem.setSourceAccount("AUX3");
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

    private ContentItem getBluetooth() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isBluetoothAvailable()) {
            contentItem = new ContentItem();
            contentItem.setSource("BLUETOOTH");
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

    private ContentItem getDeezer() throws OperationModeNotAvailableException {
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getHDMI() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isHDMI1Available()) {
            contentItem = new ContentItem();
            contentItem.setSource("PRODUCT");
            contentItem.setSourceAccount("HDMI_1");
            contentItem.setUnusedField(0);
            contentItem.setPresetable(false);
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

    private ContentItem getInternetRadio() throws NoInternetRadioPresetFoundException {
        ContentItem contentItem = null;
        if (commandExecutor.isInternetRadioAvailable()) {
            Collection<ContentItem> listOfPresets = presetContainer.getAllPresets();
            for (ContentItem iteratedItem : listOfPresets) {
                if ((contentItem == null) && (iteratedItem.getOperationMode() == OperationModeType.INTERNET_RADIO)) {
                    contentItem = iteratedItem;
                }
            }
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new NoInternetRadioPresetFoundException();
        }
    }

    private ContentItem getPandora() throws OperationModeNotAvailableException {
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getSiriusxm() throws OperationModeNotAvailableException {
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getSpotify() throws OperationModeNotAvailableException {
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getStoredMusic() throws NoStoredMusicPresetFoundException {
        ContentItem contentItem = null;
        if (commandExecutor.isStoredMusicAvailable()) {
            Collection<ContentItem> listOfPresets = presetContainer.getAllPresets();
            for (ContentItem iteratedItem : listOfPresets) {
                if ((contentItem == null) && (iteratedItem.getOperationMode() == OperationModeType.STORED_MUSIC)) {
                    contentItem = iteratedItem;
                }
            }
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new NoStoredMusicPresetFoundException();
        }
    }

    private ContentItem getTV() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (commandExecutor.isTVAvailable()) {
            contentItem = new ContentItem();
            contentItem.setSource("PRODUCT");
            contentItem.setSourceAccount("TV");
            contentItem.setUnusedField(0);
            contentItem.setPresetable(false);
        }
        if (contentItem != null) {
            return contentItem;
        } else {
            throw new OperationModeNotAvailableException();
        }
    }

}