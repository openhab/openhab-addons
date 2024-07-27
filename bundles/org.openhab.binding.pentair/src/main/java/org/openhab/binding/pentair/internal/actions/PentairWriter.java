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
package org.openhab.binding.pentair.internal.actions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.parser.PentairBasePacket;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairWriter } class to be used as base for all action commands to send on Pentair bus.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class PentairWriter {
    private final Logger logger = LoggerFactory.getLogger(PentairWriter.class);

    private @Nullable OutputStream outputStream;
    private int sourceId;

    // lock used to prevent multiple commands to be sent at same time. Condition waitAck will be cleared when the
    // appropriate response has been received thus making writePacket blocking.
    private final ReentrantLock lock = new ReentrantLock();
    private Condition waitAck = lock.newCondition();
    private int ackResponse = -1;
    private CallbackWriter owner;

    public interface CallbackWriter {
        void writerFailureCallback();
    }

    public PentairWriter(CallbackWriter owner) {
        this.owner = owner;
    }

    public void initialize(OutputStream outputStream, int sourceId) {
        this.outputStream = outputStream;
        this.sourceId = sourceId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public OutputStream getOutputStream() {
        return Objects.requireNonNull(outputStream, "outputStream is null");
    }

    /**
     * Method to write a byte array to the bus. This method is blocking until a valid response is received, or a
     * failure.
     *
     * @param packet is the byte array to write to the bus
     */
    public boolean writePacket(byte[] packet) {
        return writePacket(packet, -1, 0);
    }

    /**
     * Method to write a byte array to the bus. This method is blocking until a valid response is received, or a
     * failure.
     *
     * @param packet is the byte array to write to the bus
     * @param response is the response to wait for
     * @param retries is the number of retries
     */
    public boolean writePacket(byte[] packet, int response, int retries) {
        PentairStandardPacket p = new PentairStandardPacket(packet);

        return writePacket(p, response, retries);
    }

    /**
     * Method to write a PentairStandardPackage to the bus. This method is blocking until a valid response is received,
     * or a failure.
     *
     * @param p {@link PentairStandardPacket} to write
     */
    public boolean writePacket(PentairStandardPacket p) {
        return writePacket(p, -1, 0);
    }

    /**
     * Method to write a package on the Pentair bus. Will add source ID and checksum to bytes written. This method is
     * blocking until a valid response is received, or a failure.
     *
     * @param p {@link PentairStandardPacket} to write
     * @param response is the expected response type to wait for from this package send. The Lock will
     *            clear when a response of this type is received and ackReponse is called.
     * @param retries is number of retries before a time-out
     */
    public boolean writePacket(PentairStandardPacket p, int response, int retries) {
        boolean success = true;
        OutputStream outputStream;

        outputStream = getOutputStream();

        try {
            byte[] buf;
            int nRetries = retries;

            p.setByte(PentairStandardPacket.SOURCE, (byte) sourceId);

            buf = p.wrapPacketToSend();

            lock.lock();
            this.ackResponse = response;

            do {
                logger.trace("[{}] Writing packet: {}", p.getDest(), PentairBasePacket.toHexString(buf));

                outputStream.write(buf, 0, buf.length);
                outputStream.flush();

                if (response != -1) {
                    logger.trace("[{}] writePacket: wait for ack (response: {}, retries: {})", p.getDest(), response,
                            nRetries);
                    success = waitAck.await(1000, TimeUnit.MILLISECONDS); // success will be false if timeout
                    nRetries--;
                }
            } while (!success && (nRetries >= 0));
        } catch (IOException e) {
            owner.writerFailureCallback();
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            owner.writerFailureCallback();
            return false;
        } finally {
            lock.unlock();
        }

        if (!success) {
            logger.trace("[{}] writePacket: timeout", p.getDest());
        }

        return success;
    }

    /**
     * Method to acknowledge an ack or response packet has been sent
     *
     * @param cmdresponse is the command that was seen as a return. This is validate against that this was the response
     *            before signally a return.
     */
    public void ackResponse(int response) {
        if (response != ackResponse) {
            return;
        }

        lock.lock();
        waitAck.signalAll();
        lock.unlock();
    }
}
