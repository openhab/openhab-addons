/**
 *  CGateInterface - A library to allow interaction with Clipsal C-Gate.
 *  Copyright (C) 2008,2009,2012  Dave Oxley <dave@daveoxley.co.uk>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.openhab.binding.cbus.internal.cgate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dave Oxley <dave@daveoxley.co.uk>
 */
public class CGateSession extends CGateObject {
    private Logger logger = LoggerFactory.getLogger(DebugStatusChangeCallback.class);

    private final Map<String, BufferedWriter> response_writers = Collections
            .synchronizedMap(new HashMap<String, BufferedWriter>());
    private final Map<String, PipedWriter> piped_writers = Collections
            .synchronizedMap(new HashMap<String, PipedWriter>());

    private final CommandConnection command_connection;

    private final EventConnection event_connection;

    private final StatusChangeConnection status_change_connection;

    private final PingConnections ping_connections;

    private boolean pingKeepAlive = true;

    private boolean connected = false;

    private String sessionID;

    CGateSession(InetAddress cgate_server, int command_port, int event_port, int status_change_port) {
        super(null);
        if (cgate_server == null) {
            throw new NullPointerException("cgate_server cannot be null");
        }

        setupSubtreeCache("project");
        command_connection = new CommandConnection(cgate_server, command_port);
        event_connection = new EventConnection(cgate_server, event_port);
        status_change_connection = new StatusChangeConnection(cgate_server, status_change_port);
        // registerEventCallback(new DebugEventCallback());
        // registerStatusChangeCallback(new DebugStatusChangeCallback());
        ping_connections = new PingConnections();
    }

    @Override
    protected CGateSession getCGateSession() {
        return this;
    }

    @Override
    protected String getKey() {
        return null;
    }

    @Override
    public CGateObject getCGateObject(String address) throws CGateException {
        if (!address.startsWith("//")) {
            throw new IllegalArgumentException("Address must be a full address. i.e. Starting with //");
        }

        boolean return_next = false;
        int next_part_index = address.indexOf("/", 2);
        if (next_part_index == -1) {
            next_part_index = address.length();
            return_next = true;
        }

        String project_name = address.substring(2, next_part_index);
        Project project = Project.getProject(this, project_name);
        if (project == null) {
            throw new IllegalArgumentException("No project found: " + address);
        }

        if (return_next) {
            return project;
        }

        return project.getCGateObject(address.substring(next_part_index + 1));
    }

    @Override
    String getProjectAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    String getResponseAddress(boolean id) {
        throw new UnsupportedOperationException();
    }

    public void connect() throws CGateConnectException {
        if (connected) {
            return;
        }

        try {
            sessionID = null;
            command_connection.start();
            event_connection.start();
            status_change_connection.start();
            connected = true;
            if (pingKeepAlive) {
                ping_connections.start();
            }
        } catch (CGateConnectException e) {
            try {
                close();
            } catch (Exception e2) {
            }
            throw e;
        }
    }

    public void enableKeepAlivePing(boolean pingKeepAlive) {
        this.pingKeepAlive = pingKeepAlive;
    }

    /**
     * Issue a <code>quit</code> to the C-Gate server and close the input and output stream
     * and the command_socket.
     *
     * @see <a href="http://www.clipsal.com/cis/downloads/Toolkit/CGateServerGuide_1_0.pdf">
     *      <i>C-Gate Server Guide 4.3.99</i></a>
     * @throws com.daveoxley.cbus.CGateException
     */
    public void close() throws CGateException {
        if (!connected) {
            return;
        }

        synchronized (ping_connections) {
            try {
                sendCommand("quit");
            } catch (Exception e) {
            }

            try {
                for (BufferedWriter writer : response_writers.values()) {
                    writer.flush();
                    writer.close();
                }
                response_writers.clear();
                for (PipedWriter writer : piped_writers.values()) {
                    writer.flush();
                    writer.close();
                }
                piped_writers.clear();
                Response.thread_pool.shutdownNow();
                command_connection.stop();
                event_connection.stop();
                status_change_connection.stop();
                sessionID = null;
            } catch (Exception e) {
                throw new CGateException(e);
            } finally {
                clearCache();
                connected = false;
                ping_connections.notify();
            }
        }
    }

    /**
     *
     * @param cgate_command
     * @return ArrayList of C-Gate response lines
     * @throws com.daveoxley.cbus.CGateException
     */
    Response sendCommand(String cgate_command) throws CGateException {
        checkConnected();

        return command_connection.sendCommand(cgate_command);
    }

