/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport.stream;

import java.io.IOException;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IOStreamWriter} represents an io stream writer
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class IOStreamWriter implements Runnable {
    private static final int REPLY_TIMEOUT_TIME = 8000; // milliseconds

    private final Logger logger = LoggerFactory.getLogger(IOStreamWriter.class);

    private static enum ReplyStatus {
        GOT_ACK,
        GOT_NACK,
        WAITING_FOR_ACK
    }

    private final IOStream stream;
    private final IOStreamListener listener;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition messageReceived = lock.newCondition();
    private final Condition replyReceived = lock.newCondition();
    private final PriorityBlockingQueue<Msg> messageQueue = new PriorityBlockingQueue<>(10,
            Comparator.comparing(Msg::getPriority).thenComparingLong(Msg::getTimestamp));
    private ReplyStatus replyStatus = ReplyStatus.GOT_ACK;
    private @Nullable Msg lastMsg;

    public IOStreamWriter(IOStream stream, IOStreamListener listener) {
        this.stream = stream;
        this.listener = listener;
    }

    @Override
    public void run() {
        logger.trace("starting thread");
        try {
            while (!Thread.interrupted()) {
                logger.trace("checking message queue");
                Msg msg = messageQueue.take();
                if (msg.isExpired()) {
                    logger.trace("skipping expired message: {}", msg);
                } else {
                    stream.write(msg.getData());
                    listener.messageSent(msg);
                    waitForReply(msg);
                    limitRate(stream.getRateLimitTime());
                }
            }
        } catch (InterruptedException e) {
            logger.trace("got interrupted!");
        } catch (IOException e) {
            logger.trace("got an io exception", e);
            listener.disconnected();
        }
        logger.trace("exiting thread!");
    }

    public int getQueueSize() {
        return messageQueue.size();
    }

    public void addMessage(Msg msg) {
        messageQueue.add(msg);
    }

    public void clearQueue() {
        messageQueue.clear();
    }

    public void messageReceived(Msg msg) {
        lock.lock();
        try {
            if (replyStatus == ReplyStatus.WAITING_FOR_ACK) {
                if (msg.isPureNack() || msg.isReplyOf(lastMsg)) {
                    replyStatus = msg.isPureNack() ? ReplyStatus.GOT_NACK : ReplyStatus.GOT_ACK;
                    logger.trace("signaling receipt of ack: {}", replyStatus == ReplyStatus.GOT_ACK);
                    replyReceived.signal();
                }
            }
            messageReceived.signal();
        } finally {
            lock.unlock();
        }
    }

    public void invalidMessageReceived() {
        lock.lock();
        try {
            if (replyStatus == ReplyStatus.WAITING_FOR_ACK) {
                logger.trace("got bad data back, must assume message was acked.");
                replyStatus = ReplyStatus.GOT_ACK;
                replyReceived.signal();
            }
            messageReceived.signal();
        } finally {
            lock.unlock();
        }
    }

    private void waitForReply(Msg msg) throws InterruptedException {
        lock.lock();
        try {
            lastMsg = msg;
            replyStatus = ReplyStatus.WAITING_FOR_ACK;
            logger.trace("waiting for reply ack");
            if (replyReceived.await(REPLY_TIMEOUT_TIME, TimeUnit.MILLISECONDS)) {
                logger.trace("got reply ack: {}", replyStatus == ReplyStatus.GOT_ACK);
            } else {
                logger.trace("reply ack timeout expired");
                replyStatus = ReplyStatus.GOT_NACK;
            }
            if (replyStatus == ReplyStatus.GOT_NACK) {
                logger.trace("retransmitting msg: {}", msg);
                msg.setPriority(Priority.RETRANSMIT);
                messageQueue.add(msg);
            }
        } finally {
            lock.unlock();
        }
    }

    private void limitRate(int time) throws InterruptedException {
        lock.lock();
        try {
            do {
                logger.trace("rate limited for {} msec", time);
            } while (messageReceived.await(time, TimeUnit.MILLISECONDS));
        } finally {
            lock.unlock();
        }
    }
}
