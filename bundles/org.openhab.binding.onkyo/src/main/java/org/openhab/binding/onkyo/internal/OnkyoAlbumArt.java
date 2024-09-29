/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onkyo.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle Onkyo Album Arts.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Stewart Cossey - fixed bug in getAlbumArt function
 */
public class OnkyoAlbumArt {

    private final Logger logger = LoggerFactory.getLogger(OnkyoAlbumArt.class);

    private enum State {
        INVALID,
        NOTSTARTED,
        STARTED,
        NEXT,
        READY
    }

    private enum ImageType {
        BMP,
        JPEG,
        URL,
        NONE,
        UNKNOWN
    }

    private StringBuilder albumArtStringBuilder = new StringBuilder();
    private ImageType imageType = ImageType.UNKNOWN;
    private State state = State.NOTSTARTED;
    private String coverArtUrl;

    public boolean isAlbumCoverTransferStarted() {
        return state == State.STARTED;
    }

    public boolean isAlbumCoverReady() {
        return state == State.READY;
    }

    public void clearAlbumArt() {
        albumArtStringBuilder.setLength(0);
        imageType = ImageType.UNKNOWN;
        state = State.NOTSTARTED;
        coverArtUrl = null;
    }

    public void addFrame(String data) {
        if (data.length() <= 2) {
            return;
        }

        char imgType = data.charAt(0);
        imageType = getImageType(imgType);

        char packetFlag = data.charAt(1);
        String packetFlagStr = "unknown";

        switch (packetFlag) {
            case '0':
                if (state == State.NOTSTARTED || state == State.INVALID) {
                    state = State.STARTED;
                } else {
                    state = State.INVALID;
                }
                packetFlagStr = "Start";
                albumArtStringBuilder.setLength(0);
                break;
            case '1':
                packetFlagStr = "Next";
                if (state == State.STARTED || state == State.NEXT) {
                    state = State.NEXT;
                } else {
                    state = State.INVALID;
                }
                break;
            case '2':
                packetFlagStr = "End";
                if (state == State.STARTED || state == State.NEXT) {
                    state = State.READY;
                } else {
                    state = State.INVALID;
                }
                break;
            case '-':
                packetFlagStr = "notUsed";
                state = State.READY;
                break;
            default:
                state = State.INVALID;
                logger.debug("Unknown album art packet flag '{}'", packetFlag);
        }

        if (state != State.INVALID) {
            switch (imageType) {
                case BMP:
                case JPEG:
                    String picData = data.substring(2, data.length());
                    logger.debug("Received album art fragment in '{}' format, packet flag '{}', picData '{}'",
                            imageType, packetFlagStr, picData);
                    albumArtStringBuilder.append(picData);
                    break;
                case URL:
                    coverArtUrl = data.substring(2);
                    logger.debug("Received album art url '{}'", coverArtUrl);
                    break;
                case NONE:
                    logger.debug("Received information: album art not available");
                    break;
                default:
            }

        } else {
            logger.debug("Received album art fragment in wrong order, format '{}', packet flag '{}'", imageType,
                    packetFlagStr);
        }
    }

    public byte[] getAlbumArt() throws IllegalArgumentException {
        byte[] data = null;

        if (state == State.READY) {
            switch (imageType) {
                case BMP:
                case JPEG:
                    data = HexUtils.hexToBytes(albumArtStringBuilder.toString());
                    break;
                case URL:
                    data = downloadAlbumArt(coverArtUrl);
                    // Workaround firmware bug providing incorrect headers causing them to be seen as body instead.
                    if (data != null) {
                        int bodyLength = data.length;
                        int i = new String(data).indexOf("image/");
                        if (i > 0) {
                            while (i < bodyLength && (data[i] != '\r' && data[i] != '\n')) {
                                i++;
                            }
                            while (i < bodyLength && (data[i] == '\r' || data[i] == '\n')) {
                                i++;
                            }
                            data = Arrays.copyOfRange(data, i, bodyLength);
                            logger.trace("Onkyo fixed picture data @ {}: {} ", i, new String(data));
                        }
                    }
                    break;
                case NONE:
                default:
            }
            return data;
        }

        throw new IllegalArgumentException("Illegal Album Art");
    }

    private byte[] downloadAlbumArt(String albumArtUrl) {
        try {
            URL url = new URL(albumArtUrl);
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                return inputStream.readAllBytes();
            }
        } catch (MalformedURLException e) {
            logger.warn("Album Art download failed from url '{}', reason {}", albumArtUrl, e.getMessage());
        } catch (IOException e) {
            logger.warn("Album Art download failed from url '{}', reason {}", albumArtUrl, e.getMessage());
        }

        return null;
    }

    private ImageType getImageType(char imgType) {
        ImageType it = ImageType.UNKNOWN;
        switch (imgType) {
            case '0':
                it = ImageType.BMP;
                break;
            case '1':
                it = ImageType.JPEG;
                break;
            case '2':
                it = ImageType.URL;
                break;
            case 'n':
                it = ImageType.NONE;
                break;
            default:
                it = ImageType.UNKNOWN;
        }

        return it;
    }

    public @NonNull String getAlbumArtMimeType() {
        String mimeType = "";
        switch (imageType) {
            case BMP:
                mimeType = "image/bmp";
                break;
            case JPEG:
                mimeType = "image/jpeg";
                break;
            case UNKNOWN:
            case URL:
            case NONE:
            default:
                break;
        }
        return mimeType;
    }
}
