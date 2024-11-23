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
package org.openhab.binding.energenie.internal.handler;

import static org.openhab.binding.energenie.internal.EnergenieBindingConstants.*;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.util.HexUtils;
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
    private final String host;
    private final byte[] key;

    public EnergenieSocket(final String host, final String password) {
        this.host = host;
        this.key = getKey(password);
    }

    private static byte[] getKey(final String password) {
        final int passwordLength = password.length();
        String passwordString = password;
        for (int i = 0; i < (8 - passwordLength); i++) {
            passwordString = passwordString + " ";
        }
        return passwordString.getBytes();
    }

    public synchronized byte[] sendCommand(final byte[] ctrl) throws IOException {
        try (final TaskSocket taskSocket = authorize()) {
            final OutputStream output = taskSocket.socket.getOutputStream();
            final DataInputStream input = new DataInputStream(taskSocket.socket.getInputStream());

            if (output == null) {
                throw new IOException("No connection");
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Control message send to EG (int) '{}' (hex)'{}'", ctrl, HexUtils.bytesToHex(ctrl));
                }
                output.write(encryptControls(ctrl, taskSocket.task));
                output.flush();
                readStatus(input, taskSocket);
                return updateStatus(taskSocket);
            }
        }
    }

    public synchronized byte[] retrieveStatus() throws IOException {
        try (final TaskSocket taskSocket = authorize()) {
            return updateStatus(taskSocket);
        }
    }

    private TaskSocket authorize() throws IOException {
        final TaskSocket taskSocket = new TaskSocket();
        final OutputStream output = taskSocket.socket.getOutputStream();
        final DataInputStream input = new DataInputStream(taskSocket.socket.getInputStream());
        if (output == null) {
            throw new IOException("No connection");
        }
        output.write(MESSAGE);
        output.flush();
        logger.trace("Start Condition '{}' send to EG", MESSAGE);
        input.readFully(taskSocket.task);

        if (logger.isTraceEnabled()) {
            logger.trace("EG responded with task (int) '{}' (hex) '{}'", taskSocket.task,
                    HexUtils.bytesToHex(taskSocket.task));
        }
        final byte[] solutionMessage = calculateSolution(taskSocket.task);

        output.write(solutionMessage);
        output.flush();
        logger.trace("Solution '{}' send to EG", solutionMessage);
        readStatus(input, taskSocket);
        return taskSocket;
    }

    private void readStatus(final DataInputStream input, final TaskSocket taskSocket) throws IOException {
        input.readFully(taskSocket.statcryp);
        if (logger.isTraceEnabled()) {
            logger.trace("EG responded with statcryp (int) '{}' (hex) '{}'", taskSocket.statcryp,
                    HexUtils.bytesToHex(taskSocket.statcryp));
        }
    }

    private byte[] updateStatus(final TaskSocket taskSocket) throws IOException {
        final byte[] status = decryptStatus(taskSocket);

        if (logger.isTraceEnabled()) {
            logger.trace("EG responded with status (int) '{}' (hex) '{}'", status, HexUtils.bytesToHex(status));
        }
        return status;
    }

    private byte[] calculateSolution(final byte[] task) {
        final int[] uIntTask = new int[4];

        for (int i = 0; i < 4; i++) {
            uIntTask[i] = Byte.toUnsignedInt(task[i]);
        }
        final int solutionLoword = (((uIntTask[0] ^ key[2]) * key[0]) ^ (key[6] | (key[4] << 8)) ^ uIntTask[2]);
        final byte[] loword = ByteBuffer.allocate(4).putInt(solutionLoword).array();

        final int solutionHiword = (((uIntTask[1] ^ key[3]) * key[1]) ^ (key[7] | (key[5] << 8)) ^ uIntTask[3]);
        final byte[] hiword = ByteBuffer.allocate(4).putInt(solutionHiword).array();
        final byte[] solution = new byte[SOLUTION_LEN];

        solution[0] = loword[3];
        solution[1] = loword[2];
        solution[2] = hiword[3];
        solution[3] = hiword[2];

        return solution;
    }

    private byte[] decryptStatus(final TaskSocket taskSocket) {
        final byte[] status = new byte[4];

        for (int i = 0; i < 4; i++) {
            status[i] = (byte) ((((taskSocket.statcryp[3 - i] - key[1]) ^ key[0]) - taskSocket.task[3])
                    ^ taskSocket.task[2]);
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

    private class TaskSocket implements Closeable {
        final Socket socket;
        final byte[] task = new byte[TASK_LEN];
        final byte[] statcryp = new byte[STATCRYP_LEN];

        public TaskSocket() throws UnknownHostException, IOException {
            socket = new Socket(host, TCP_PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLISECONDS);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
