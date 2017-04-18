/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch.internal.items;

import java.util.Collection;

import org.openhab.binding.bosesoundtouch.internal.exceptions.NoInternetRadioPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.NoStoredMusicPresetFoundException;
import org.openhab.binding.bosesoundtouch.internal.exceptions.OperationModeNotAvailableException;
import org.openhab.binding.bosesoundtouch.types.OperationModeType;

/**
 * The {@link ContentItemMaker} class handles all nowPlaying Channels
 *
 * @author Thomas Traunbauer
 */
public class ContentItemMaker {

    private SoundTouchType soundTouchType;
    PresetContainer presetContainer;

    public ContentItemMaker(SoundTouchType soundTouchType, PresetContainer presetContainer) {
        this.soundTouchType = soundTouchType;
        this.presetContainer = presetContainer;
    }

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
            case HDMI:
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
        if ((soundTouchType == SoundTouchType.SoundTouch_10_WirelessSpeaker)
                || (soundTouchType == SoundTouchType.SoundTouch_20_WirelessSpeaker)
                || (soundTouchType == SoundTouchType.SoundTouch_30_WirelessSpeaker)
                || (soundTouchType == SoundTouchType.WaveSoundTouchMusicSystemIV)
                || (soundTouchType == SoundTouchType.SoundTouch_WirelessLinkAdapter)) {
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
        if (soundTouchType == SoundTouchType.SoundTouch_SA5Amplifier) {
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
        if (soundTouchType == SoundTouchType.SoundTouch_SA5Amplifier) {
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
        if (soundTouchType == SoundTouchType.SoundTouch_SA5Amplifier) {
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
        ContentItem contentItem = new ContentItem();
        contentItem.setSource("BLUETOOTH");
        return contentItem;
    }

    private ContentItem getDeezer() throws OperationModeNotAvailableException {
        throw new OperationModeNotAvailableException();
    }

    private ContentItem getHDMI() throws OperationModeNotAvailableException {
        ContentItem contentItem = null;
        if (soundTouchType == SoundTouchType.SoundTouch_300_Soundbar) {
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
        Collection<ContentItem> listOfPresets = presetContainer.values();
        for (ContentItem iteratedItem : listOfPresets) {
            if ((contentItem == null) && (iteratedItem.getOperationMode() == OperationModeType.INTERNET_RADIO)) {
                contentItem = iteratedItem;
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
        // // This is just an example, must find a way to do this
        // // Maybe an similar solution as for the INTERNET_RADIO
        // // If the more than 6 PRESETS feature is available, this makes more sense
        // ContentItem contentItem = new ContentItem();
        // contentItem.setSource("STORED_MUSIC");
        // contentItem.setSourceAccount("00113216-107a-0011-7a10-7a1016321100/0");
        // contentItem.setUnusedField(0);
        // contentItem.setLocation("28$65445");
        // contentItem.setPresetable(true);
        // contentItem.setItemName("100 Jahre");

        ContentItem contentItem = null;
        Collection<ContentItem> listOfPresets = presetContainer.values();
        for (ContentItem iteratedItem : listOfPresets) {
            if ((contentItem == null) && (iteratedItem.getOperationMode() == OperationModeType.STORED_MUSIC)) {
                contentItem = iteratedItem;
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
        if (soundTouchType == SoundTouchType.SoundTouch_300_Soundbar) {
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