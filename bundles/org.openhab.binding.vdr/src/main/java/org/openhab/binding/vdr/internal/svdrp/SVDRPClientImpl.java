/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vdr.internal.svdrp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SVDRPClientImpl} encapsulates all calls to the SVDRP interface of a VDR
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPClientImpl implements SVDRPClient {

    private final Logger logger = LoggerFactory.getLogger(SVDRPClientImpl.class);

    private String host;
    private int port = 6419;
    private String charset = "UTF-8";
    private String version = "";

    private final String welcomeMessagePattern = "([0-9]{3})([ -])(.*)";
    private Pattern patternMessage = Pattern.compile(welcomeMessagePattern);

    private static int timeout = 3000;

    private @Nullable Socket socket = null;
    private @Nullable BufferedWriter out = null;
    private @Nullable BufferedReader in = null;

    public SVDRPClientImpl(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

    public SVDRPClientImpl(String host, int port, String charset) {
        super();
        this.host = host;
        this.port = port;
        this.charset = charset;
    }

    @Override
    protected void finalize() {
        try {
            this.closeConnection();
        } catch (Exception ex) {
            logger.trace("Closing SVDRPConnection not successful: {}", ex.getMessage());
        }
    }

    /**
     *
     * Open VDR Socket Connection
     *
     * @throws IOException if an IO Error occurs
     */
    @SuppressWarnings("null")
    @Override
    public void openConnection() throws SVDRPConnectionException, SVDRPParseResponseException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket();
        }
        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            socket.connect(isa, timeout);
            socket.setSoTimeout(timeout);

            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset), 8192);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset), 8192);

            // read welcome message and init version & charset
            SVDRPResponse res = null;

            res = execute(null);

            if (res.getCode() == 220) {
                SVDRPWelcome welcome = SVDRPWelcome.parse(res.getMessage());
                this.charset = welcome.getCharset();
                this.version = welcome.getVersion();
            } else {
                throw new SVDRPParseResponseException(res);
            }
        } catch (IOException e1) {
            try {
                // cleanup after timeout
                out.close();
                in.close();

                socket.close();
            } catch (Exception e) {
            }
            throw new SVDRPConnectionException(e1.getMessage());
        }
    }

    /**
     * Close VDR Socket Connection
     *
     * @throws IOException if an IO Error occurs
     */
    @SuppressWarnings("null")
    @Override
    public void closeConnection() throws SVDRPConnectionException, SVDRPParseResponseException {
        /*
         * socket on vdr stays in FIN_WAIT2 without this
         */
        try {
            if (out != null) {
                out.write("QUIT");
                out.newLine();
                out.flush();
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            throw new SVDRPConnectionException(e.getMessage());
        }
    }

    /**
     *
     * execute SVDRP Call
     *
     * @param command SVDRP command to execute
     * @return response of SVDRPCall
     * @throws SVDRPException exception from SVDRP call
     */
    @SuppressWarnings("null")
    private SVDRPResponse execute(@Nullable String command)
            throws SVDRPConnectionException, SVDRPParseResponseException {
        StringBuilder message = new StringBuilder();
        // try {
        Matcher matcher = null;

        int code;
        try {
            if (command != null) {
                if (out == null) {
                    throw new SVDRPConnectionException("OutputStream is null!");
                } else {
                    out.write(command);
                    out.newLine();
                    out.flush();
                }
            }

            code = -1;
            String line = null;
            boolean cont = true;
            while (cont && (line = in.readLine()) != null) {
                matcher = patternMessage.matcher(line);
                if (matcher.matches() && matcher.groupCount() > 2) {
                    if (code < 0) {
                        code = Integer.parseInt(matcher.group(1));
                    }
                    if (" ".equals(matcher.group(2))) {
                        cont = false;
                    }
                    message.append(matcher.group(3));
                    if (cont) {
                        message.append(System.lineSeparator());
                    }
                } else {
                    cont = false;
                }
            }
            return new SVDRPResponse(code, message.toString());
        } catch (IOException e) {
            throw new SVDRPConnectionException(e.getMessage());
        } catch (NumberFormatException ne) {
            throw new SVDRPParseResponseException(ne.getMessage());
        }
    }

    /**
     * Retrieve Disk Status from SVDRP Client
     *
     * @return SVDRP Disk Status
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public SVDRPDiskStatus getDiskStatus() throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;

        res = execute("STAT disk");

        if (res.getCode() == 250) {
            SVDRPDiskStatus status = SVDRPDiskStatus.parse(res.getMessage());
            return status;
        } else {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Retrieve EPG Event from SVDRPClient
     *
     * @param type Type of EPG Event (now, next)
     * @return SVDRP EPG Event
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public SVDRPEpgEvent getEpgEvent(SVDRPEpgEvent.TYPE type)
            throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;

        SVDRPChannel channel = this.getCurrentSVDRPChannel();
        switch (type) {
            case NOW:
                res = execute(String.format("LSTE %s %s", channel.getNumber(), "now"));
                break;
            case NEXT:
                res = execute(String.format("LSTE %s %s", channel.getNumber(), "next"));
                break;
        }

        if (res != null && res.getCode() == 215) {
            SVDRPEpgEvent entry = SVDRPEpgEvent.parse(res.getMessage());
            return entry;
        } else if (res != null) {
            throw new SVDRPParseResponseException(res);
        } else {
            throw new SVDRPConnectionException("SVDRPResponse is Null");
        }
    }

    /**
     * Retrieve current volume from SVDRP Client
     *
     * @return SVDRP Volume Object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public SVDRPVolume getSVDRPVolume() throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;

        res = execute("VOLU");

        if (res.getCode() == 250) {
            SVDRPVolume volume = SVDRPVolume.parse(res.getMessage());
            return volume;
        } else {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Set volume on SVDRP Client
     *
     * @param newVolume Volume in Percent
     * @return SVDRP Volume Object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public SVDRPVolume setSVDRPVolume(int newVolume) throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;
        double newVolumeDouble = newVolume * 255 / 100;
        res = execute(String.format("VOLU %s", String.valueOf(Math.round(newVolumeDouble))));

        if (res.getCode() == 250) {
            SVDRPVolume volume = SVDRPVolume.parse(res.getMessage());
            return volume;
        } else {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Send Key command to SVDRP Client
     *
     * @param key Key Command to send
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public void sendSVDRPKey(String key) throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;
        res = execute(String.format("HITK %s", key));

        if (res.getCode() != 250) {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Send Message to SVDRP Client
     *
     * @param message Message to send
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public void sendSVDRPMessage(String message) throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;
        res = execute(String.format("MESG %s", message));

        if (res.getCode() != 250) {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Retrieve current Channel from SVDRP Client
     *
     * @return SVDRPChannel object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public SVDRPChannel getCurrentSVDRPChannel() throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;

        res = execute("CHAN");

        if (res.getCode() == 250) {
            SVDRPChannel channel = SVDRPChannel.parse(res.getMessage());
            return channel;
        } else {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Change current Channel on SVDRP Client
     *
     * @param number Channel to be set
     * @return SVDRPChannel object
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public SVDRPChannel setSVDRPChannel(int number) throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;

        res = execute(String.format("CHAN %s", number));

        if (res.getCode() == 250) {
            SVDRPChannel channel = SVDRPChannel.parse(res.getMessage());
            return channel;
        } else {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Retrieve from SVDRP Client if a recording is currently active
     *
     * @return is currently a recording active
     * @throws SVDRPConnectionException thrown if connection to VDR failed or was not possible
     * @throws SVDRPParseResponseException thrown if something's not OK with SVDRP response
     */
    @Override
    public boolean isRecordingActive() throws SVDRPConnectionException, SVDRPParseResponseException {
        SVDRPResponse res = null;
        res = execute("LSTT");

        if (res.getCode() == 250) {
            SVDRPTimerList timers = SVDRPTimerList.parse(res.getMessage());
            return timers.isRecordingActive();
        } else {
            throw new SVDRPParseResponseException(res);
        }
    }

    /**
     * Retrieve VDR Version from SVDRP Client
     *
     * @return VDR Version
     * @throws SVDRPException thrown if something's not OK with SVDRP call
     */
    @Override
    public String getSVDRPVersion() {
        return version;
    }
}
