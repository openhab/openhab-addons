/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openhab.binding.elerotransmitterstick.internal.config.EleroTransmitterStickConfig;
import org.openhab.binding.elerotransmitterstick.internal.handler.StatusListener;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Volker Bier - Initial contribution
 */
public class TransmitterStick {
    private final Logger logger = LoggerFactory.getLogger(TransmitterStick.class);
    private final HashMap<Integer, ArrayList<StatusListener>> allListeners = new HashMap<>();
    private final StickListener listener;

    private EleroTransmitterStickConfig config;
    private SerialPortManager serialPortManager;
    private CommandWorker worker;

    public TransmitterStick(StickListener l) {
        listener = l;
    }

    public synchronized void initialize(EleroTransmitterStickConfig stickConfig, ScheduledExecutorService scheduler,
            SerialPortManager serialPortManager) {
        logger.debug("Initializing Transmitter Stick...");
        config = stickConfig;
        this.serialPortManager = serialPortManager;
        worker = new CommandWorker();
        scheduler.schedule(worker, 0, TimeUnit.MILLISECONDS);
        logger.debug("Transmitter Stick initialized, worker running.");
    }

    public synchronized void dispose() {
        logger.debug("Disposing Transmitter Stick...");
        worker.terminateUpdates();
        worker = null;
        config = null;
        logger.debug("Transmitter Stick disposed.");
    }

    public synchronized ArrayList<Integer> getKnownIds() {
        if (worker != null) {
            return worker.validIds;
        }

        return new ArrayList<>();
    }

    public synchronized void sendCommand(CommandType cmd, List<Integer> channelIds) {
        if (worker != null) {
            worker.executeCommand(cmd, channelIds);
        }
    }

    public synchronized void requestUpdate(List<Integer> channelIds) {
        if (worker != null) {
            worker.requestUpdates(channelIds);
        }
    }

    public void addStatusListener(int channelId, StatusListener listener) {
        synchronized (allListeners) {
            ArrayList<StatusListener> listeners = allListeners.get(channelId);
            if (listeners == null) {
                listeners = new ArrayList<>();
                allListeners.put(channelId, listeners);
            }
            listeners.add(listener);
        }
    }

    public void removeStatusListener(int channelId, StatusListener listener) {
        synchronized (allListeners) {
            ArrayList<StatusListener> listeners = allListeners.get(channelId);
            if (listeners != null) {
                listeners.remove(listener);

                if (listeners.isEmpty()) {
                    allListeners.remove(channelId);
                }
            }
        }
    }

    private void notifyListeners(int channelId, ResponseStatus status) {
        synchronized (allListeners) {
            ArrayList<StatusListener> listeners = allListeners.get(channelId);
            if (listeners != null) {
                for (StatusListener l : listeners) {
                    l.statusChanged(channelId, status);
                }
            }
        }
    }

    /**
     * Make sure we have
     * - only one INFO for the same channel ids
     * - only one other command for the same channel ids
     */
    private static boolean prepareAddition(Command newCmd, Collection<Command> coll) {
        Iterator<Command> queuedCommands = coll.iterator();
        while (queuedCommands.hasNext()) {
            Command existingCmd = queuedCommands.next();

            if (Arrays.equals(newCmd.getChannelIds(), existingCmd.getChannelIds())) {
                // remove pending INFOs for same channel ids
                if (newCmd.getCommandType() == CommandType.INFO && existingCmd.getCommandType() == CommandType.INFO) {
                    if (existingCmd.getPriority() < newCmd.priority) {
                        // we have an older INFO command with same or lower priority, remove
                        queuedCommands.remove();
                    } else {
                        // existing has higher priority, skip addition
                        return false;
                    }
                }

                if (newCmd.getCommandType() != CommandType.INFO && existingCmd.getCommandType() != CommandType.INFO) {
                    // we have an older command for the same channels, remove
                    queuedCommands.remove();
                }
            }
        }

        return true;
    }

