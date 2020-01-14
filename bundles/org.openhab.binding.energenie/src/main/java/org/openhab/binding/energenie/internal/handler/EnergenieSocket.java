/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.energenie.internal.handler;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class handling the Socket connections.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Hilbrand Bouwkamp - Moved Socket to it's own class
 */
@NonNullByDefault
class EnergenieSocket {

    private static final int SOCKET_TIMEOUT_MILLISECONDS = 1500;
    private static final byte[] MESSAGE = { 0x11 };

    private final Logger logger = LoggerFactory.getLogger(EnergenieSocket.class);

    private @Nullable Socket socket = null;
    private @Nullable OutputStream output = null;
    private @Nullable InputStream input = null;

    private final String host;
    private final byte[] key;

    public EnergenieSocket(final String host, final String password) {
        this.host = host;
        key = getKey(password);
    }

    private byte[] getKey(final String password) {
        final int passwordLength = password.length();
        String passwordString = password;
        for (int i = 0; i < (8 - passwordLength); i++) {
            passwordString = passwordString + " ";
        }
        return passwordString.getBytes();
    }

    public synchronized byte[] sendCommand(final byte[] ctrl) throws IOException {
        try {
            final TaskStat taskStat = authorize();
            final OutputStream output = this.output;
            final InputStream input = this.input;

            if (output == null || input == null) {
                throw new IOException("No connection");
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Control message send to EG (int) '{}' (hex)'{}'", ctrl, HexUtils.bytesToHex(ctrl));
                }
                output.write(encryptControls(ctrl, taskStat.task));
                readStatus(input, taskStat);
                return updateStatus(taskStat);
            }
        } finally {
            close();
        }
    }

    public synchronized byte[] retrieveStatus() throws IOException {
        try {
            return updateStatus(authorize());
        } finally {
            close();
        }
    }

    private TaskStat authorize() throws IOException {
        connect();
        final OutputStream output = this.output;
        final InputStream input = this.input;

        if (output == null || input == null) {
            throw new IOException("No connection");
        }
        output.write(MESSAGE);
        logger.trace("Start Condition '{}' send to EG", MESSAGE);
        final TaskStat taskStat = new TaskStat();
        input.read(taskStat.task);

        if (logger.isTraceEnabled()) {
            logger.trace("EG responded with task (int) '{}' (hex) '{}'", taskStat.task,
                    HexUtils.bytesToHex(taskStat.task));
        }
        final byte[] solutionMessage = calculateSolution(taskStat.task);

        output.write(solutionMessage);
        logger.trace("Solution '{}' send to EG", solutionMessage);
        readStatus(input, taskStat);
        return taskStat;
    }

    private void readStatus(final InputStream input, final TaskStat taskStat) throws IOException {
        input.read(taskStat.statcryp);
        if (logger.isTraceEnabled()) {
            logger.trace("EG responded with statcryp (int) '{}' (hex) '{}'", taskStat.statcryp,
                    HexUtils.bytesToHex(taskStat.statcryp));
        }
    }

    private byte[] updateStatus(final TaskStat taskStat) throws IOException {
        final InputStream input = this.input;

        if (input == null) {
            throw new IOException("No connection");
        } else {
            final byte[] status = decryptStatus(taskStat);

            if (logger.isTraceEnabled()) {
                logger.trace("EG responded with status (int) '{}' (hex) '{}'", status, HexUtils.bytesToHex(status));
            }
            return status;
        }
    }

    public void connect() throws UnknownHostException, IOException {
        final Socket socket = this.socket;

        if (socket == null || socket.isClosed()) {
            final Socket newSocket = new Socket(host, TCP_PORT);
            this.socket = newSocket;

            newSocket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
            output = newSocket.getOutputStream();
            input = newSocket.getInputStream();
        }
    }

    private void close() {
        final Socket socket = this.socket;

        if (socket != null) {
            try {
                socket.close();
            } catch (final IOException e) {
                logger.trace("Error closing socket", e);
            }
            this.socket = null;
        }
    }

    private byte[] calculateSolution(final byte[] task) {
        final int[] uIntTask = new int[4];

        for (int i = 0; i < 4; i++) {
            uIntTask[i] = Byte.toUnsignedInt(task[i]);
        }
        final int solutionLoword = (((uIntTask[0] ^ key[2]) * key[0]) ^ (key[6] | (key[4] << 8)) ^ uIntTask[2]);
        final byte loword[] = ByteBuffer.allocate(4).putInt(solutionLoword).array();

        final int solutionHiword = (((uIntTask[1] ^ key[3]) * key[1]) ^ (key[7] | (key[5] << 8)) ^ uIntTask[3]);
        final byte hiword[] = ByteBuffer.allocate(4).putInt(solutionHiword).array();
        final byte[] solution = new byte[SOLUTION_LEN];

        solution[0] = loword[3];
        solution[1] = loword[2];
        solution[2] = hiword[3];
        solution[3] = hiword[2];

        return solution;
    }

    private byte[] decryptStatus(final TaskStat taskStat) {
        final byte[] status = new byte[4];

        for (int i = 0; i < 4; i++) {
            status[i] = (byte) ((((taskStat.statcryp[3 - i] - key[1]) ^ key[0]) - taskStat.task[3]) ^ taskStat.task[2]);
        }
        return status;
    }

    private byte[] encryptControls(final byte[] controls, final byte[] task) {
        final byte[] ctrlcryp = new byte[CTRLCRYP_LEN];

        for (int i = 0; i < 4; i++) {
            ctrlcryp[i] = (byte) ((((controls[3 - i] ^ task[2]) + task[3]) ^ key[0]) + key[1]);
        }
        return ctrlcryp;
    }

    private class TaskStat {
        final byte[] task = new byte[TASK_LEN];
        final byte[] statcryp = new byte[STATCRYP_LEN];
    }
}
