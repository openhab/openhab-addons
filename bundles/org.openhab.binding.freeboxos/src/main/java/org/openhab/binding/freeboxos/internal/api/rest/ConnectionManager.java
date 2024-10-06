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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;

import inet.ipaddr.IPAddress;

/**
 * The {@link ConnectionManager} is the Java class used to handle api requests related to connection
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ConnectionManager extends ConfigurableRest<ConnectionManager.Status, ConnectionManager.StatusResponse> {
    private static final String PATH = "connection";

    protected static class StatusResponse extends Response<Status> {
    }

    private class FtthStatusResponse extends Response<FtthStatus> {
    }

    private class XdslStatusResponse extends Response<XdslInfos> {
    }

    private enum State {
        GOING_UP,
        UP,
        GOING_DOWN,
        DOWN,
        UNKNOWN
    }

    private enum Type {
        ETHERNET,
        RFC2684,
        PPPOATM,
        UNKNOWN
    }

    public enum Media {
        FTTH,
        ETHERNET,
        XDSL,
        BACKUP_4G,
        UNKNOWN
    }

    public static record Status(State state, Type type, Media media, @Nullable List<Integer> ipv4PortRange,
            @Nullable IPAddress ipv4, // This can be null if state is not up
            @Nullable IPAddress ipv6, // This can be null if state is not up
            long rateUp, // current upload rate in byte/s
            long rateDown, // current download rate in byte/s
            long bandwidthUp, // available upload bandwidth in bit/s
            long bandwidthDown, // available download bandwidth in bit/s
            long bytesUp, // total uploaded bytes since last connection
            long bytesDown // total downloaded bytes since last connection
    ) {
    }

    public static record FtthStatus(boolean sfpPresent, boolean sfpAlimOk, boolean sfpHasPowerReport,
            boolean sfpHasSignal, boolean link, String sfpSerial, String sfpModel, String sfpVendor, //
            int sfpPwrTx, // scaled by 100 (in dBm)
            int sfpPwrRx // scaled by 100 (in dBm)
    ) {
        public double getReceivedDBM() {
            return 1d * sfpPwrRx / 100;
        }

        public double getTransmitDBM() {
            return 1d * sfpPwrTx / 100;
        }
    }

    public static record XdslStats( //
            int maxrate, // ATM max rate in kbit/s
            int rate, // ATM rate in kbit/s
            int snr, // in dB
            int attn, // in dB
            int snr10, // in dB/10
            int attn10, // in dB/10
            int fec, int crc, int hec, int es, int ses, boolean phyr, boolean ginp, boolean nitro, //
            int rxmt, // only available when phyr is on
            int rxmtCorr, // only available when phyr is on
            int rxmtUncorr, // only available when phyr is on
            int rtxTx, // only available when ginp is on
            int rtxC, // only available when ginp is on
            int rtxUc// only available when ginp is on
    ) {
    }

    public enum SynchroState {
        DOWN, // unsynchronized
        TRAINING, // synchronizing step 1/4
        STARTED, // synchronizing step 2/4
        CHAN_ANALYSIS, // synchronizing step 3/4
        MSG_EXCHANGE, // synchronizing step 4/4
        SHOWTIME, // Ready
        DISABLED, // Disabled
        UNKNOWN
    }

    public enum Modulation {
        ADSL,
        VDSL,
        UNKNOWN
    }

    public static record XdslStatus(SynchroState status, String protocol, Modulation modulation, long uptime) {
    }

    public static record XdslInfos(XdslStatus status, XdslStats down, XdslStats up) {
    }

    public ConnectionManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, StatusResponse.class, session.getUriBuilder().path(PATH), null);
    }

    public FtthStatus getFtthStatus() throws FreeboxException {
        return super.getSingle(FtthStatusResponse.class, "ftth");
    }

    public XdslInfos getXdslStatus() throws FreeboxException {
        return super.getSingle(XdslStatusResponse.class, "xdsl");
    }
}