    public boolean isConnected() {
        return connected;
    }

    private void checkConnected() throws CGateNotConnectedException {
        if (!connected) {
            throw new CGateNotConnectedException();
        }
        try {
            command_connection.start();
            event_connection.start();
            status_change_connection.start();
        } catch (CGateConnectException e) {
            throw new CGateNotConnectedException();
        }
    }

    private void updateSessionID() {
        try {
            if (!isConnected()) {
                return;
            }
            sendCommand("session_id tag openHAB C-Bus Binding").handle200();
            ArrayList<String> resp_array = sendCommand("session_id").toArray();
            this.sessionID = responseToMap(resp_array.get(0)).get("sessionID");
            logger.debug("Updated session id: {}", sessionID);
        } catch (CGateException e) {
            logger.error("Cannot check session id", e);
        }
    }

    public String getSessionID() {
        if (sessionID == null) {
            updateSessionID();
        }
        return sessionID;
    }

    private abstract class CGateConnection implements Runnable {
        private final InetAddress server;

        private final int port;

        private final boolean create_output;

        private Thread thread = null;

        private Socket socket;

        private volatile BufferedReader input_reader;

        private PrintWriter output_stream;

        protected CGateConnection(InetAddress server, int port, boolean create_output) {
            this.server = server;
            this.port = port;
            this.create_output = create_output;
        }

        protected synchronized void start() throws CGateConnectException {
            if (thread != null) {
                return;
            }

            try {
                socket = new Socket(server, port);
                socket.setSoTimeout(5000);
                input_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if (create_output) {
                    output_stream = new PrintWriter(socket.getOutputStream(), true);
                }
                logConnected();
                thread = new Thread(this);
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
                throw new CGateConnectException(e);
            }
        }

        protected synchronized void stop() {
            try {
                thread = null;

                // Only close the Socket as trying to close the BufferedReader results
                // in a deadlock (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4859836).
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    new CGateException(e);
                }
            } catch (Exception e) {
                new CGateException(e);
            } finally {
                input_reader = null;
                output_stream = null;
                socket = null;
            }
        }

        public void println(String str) throws CGateException {
            if (!create_output || output_stream == null) {
                throw new CGateException();
            }

            output_stream.println(str);
            output_stream.flush();
        }

        protected final BufferedReader getInputReader() {
            return input_reader;
        }

        protected void logConnected() throws IOException {
        }

        protected synchronized boolean continueRunning() {
            return thread != null;
        }

        @Override
        public final void run() {
            try {
                while (continueRunning()) {
                    doRun();
                }
            } catch (IOException ioe) {
                if (thread != null) {
                    new CGateException(ioe);
                }
            } catch (Exception e) {
                new CGateException(e);
            } finally {
                boolean restart = thread != null;
                stop();
                if (restart) {
                    try {
                        start();
                    } catch (CGateConnectException e) {
                    }
                }
            }
        }