    static class DueCommandSet extends TreeSet<Command> {
        private static final long serialVersionUID = -3216360253151368826L;

        public DueCommandSet() {
            super(new Comparator<>() {
                /**
                 * Due commands are sorted by priority first and then by delay.
                 */
                @Override
                public int compare(Command o1, Command o2) {
                    if (o1.equals(o2)) {
                        return 0;
                    }

                    int d = o2.getPriority() - o1.getPriority();
                    if (d < 0) {
                        return -1;
                    }

                    if (d == 0 && o1.getDelay(TimeUnit.MILLISECONDS) < o2.getDelay(TimeUnit.MILLISECONDS)) {
                        return -1;
                    }
                    return 1;
                }
            });
        }

        @Override
        public boolean add(Command e) {
            if (TransmitterStick.prepareAddition(e, this)) {
                return super.add(e);
            }

            return false;
        }
    }

    class CommandWorker implements Runnable {
        private ArrayList<Integer> validIds = new ArrayList<>();
        private final AtomicBoolean terminated = new AtomicBoolean();
        private final int updateInterval;
        private final SerialConnection connection;

        private final BlockingQueue<Command> cmdQueue = new DelayQueue<Command>() {
            @Override
            public boolean add(Command e) {
                if (TransmitterStick.prepareAddition(e, this)) {
                    return super.add(e);
                }

                return false;
            }
        };

        CommandWorker() {
            connection = new SerialConnection(config.portName, serialPortManager);
            updateInterval = config.updateInterval;
        }

        void terminateUpdates() {
            terminated.set(true);

            // add a NONE command to make the thread exit from the call to take()
            cmdQueue.add(new Command(CommandType.NONE));
        }

        void requestUpdates(List<Integer> channelIds) {
            // this is a workaround for a bug in the stick firmware that does not
            // handle commands that are sent to multiple channels correctly
            if (channelIds.size() > 1) {
                for (int channelId : channelIds) {
                    requestUpdates(Collections.singletonList(channelId));
                }
            } else if (!channelIds.isEmpty()) {
                final Integer[] ids = channelIds.toArray(new Integer[channelIds.size()]);

                logger.debug("adding INFO command for channel id {} to queue...", Arrays.toString(ids));
                cmdQueue.add(new DelayedCommand(CommandType.INFO, 0, Command.FAST_INFO_PRIORITY, ids));
            }
        }

        void executeCommand(CommandType command, List<Integer> channelIds) {
            // this is a workaround for a bug in the stick firmware that does not
            // handle commands that are sent to multiple channels correctly
            if (channelIds.size() > 1) {
                for (int channelId : channelIds) {
                    executeCommand(command, Collections.singletonList(channelId));
                }
            } else if (!channelIds.isEmpty()) {
                final Integer[] ids = channelIds.toArray(new Integer[channelIds.size()]);

                logger.debug("adding command {} for channel ids {} to queue...", command, Arrays.toString(ids));
                cmdQueue.add(new Command(command, ids));
            }
        }

        @Override
        public void run() {
            try {
                queryChannels();
                doWork();
            } catch (Throwable t) {
                logger.error("Worker stopped by unexpected exception", t);
            } finally {
                connection.close();
            }
        }

