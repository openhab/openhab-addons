package org.openhab.binding.onkyo.internal;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle Onkyo Album Arts.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class OnkyoAlbumArt {

    private Logger logger = LoggerFactory.getLogger(OnkyoAlbumArt.class);

    private enum State {
        INVALID,
        NOTSTARTED,
        STARTED,
        NEXT,
        READY;
    }

    private StringBuilder albumArtStringBuilder = new StringBuilder();
    private String imageType = "unknown";
    private State state = State.NOTSTARTED;

    public String getImageType() {
        return imageType;
    }

    public boolean isAlbumCoverTransferStarted() {
        return state == State.STARTED;
    }

    public boolean isAlbumCoverReady() {
        return state == State.READY;
    }

    public void clearAlbumArt() {
        albumArtStringBuilder.setLength(0);
        imageType = "unknown";
        state = State.NOTSTARTED;
    }

    public void addFrame(String data) {

        if (data.length() <= 2) {
            return;
        }

        char imgType = data.charAt(0);

        switch (imgType) {
            case '0':
                imageType = "bmp";
                break;
            case '1':
                imageType = "jpeg";
                break;
            default:
                imageType = "unknown";
        }

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
            default:
                state = State.INVALID;
                logger.debug("Unknown album art packet flag '{}'", packetFlag);
        }

        if (state != State.INVALID) {
            String picData = data.substring(2, data.length());
            logger.debug("Received album art fragment in '{}' format, packet flag '{}', picData '{}'", imageType,
                    packetFlagStr, picData);
            albumArtStringBuilder.append(picData);
        } else {
            logger.debug("Received album art fragment in wrong order, format '{}', packet flag '{}'", imageType,
                    packetFlagStr);
        }
    }

    public byte[] getAlbumArt() throws IllegalArgumentException {
        if (state == State.READY) {
            if (albumArtStringBuilder.length() > 2) {
                return DatatypeConverter.parseHexBinary(albumArtStringBuilder.toString());
            }
        }

        throw new IllegalArgumentException("Illegal Album Art");
    }
}