        protected abstract void doRun() throws IOException;
    }

    private class PingConnections implements Runnable {
        private Thread thread;

        private PingConnections() {
        }

        protected void start() {
            thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
            thread.setName("CGateConnectionPing");
        }

        @Override
        public synchronized void run() {
            try {
                while (connected && pingKeepAlive) {
                    try {
                        try {
                            wait(10000l);
                        } catch (InterruptedException e) {
                        }

                        if (connected && pingKeepAlive) {
                            CGateInterface.noop(CGateSession.this);
                        }
                    } catch (Exception e) {
                        // Close connection
                        /*
                         * command_connection.stop();
                         * event_connection.stop();
                         * status_change_connection.stop();
                         * clearCache();
                         * connected = false;
                         * logger.error("CGate connection lost");
                         */
                        logger.error("CGate connection error {}", e);
                    }
                }
            } finally {
                thread = null;
            }
        }
    }

    private BufferedReader getReader(String id) throws CGateException {
        try {
            PipedWriter piped_writer = new PipedWriter();
            BufferedWriter out = new BufferedWriter(piped_writer);
            response_writers.put(id, out);
            piped_writers.put(id, piped_writer);

            PipedReader piped_reader = new PipedReader(piped_writer);
            return new BufferedReader(piped_reader);
        } catch (IOException e) {
            throw new CGateException(e);
        }
    }

    private class CommandConnection extends CGateConnection {
        private int next_id = 0;

        private CommandConnection(InetAddress server, int port) {
            super(server, port, true);
        }

        private Response sendCommand(String cgate_command) throws CGateException {
            String id = getID();
            BufferedReader response_reader = getReader(id);

            command_connection.println("[" + id + "] " + cgate_command);

            return new Response(response_reader);
        }

        private synchronized String getID() {
            return String.valueOf(next_id++);
        }

        @Override
        protected void logConnected() throws IOException {
            logger.debug(getInputReader().readLine());
        }

        @Override
        public void doRun() throws IOException {
            String response;
            try {
                response = getInputReader().readLine();
            } catch (SocketTimeoutException e) {
                logger.trace("IO Timeout");
                return;
            }
            if (response == null) {
                super.stop();
            } else {
                int id_end = response.indexOf("]");
                String id = response.substring(1, id_end);
                String actual_response = response.substring(id_end + 2);

                BufferedWriter writer = response_writers.get(id);
                writer.write(actual_response);
                writer.newLine();

                if (!Response.responseHasMore(actual_response)) {
                    writer.flush();
                    writer.close();
                    piped_writers.get(id).close();
                    response_writers.remove(id);
                    piped_writers.remove(id);
                }
            }
        }
    }

    /**
     *
     * @param event_callback
     */
    public void registerEventCallback(EventCallback event_callback) {
        event_connection.registerEventCallback(event_callback);
    }

    private class EventConnection extends CGateConnection {

        private final ThreadPoolExecutor thread_pool = (ThreadPoolExecutor) ThreadPoolManager
                .getPool("CGateEventCallback");

        private final List<EventCallback> event_callbacks = Collections
                .synchronizedList(new ArrayList<EventCallback>());

        private EventConnection(InetAddress server, int port) {
            super(server, port, false);
        }

        private void registerEventCallback(EventCallback event_callback) {
            event_callbacks.add(event_callback);
        }

        @Override
        protected void doRun() throws IOException {
            String response;
            try {
                response = getInputReader().readLine();
            } catch (SocketTimeoutException e) {
                logger.trace("IO Timeout");
                return;
            }
            final String event = response;
            if (event == null) {
                super.stop();
            } else if (event.length() >= 19) {
                final int event_code = Integer.parseInt(event.substring(16, 19).trim());
                for (final EventCallback event_callback : event_callbacks) {
                    if (!continueRunning()) {
                        return;
                    }

                    try {
                        if (event_callback.acceptEvent(event_code)) {
                            thread_pool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    GregorianCalendar event_time = new GregorianCalendar(
                                            Integer.parseInt(event.substring(0, 4)),
                                            Integer.parseInt(event.substring(4, 6)),
                                            Integer.parseInt(event.substring(6, 8)),
                                            Integer.parseInt(event.substring(9, 11)),
                                            Integer.parseInt(event.substring(11, 13)),
                                            Integer.parseInt(event.substring(13, 15)));

                                    event_callback.processEvent(CGateSession.this, event_code, event_time,
                                            event.length() == 19 ? null : event.substring(19));
                                }
                            });
                        }
                    } catch (Exception e) {
                        new CGateException(e);
                    }
                }
            }
        }
    }

    /**
     *
     * @param event_callback
     */
    public void registerStatusChangeCallback(StatusChangeCallback status_change_callback) {
        status_change_connection.registerStatusChangeCallback(status_change_callback);
    }

    private class StatusChangeConnection extends CGateConnection {

        private final ThreadPoolExecutor thread_pool = (ThreadPoolExecutor) ThreadPoolManager
                .getPool("CGateStatusCallback");

        private final List<StatusChangeCallback> sc_callbacks = Collections
                .synchronizedList(new ArrayList<StatusChangeCallback>());

        private StatusChangeConnection(InetAddress server, int port) {
            super(server, port, false);
        }

        private void registerStatusChangeCallback(StatusChangeCallback event_callback) {
            sc_callbacks.add(event_callback);
        }

        @Override
        protected void doRun() throws IOException {
            String response;
            try {
                response = getInputReader().readLine();
            } catch (SocketTimeoutException e) {
                logger.trace("IO Timeout");
                return;
            }
            final String status_change = response;
            if (status_change == null) {
                super.stop();
            } else if (status_change.length() > 0) {
                for (final StatusChangeCallback sc_callback : sc_callbacks) {
                    if (!continueRunning()) {
                        return;
                    }

                    if (sc_callback.isActive()) {
                        try {
                            thread_pool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    sc_callback.processStatusChange(CGateSession.this, status_change);
                                }
                            });
                        } catch (Exception e) {
                            new CGateException(e);
                        }
                    }
                }
            }
        }
    }
}