        private void doWork() {
            // list of due commands sorted by priority
            final DueCommandSet dueCommands = new DueCommandSet();

            logger.debug("worker started.");
            while (!terminated.get()) {
                waitConnected();

                try {
                    // in case we have no commands that are currently due, wait for a new one
                    if (dueCommands.isEmpty()) {
                        logger.trace("No due commands, invoking take on queue...");
                        dueCommands.add(cmdQueue.take());
                        logger.trace("take returned {}", dueCommands.first());
                    }

                    if (!terminated.get()) {
                        // take all commands that are due from the queue
                        logger.trace("Draining all available commands...");
                        Command cmd;
                        int drainCount = 0;
                        while ((cmd = cmdQueue.poll()) != null) {
                            drainCount++;
                            dueCommands.remove(cmd);
                            dueCommands.add(cmd);
                        }
                        logger.trace("Drained {} commands, active queue size is {}, queue size is {}", drainCount,
                                dueCommands.size(), cmdQueue.size());

                        // process the command with the highest priority
                        cmd = dueCommands.first();
                        logger.debug("active command is {}", cmd);

                        if (cmd.getCommandType() != CommandType.NONE) {
                            Response response = connection.sendPacket(CommandUtil.createPacket(cmd));
                            // remove the command now we know it has been correctly processed
                            dueCommands.pollFirst();

                            if (response != null && response.hasStatus()) {
                                for (int id : response.getChannelIds()) {
                                    notifyListeners(id, response.getStatus());
                                }
                            }

                            if (cmd instanceof TimedCommand) {
                                long delay = 1000 * ((TimedCommand) cmd).getDuration();
                                logger.debug("adding timed command STOP for channel ids {} to queue with delay {}...",
                                        cmd.getChannelIds(), delay);

                                cmdQueue.add(new DelayedCommand(CommandType.STOP, delay, Command.TIMED_PRIORITY,
                                        cmd.getChannelIds()));
                            } else if (response != null && response.isMoving()) {
                                logger.debug("adding timed command INFO for channel ids {} to queue with delay 2000...",
                                        cmd.getChannelIds());

                                cmdQueue.add(new DelayedCommand(CommandType.INFO, 2000, Command.FAST_INFO_PRIORITY,
                                        cmd.getChannelIds()));
                            } else if (cmd.getCommandType() == CommandType.INFO) {
                                logger.debug("adding timed command INFO for channel ids {} to queue with delay {}...",
                                        cmd.getChannelIds(), updateInterval * 1000);

                                cmdQueue.add(new DelayedCommand(CommandType.INFO, updateInterval * 1000,
                                        Command.INFO_PRIORITY, cmd.getChannelIds()));
                            }
                        } else {
                            logger.trace("ignoring NONE command.");
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error("Got interrupt while waiting for next command time", e);
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    logger.error("Got IOException communicating with the stick", e);
                    listener.connectionDropped(e);
                    connection.close();
                }
            }
            logger.debug("worker finished.");
        }

        private void queryChannels() {
            logger.debug("querying available channels...");
            while (!terminated.get()) {
                waitConnected();

                try {
                    Response r = null;
                    while (r == null && !terminated.get() && connection.isOpen()) {
                        logger.debug("sending CHECK packet...");
                        r = connection.sendPacket(CommandUtil.createPacket(CommandType.CHECK));

                        if (r == null) {
                            Thread.sleep(2000);
                        }
                    }

                    if (r != null) {
                        int[] knownIds = r.getChannelIds();
                        logger.debug("Worker found channels: {} ", Arrays.toString(knownIds));

                        for (int id : knownIds) {
                            if (!validIds.contains(id)) {
                                validIds.add(id);
                            }
                        }

                        requestUpdates(validIds);
                        break;
                    }
                } catch (IOException e) {
                    logger.error("Got IOException communicating with the stick", e);
                    listener.connectionDropped(e);
                    connection.close();
                } catch (InterruptedException e) {
                    logger.error("Got interrupt while waiting for next command time", e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void waitConnected() {
            if (!connection.isOpen()) {
                while (!connection.isOpen() && !terminated.get()) {
                    try {
                        connection.open();
                        listener.connectionEstablished();
                    } catch (ConnectException e1) {
                        listener.connectionDropped(e1);
                    }

                    if (!connection.isOpen() && !terminated.get()) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            logger.error("Got interrupt while waiting for next command time", e);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }

            logger.trace("finished waiting. connection open={}, terminated={}", connection.isOpen(), terminated.get());
        }
    }

    public interface StickListener {
        void connectionEstablished();

        void connectionDropped(Exception e);
    }
}
