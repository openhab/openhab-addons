/*---------------------------------------------------------------------------
 * Copyright (C) 2002 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.dalsemi.onewire.adapter;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.utils.Convert;

/**
 * <P>
 * NetAdapter is a network-based DSPortAdapter. It allows for the use of
 * an actual DSPortAdapter which isn't on the local machine, but rather is
 * connected to another device which is reachable via a TCP/IP network
 * connection.
 * </P>
 *
 * <P>
 * The syntax for the <code>selectPort(String)</code> command is the
 * hostname of the computer which hosts the actual DSPortAdapter and the
 * TCP/IP port that the host is listening on. If the port number is not
 * specified, a default value of 6161 is used. Here are a few examples to
 * illustrate the syntax:
 * <ul>
 * <li>my.host.com:6060</li>
 * <li>180.0.2.46:6262</li>
 * <li>my.host.com</li>
 * <li>180.0.2.46</li>
 * </ul>
 * </P>
 *
 * <P?The use of the NetAdapter is virtually identical to the use of any
 * other DSPortAdapter. The only significant changes are the necessity
 * of the 'host' component (see NetAdapterHost)
 * and the discovery of hosts on your network. There are currently two
 * techniques used for discovering all of the hosts: The look-up of each host
 * from the onewire.properties file and the use of multicast sockets for
 * automatic discovery.
 * </P>
 *
 * <P>
 * In the onewire.properties file, you can add a host to your list of valid
 * hosts by making a NetAdapter.host with an integer to distinguish the hosts.
 * There is no limit on the number of hosts which can appear in this list, but
 * the first one must be numbered '0'. These hosts will then be returned in
 * the list of valid 'ports' from the <code>selectPortNames()</code> method.
 * Note that there do not have to be any servers returned from
 * <code>selectPortNames()</code> for the NetAdapter to be able to connect
 * to them (so it isn't necessary to add these entries for it to function),
 * but applications which allow a user to automatically select an appropriate
 * adapter and a port from a given list will not function properly without it.
 * For example:
 * <ul>
 * <li>NetAdapter.host0=my.host.com:6060</li>
 * <li>NetAdapter.host1=180.0.2.46:6262</li>
 * <li>NetAdapter.host2=my.host.com</li>
 * <li>NetAdapter.host3=180.0.2.46</li>
 * </ul>
 * </P>
 *
 * <P>
 * The multicast socket technique allows you to automatically discover
 * hosts on your subnet which are listening for multicast packets. By
 * default, the multicast discovery of NetAdapter hosts is disabled.
 * When enabled, the NetAdapter creates a multicast socket and looks for servers
 * every time you call <code>selectPortNames()</code>. This will add a
 * 1 second delay (due to the socket timeout) on calling the method. If you'd
 * like to enable this feature, add the following line to your
 * onewire.properties file:
 * <ul>
 * <li>NetAdapter.MulticastEnabled=true</li>
 * </ul>
 * The port used and the multicast group used for multicast sockets can
 * also be changed. The group however, must fall withing a valid range.
 * For more information about multicast sockets in Java, see the Java
 * tutorial on networking at <A HREF="http://java.sun.com/docs/books/tutorial/">
 * http://java.sun.com/docs/books/tutorial/</A>. Change the defaults in the
 * onewire.properties file with the following entries:
 * <ul>
 * <li>NetAdapter.MulticastGroup=228.5.6.7</li>
 * <li>NetAdapter.MulticastPort=6163</li>
 * </ul>
 * </P>
 *
 * <P>
 * Once the NetAdapter is connected with a host, a version check is performed
 * followed by a simple authentication step. The authentication is dependent
 * upon a secret shared between the NetAdapter and the host. Both will use
 * a default value, that each will agree with if you don't provide a secret
 * of your own. To set the secret, add the following line to your
 * onewire.properties file:
 * <ul>
 * <li>NetAdapter.secret="This is my custom secret"</li>
 * </ul>
 * Optionally, the secret can be specified on a per-host basis by simply
 * adding the secret after the port number followed by a colon. If no port
 * number is specified, a double-colon is required. Here are examples:
 * <ul>
 * <li>my.host.com:6060:my custom secret</li>
 * <li>180.0.2.46:6262:another custom secret</li>
 * <li>my.host.com::the custom secret without port number</li>
 * <li>180.0.2.46::another example of a custom secret</li>
 * </ul>
 * </P>
 *
 * <P>
 * All of the above mentioned properties can be set on the command-line
 * as well as being set in the onewire.properties file. To set the
 * properties on the command-line, use the -D option:
 * java -DNetAdapter.Secret="custom secret" myApplication
 * </P>
 *
 * <P>
 * The following is a list of all parameters that can be set for the
 * NetAdapter, followed by default values where applicable.<br>
 * <ul>
 * <li>NetAdapter.secret=Adapter Secret Default</li>
 * <li>NetAdapter.secret[0-MaxInt]=[no default]</li>
 * <li>NetAdapter.host[0-MaxInt]=[no default]</li>
 * <li>NetAdapter.MulticastEnabled=false</li>
 * <li>NetAdapter.MulticastGroup=228.5.6.7</li>
 * <li>NetAdapter.MulticastPort=6163</li>
 * </ul>
 * </P>
 *
 * <p>
 * If you wanted added security on the communication channel, an SSL socket
 * (or similar custom socket implementation) can be used by circumventing the
 * standard DSPortAdapter's <code>selectPort(String)</code> and using the
 * NetAdapter-specific <code>selectPort(Socket)</code>. For example:
 *
 * <pre>
 * NetAdapter na = new NetAdapter();
 *
 * Socket secureSocket = // insert fancy secure socket implementation here
 *
 * na.selectPort(secureSocket);
 *
 * <pre>
 * </P>
 *
 * <P>
 * For information on setting up the host component, see the JavaDocs
 * for the <code>NetAdapterHost</code>
 *
 * @see NetAdapterHost
 *
 * @author SH
 * @version 1.00, 9 Jan 2002
 */
public class NetAdapter extends DSPortAdapter implements NetAdapterConstants {
    /** Error message when neither RET_SUCCESS or RET_FAILURE are returned */
    protected static final String UNSPECIFIED_ERROR = "An unspecified error occurred.";
    /** Error message when I/O failure occurs */
    protected static final String COMM_FAILED = "IO Error: ";

    /** constant for no exclusive lock */
    protected static final Integer NOT_OWNED = new Integer(0);
    /** Keeps hash of current thread for exclusive lock */
    protected Integer currentThreadHash = NOT_OWNED;

    /** instance for current connection, defaults to EMPTY */
    protected Connection conn = EMPTY_CONNECTION;

    /** portName For Reconnecting to Host */
    protected String portNameForReconnect = null;

    /** secret for authentication with the server */
    protected byte[] netAdapterSecret = null;

    /** if true, the user used a custom secret */
    protected boolean useCustomSecret = false;

    // -------
    // ------- Multicast variables
    // -------

    /** indicates whether or not mulicast is enabled */
    protected Boolean multicastEnabled = null;

    /** The multicast group to use for NetAdapter Datagram packets */
    protected String multicastGroup = null;

    /** The port to use for NetAdapter Datagram packets */
    protected int datagramPort = -1;

    /**
     * Creates an instance of NetAdapter that isn't connected. Must call
     * selectPort(String); or selectPort(Socket);
     */
    public NetAdapter() {
        try {
            resetSecret();
        } catch (Throwable t) {
            setSecret(DEFAULT_SECRET);
        }
    }

    /**
     * Sets the shared secret for authenticating this NetAdapter with
     * a NetAdapterHost.
     *
     * @param secret the new secret for authenticating this client.
     */
    public void setSecret(String secret) {
        if (secret != null) {
            this.netAdapterSecret = secret.getBytes();
        } else
            resetSecret();
    }

    /**
     * Resets the secret to be the default stored in the onewire.properties
     * file (if there is one), or the default as defined by NetAdapterConstants.
     */
    public void resetSecret() {
        String secret = OneWireAccessProvider.getProperty("NetAdapter.Secret");
        if (secret != null)
            this.netAdapterSecret = secret.getBytes();
        else
            this.netAdapterSecret = DEFAULT_SECRET.getBytes();
    }

    /**
     * Checks return value from input stream. Reads one byte. If that
     * byte is not equal to RET_SUCCESS, then it tries to create an
     * appropriate error message. If it is RET_FAILURE, it reads a
     * string representing the error message. If it is neither, it
     * wraps an error message indicating that an unspecified error
     * occurred and attemps a reconnect.
     */
    protected void checkReturnValue(Connection conn) throws IOException, OneWireException, OneWireIOException {
        byte retVal = conn.input.readByte();
        if (retVal != RET_SUCCESS) {
            // an error occurred
            String errorMsg;
            if (retVal == RET_FAILURE) {
                // should be a standard error message after RET_FAILURE
                errorMsg = conn.input.readUTF();
            } else {
                // didn't even get RET_FAILURE
                errorMsg = UNSPECIFIED_ERROR;

                // that probably means we have a major communication error.
                // better to disconnect and reconnect.
                freePort();
                selectPort(portNameForReconnect);
            }

            throw new OneWireIOException(errorMsg);
        }
    }

    /**
     * Sends a ping to the host, just to keep the connection alive. Although
     * it currently is not implemented on the standard NetAdapterHost, this
     * command is used as a signal to the NetAdapterSim to simulate some amount
     * of time that has run.
     */
    public void pingHost() throws OneWireException, OneWireIOException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_PINGCONNECTION);
                conn.output.flush();

                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    // --------
    // -------- Methods
    // --------

    /**
     * Detects adapter presence on the selected port.
     *
     * @return <code>true</code> if the adapter is confirmed to be connected to
     *         the selected port, <code>false</code> if the adapter is not connected.
     *
     * @throws OneWireIOException
     * @throws OneWireException
     */
    @Override
    public boolean adapterDetected() throws OneWireIOException, OneWireException {
        synchronized (conn) {
            return conn != EMPTY_CONNECTION && conn.sock != null;
        }
    }

    /**
     * Retrieves the name of the port adapter as a string. The 'Adapter'
     * is a device that connects to a 'port' that allows one to
     * communicate with an iButton or other 1-Wire device. As example
     * of this is 'DS9097U'.
     *
     * @return <code>String</code> representation of the port adapter.
     */
    @Override
    public String getAdapterName() {
        return "NetAdapter";
    }

    /**
     * Retrieves a description of the port required by this port adapter.
     * An example of a 'Port' would 'serial communication port'.
     *
     * @return <code>String</code> description of the port type required.
     */
    @Override
    public String getPortTypeDescription() {
        return "Network 'Hostname:Port'";
    }

    /**
     * Retrieves a version string for this class.
     *
     * @return version string
     */
    @Override
    public String getClassVersion() {
        return "" + versionUID;
    }

    // --------
    // -------- Port Selection
    // --------

    /**
     * Retrieves a list of the platform appropriate port names for this
     * adapter. A port must be selected with the method 'selectPort'
     * before any other communication methods can be used. Using
     * a communcation method before 'selectPort' will result in
     * a <code>OneWireException</code> exception.
     *
     * @return <code>Enumeration</code> of type <code>String</code> that contains the port
     *         names
     */
    @Override
    public Enumeration getPortNames() {
        Vector v = new Vector();

        // figure out if multicast is enabled
        if (multicastEnabled == null) {
            String enabled = null;
            try {
                enabled = OneWireAccessProvider.getProperty("NetAdapter.MulticastEnabled");
            } catch (Throwable t) {
                ;
            }
            if (enabled != null)
                multicastEnabled = Boolean.valueOf(enabled);
            else
                multicastEnabled = Boolean.FALSE;
        }

        // if multicasting is enabled, we'll look for servers dynamically
        // and add them to the list
        if (multicastEnabled.booleanValue()) {
            // figure out what the datagram listen port is
            if (datagramPort == -1) {
                String strPort = null;
                try {
                    strPort = OneWireAccessProvider.getProperty("NetAdapter.MulticastPort");
                } catch (Throwable t) {
                    ;
                }
                if (strPort == null)
                    datagramPort = DEFAULT_MULTICAST_PORT;
                else
                    datagramPort = Integer.parseInt(strPort);
            }

            // figure out what the multicast group is
            if (multicastGroup == null) {
                String group = null;
                try {
                    group = OneWireAccessProvider.getProperty("NetAdapter.MulticastGroup");
                } catch (Throwable t) {
                    ;
                }
                if (group == null)
                    multicastGroup = DEFAULT_MULTICAST_GROUP;
                else
                    multicastGroup = group;
            }

            MulticastSocket socket = null;
            InetAddress group = null;
            try {
                // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                if (DEBUG) {
                    System.out.println("DEBUG: Opening multicast on port: " + datagramPort);
                    System.out.println("DEBUG: joining group: " + multicastGroup);
                }
                // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

                // create the multi-cast socket
                socket = new MulticastSocket(datagramPort);
                // create the group's InetAddress
                group = InetAddress.getByName(multicastGroup);
                // join the group
                socket.joinGroup(group);

                // convert the versionUID to a byte[]
                byte[] versionBytes = Convert.toByteArray(versionUID);

                // send a packet with the versionUID
                DatagramPacket outPacket = new DatagramPacket(versionBytes, 4, group, datagramPort);
                socket.send(outPacket);

                // set a timeout of 1/2 second for the receive
                socket.setSoTimeout(500);

                byte[] receiveBuffer = new byte[32];
                for (;;) {
                    // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                    if (DEBUG)
                        System.out.println("DEBUG: waiting for multicast packet");
                    // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                    DatagramPacket inPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    socket.receive(inPacket);

                    int length = inPacket.getLength();
                    byte[] data = inPacket.getData();
                    // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                    if (DEBUG) {
                        System.out.println("DEBUG: packet.length=" + length);
                        System.out.println("DEBUG: expecting=" + 5);
                    }
                    // \\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
                    if (length == 5 && data[4] == (byte) 0xFF) {
                        int listenPort = Convert.toInt(data, 0, 4);
                        v.addElement(inPacket.getAddress().getHostName() + ":" + listenPort);
                    }
                }
            } catch (Exception e) {
                /* drain */;
            } finally {
                try {
                    socket.leaveGroup(group);
                    socket.close();
                } catch (Exception e) {
                    /* drain */;
                }
            }
        }

        // get all servers from the properties file
        String server = "";
        try {
            for (int i = 0; server != null; i++) {
                server = OneWireAccessProvider.getProperty("NetAdapter.host" + i);
                if (server != null)
                    v.addElement(server);
            }
        } catch (Throwable t) {
            ;
        }

        return v.elements();
    }

    /**
     * Specifies a platform appropriate port name for this adapter. Note that
     * even though the port has been selected, it's ownership may be relinquished
     * if it is not currently held in a 'exclusive' block. This class will then
     * try to re-aquire the port when needed. If the port cannot be re-aquired
     * ehen the exception <code>PortInUseException</code> will be thrown.
     *
     * @param portName Address to connect this NetAdapter to, in the form of
     *            "hostname:port". For example, "shughes.dalsemi.com:6161", where 6161
     *            is the port number to connect to. The use of NetAdapter.DEFAULT_PORT
     *            is recommended.
     *
     * @return <code>true</code> if the port was aquired, <code>false</code>
     *         if the port is not available.
     *
     * @throws OneWireIOException If port does not exist, or unable to communicate with port.
     * @throws OneWireException If port does not exist
     */
    @Override
    public boolean selectPort(String portName) throws OneWireIOException, OneWireException {
        synchronized (conn) {
            Socket s = null;
            try {
                int port = DEFAULT_PORT;
                // should be of the format "hostname:port" or hostname
                int index = portName.indexOf(':');
                if (index >= 0) {
                    int index2 = portName.indexOf(':', index + 1);
                    if (index2 < 0) // no custom secret specified
                    {
                        port = Integer.parseInt(portName.substring(index + 1));
                        // reset the secret to default
                        resetSecret();
                        useCustomSecret = false;
                    } else {
                        // custom secret is specified
                        setSecret(portName.substring(index2 + 1));
                        useCustomSecret = true;
                        if (index < index2 - 1) // port number is specified
                            port = Integer.parseInt(portName.substring(index + 1, index2));
                    }
                    portName = portName.substring(0, index);
                } else {
                    // reset the secret
                    resetSecret();
                    useCustomSecret = false;
                }
                s = new Socket(portName, port);
            } catch (IOException ioe) {
                throw new OneWireIOException("Can't reach server: " + ioe.getMessage());
            }

            return selectPort(s);
        }
    }

    /**
     * New method, unique to NetAdapter. Sets the "port", i.e. the connection
     * to the server via an already established socket connection.
     *
     * @param sock Socket connection to NetAdapterHost
     *
     * @return <code>true</code> if connection to host was successful
     *
     * @throws OneWireIOException If port does not exist, or unable to communicate with port.
     * @throws OneWireException If port does not exist
     */
    public boolean selectPort(Socket sock) throws OneWireIOException, OneWireException {
        boolean bSuccess = false;
        synchronized (conn) {
            Connection tmpConn = new Connection();
            tmpConn.sock = sock;

            try {
                tmpConn.input = new DataInputStream(sock.getInputStream());
                if (BUFFERED_OUTPUT) {
                    tmpConn.output = new DataOutputStream(new BufferedOutputStream(sock.getOutputStream()));
                } else {
                    tmpConn.output = new DataOutputStream(sock.getOutputStream());
                }

                // check host version
                int hostVersionUID = tmpConn.input.readInt();

                if (hostVersionUID == versionUID) {
                    // tell the server that the versionUID matched
                    tmpConn.output.writeByte(RET_SUCCESS);
                    tmpConn.output.flush();

                    // if the versionUID matches, we need to authenticate ourselves
                    // using the challenge from the server.
                    byte[] chlg = new byte[8];
                    tmpConn.input.read(chlg, 0, 8);

                    // compute the crc of the secret and the challenge
                    int crc = CRC16.compute(netAdapterSecret, 0);
                    crc = CRC16.compute(chlg, crc);
                    // and send it back to the server
                    tmpConn.output.writeInt(crc);
                    tmpConn.output.flush();

                    // check to see if it matched
                    checkReturnValue(tmpConn);

                    bSuccess = true;
                } else {
                    tmpConn.output.writeByte(RET_FAILURE);
                    tmpConn.output.flush();
                    tmpConn = null;
                }
            } catch (IOException e) {
                bSuccess = false;
                tmpConn = null;
            }

            if (bSuccess) {
                portNameForReconnect = sock.getInetAddress().getHostName() + ":" + sock.getPort();
                conn = tmpConn;
            }
        }

        // invalid response or version number
        return bSuccess;
    }

    /**
     * Frees ownership of the selected port, if it is currently owned, back
     * to the system. This should only be called if the recently
     * selected port does not have an adapter, or at the end of
     * your application's use of the port.
     *
     * @throws OneWireException If port does not exist
     */
    @Override
    public void freePort() throws OneWireException {
        try {
            synchronized (conn) {
                conn.output.writeByte(CMD_CLOSECONNECTION);
                conn.output.flush();
                conn.sock.close();
                conn = EMPTY_CONNECTION;
            }
        } catch (Exception e) {
            throw new OneWireException(COMM_FAILED + e.getMessage());
        }
    }

    /**
     * Retrieves the name of the selected port as a <code>String</code>.
     *
     * @return <code>String</code> of selected port
     *
     * @throws OneWireException if valid port not yet selected
     */
    @Override
    public String getPortName() throws OneWireException {
        synchronized (conn) {
            if (!adapterDetected())
                return "Not Connected";
            else if (useCustomSecret)
                return conn.sock.getInetAddress().getHostName() + ":" + conn.sock.getPort() + ":"
                        + new String(this.netAdapterSecret);
            else
                return conn.sock.getInetAddress().getHostName() + ":" + conn.sock.getPort();
        }
    }

    /**
     * Returns whether adapter can physically support overdrive mode.
     *
     * @return <code>true</code> if this port adapter can do OverDrive,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canOverdrive() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANOVERDRIVE);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns whether the adapter can physically support hyperdrive mode.
     *
     * @return <code>true</code> if this port adapter can do HyperDrive,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canHyperdrive() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANHYPERDRIVE);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns whether the adapter can physically support flex speed mode.
     *
     * @return <code>true</code> if this port adapter can do flex speed,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canFlex() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANFLEX);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns whether adapter can physically support 12 volt power mode.
     *
     * @return <code>true</code> if this port adapter can do Program voltage,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canProgram() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANPROGRAM);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns whether the adapter can physically support strong 5 volt power
     * mode.
     *
     * @return <code>true</code> if this port adapter can do strong 5 volt
     *         mode, <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canDeliverPower() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANDELIVERPOWER);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns whether the adapter can physically support "smart" strong 5
     * volt power mode. "smart" power delivery is the ability to deliver
     * power until it is no longer needed. The current drop it detected
     * and power delivery is stopped.
     *
     * @return <code>true</code> if this port adapter can do "smart" strong
     *         5 volt mode, <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canDeliverSmartPower() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANDELIVERSMARTPOWER);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns whether adapter can physically support 0 volt 'break' mode.
     *
     * @return <code>true</code> if this port adapter can do break,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error with the adapter
     * @throws OneWireException on a setup error with the 1-Wire
     *             adapter
     */
    @Override
    public boolean canBreak() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_CANBREAK);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    // --------
    // -------- Finding iButton/1-Wire device options
    // --------

    /**
     * Returns <code>true</code> if the first iButton or 1-Wire device
     * is found on the 1-Wire Network.
     * If no devices are found, then <code>false</code> will be returned.
     *
     * @return <code>true</code> if an iButton or 1-Wire device is found.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean findFirstDevice() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send findFirstDevice command
                conn.output.writeByte(CMD_FINDFIRSTDEVICE);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // return boolean from findFirstDevice
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns <code>true</code> if the next iButton or 1-Wire device
     * is found. The previous 1-Wire device found is used
     * as a starting point in the search. If no more devices are found
     * then <code>false</code> will be returned.
     *
     * @return <code>true</code> if an iButton or 1-Wire device is found.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean findNextDevice() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send findNextDevice command
                conn.output.writeByte(CMD_FINDNEXTDEVICE);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // return boolean from findNextDevice
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Copies the 'current' 1-Wire device address being used by the adapter into
     * the array. This address is the last iButton or 1-Wire device found
     * in a search (findNextDevice()...).
     * This method copies into a user generated array to allow the
     * reuse of the buffer. When searching many iButtons on the one
     * wire network, this will reduce the memory burn rate.
     *
     * @param address An array to be filled with the current iButton address.
     * @see com.dalsemi.onewire.utils.Address
     */
    @Override
    public void getAddress(byte[] address) {
        try {
            synchronized (conn) {
                // send getAddress command
                conn.output.writeByte(CMD_GETADDRESS);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // get the address
                conn.input.read(address, 0, 8);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Sets the 1-Wire Network search to find only iButtons and 1-Wire
     * devices that are in an 'Alarm' state that signals a need for
     * attention. Not all iButton types
     * have this feature. Some that do: DS1994, DS1920, DS2407.
     * This selective searching can be canceled with the
     * 'setSearchAllDevices()' method.
     *
     * @see #setNoResetSearch
     */
    @Override
    public void setSearchOnlyAlarmingDevices() {
        try {
            synchronized (conn) {
                // send setSearchOnlyAlarmingDevices command
                conn.output.writeByte(CMD_SETSEARCHONLYALARMINGDEVICES);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Sets the 1-Wire Network search to not perform a 1-Wire
     * reset before a search. This feature is chiefly used with
     * the DS2409 1-Wire coupler.
     * The normal reset before each search can be restored with the
     * 'setSearchAllDevices()' method.
     */
    @Override
    public void setNoResetSearch() {
        try {
            synchronized (conn) {
                // send setNoResetSearch command
                conn.output.writeByte(CMD_SETNORESETSEARCH);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Sets the 1-Wire Network search to find all iButtons and 1-Wire
     * devices whether they are in an 'Alarm' state or not and
     * restores the default setting of providing a 1-Wire reset
     * command before each search. (see setNoResetSearch() method).
     *
     * @see #setNoResetSearch
     */
    @Override
    public void setSearchAllDevices() {
        try {
            synchronized (conn) {
                // send setSearchAllDevices command
                conn.output.writeByte(CMD_SETSEARCHALLDEVICES);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Removes any selectivity during a search for iButtons or 1-Wire devices
     * by family type. The unique address for each iButton and 1-Wire device
     * contains a family descriptor that indicates the capabilities of the
     * device.
     *
     * @see #targetFamily
     * @see #targetFamily(byte[])
     * @see #excludeFamily
     * @see #excludeFamily(byte[])
     */
    @Override
    public void targetAllFamilies() {
        try {
            synchronized (conn) {
                // send targetAllFamilies command
                conn.output.writeByte(CMD_TARGETALLFAMILIES);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Takes an integer to selectively search for this desired family type.
     * If this method is used, then no devices of other families will be
     * found by any of the search methods.
     *
     * @param family the code of the family type to target for searches
     * @see com.dalsemi.onewire.utils.Address
     * @see #targetAllFamilies
     */
    @Override
    public void targetFamily(int family) {
        try {
            synchronized (conn) {
                // send targetFamily command
                conn.output.writeByte(CMD_TARGETFAMILY);
                conn.output.writeInt(1);
                conn.output.writeByte((byte) family);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Takes an array of bytes to use for selectively searching for acceptable
     * family codes. If used, only devices with family codes in this array
     * will be found by any of the search methods.
     *
     * @param family array of the family types to target for searches
     * @see com.dalsemi.onewire.utils.Address
     * @see #targetAllFamilies
     */
    @Override
    public void targetFamily(byte family[]) {
        try {
            synchronized (conn) {
                // send targetFamily command
                conn.output.writeByte(CMD_TARGETFAMILY);
                conn.output.writeInt(family.length);
                conn.output.write(family, 0, family.length);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Takes an integer family code to avoid when searching for iButtons.
     * or 1-Wire devices.
     * If this method is used, then no devices of this family will be
     * found by any of the search methods.
     *
     * @param family the code of the family type NOT to target in searches
     * @see com.dalsemi.onewire.utils.Address
     * @see #targetAllFamilies
     */
    @Override
    public void excludeFamily(int family) {
        try {
            synchronized (conn) {
                // send excludeFamily command
                conn.output.writeByte(CMD_EXCLUDEFAMILY);
                conn.output.writeInt(1);
                conn.output.writeByte((byte) family);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    /**
     * Takes an array of bytes containing family codes to avoid when finding
     * iButtons or 1-Wire devices. If used, then no devices with family
     * codes in this array will be found by any of the search methods.
     *
     * @param family array of family cods NOT to target for searches
     * @see com.dalsemi.onewire.utils.Address
     * @see #targetAllFamilies
     */
    @Override
    public void excludeFamily(byte family[]) {
        try {
            synchronized (conn) {
                // send excludeFamily command
                conn.output.writeByte(CMD_EXCLUDEFAMILY);
                conn.output.writeInt(family.length);
                conn.output.write(family, 0, family.length);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (Exception e) {
            /* drain */ }
    }

    // --------
    // -------- 1-Wire Network Semaphore methods
    // --------

    /**
     * Gets exclusive use of the 1-Wire to communicate with an iButton or
     * 1-Wire Device.
     * This method should be used for critical sections of code where a
     * sequence of commands must not be interrupted by communication of
     * threads with other iButtons, and it is permissible to sustain
     * a delay in the special case that another thread has already been
     * granted exclusive access and this access has not yet been
     * relinquished.
     * <p>
     *
     * It can be called through the OneWireContainer
     * class by the end application if they want to ensure exclusive
     * use. If it is not called around several methods then it
     * will be called inside each method.
     *
     * @param blocking <code>true</code> if want to block waiting
     *            for an excluse access to the adapter
     * @return <code>true</code> if blocking was false and a
     *         exclusive session with the adapter was aquired
     *
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean beginExclusive(boolean blocking) throws OneWireException {
        boolean bGotLocalBlock = false, bGotServerBlock = false;
        if (blocking) {
            while (!beginExclusive()) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }

            bGotLocalBlock = true;
        } else
            bGotLocalBlock = beginExclusive();

        try {
            synchronized (conn) {
                // send beginExclusive command
                conn.output.writeByte(CMD_BEGINEXCLUSIVE);
                conn.output.writeBoolean(blocking);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from beginExclusive
                bGotServerBlock = conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }

        // if blocking, I shouldn't get here unless both are true
        return bGotLocalBlock && bGotServerBlock;
    }

    /**
     * Gets exclusive use of the 1-Wire to communicate with an iButton or
     * 1-Wire Device.
     * This method should be used for critical sections of code where a
     * sequence of commands must not be interrupted by communication of
     * threads with other iButtons, and it is permissible to sustain
     * a delay in the special case that another thread has already been
     * granted exclusive access and this access has not yet been
     * relinquished. This is private and non blocking
     * <p>
     *
     * @return <code>true</code> a exclusive session with the adapter was
     *         aquired
     *
     * @throws OneWireException
     */
    private boolean beginExclusive() throws OneWireException {
        synchronized (currentThreadHash) {
            if (currentThreadHash == NOT_OWNED) {
                // not owned so take
                currentThreadHash = new Integer(Thread.currentThread().hashCode());

                // provided debug on standard out
                if (DEBUG) {
                    System.out.println("beginExclusive, now owned by: " + Thread.currentThread().getName());
                }

                return true;
            } else if (currentThreadHash.intValue() == Thread.currentThread().hashCode()) {
                // provided debug on standard out
                if (DEBUG) {
                    System.out.println("beginExclusive, already owned by: " + Thread.currentThread().getName());
                }

                // already own
                return true;
            } else {
                // want port but don't own
                return false;
            }
        }
    }

    /**
     * Relinquishes exclusive control of the 1-Wire Network.
     * This command dynamically marks the end of a critical section and
     * should be used when exclusive control is no longer needed.
     */
    @Override
    public void endExclusive() {
        synchronized (currentThreadHash) {
            // if own then release
            if (currentThreadHash != NOT_OWNED && currentThreadHash.intValue() == Thread.currentThread().hashCode()) {
                if (DEBUG) {
                    System.out.println("endExclusive, was owned by: " + Thread.currentThread().getName());
                }

                currentThreadHash = NOT_OWNED;
                try {
                    synchronized (conn) {
                        // send endExclusive command
                        conn.output.writeByte(CMD_ENDEXCLUSIVE);
                        conn.output.flush();

                        // check return value for success
                        checkReturnValue(conn);
                    }
                } catch (Exception e) {
                    /* drain */ }
            }
        }
    }

    // --------
    // -------- Primitive 1-Wire Network data methods
    // --------

    /**
     * Sends a Reset to the 1-Wire Network.
     *
     * @return the result of the reset. Potential results are:
     *         <ul>
     *         <li>0 (RESET_NOPRESENCE) no devices present on the 1-Wire Network.
     *         <li>1 (RESET_PRESENCE) normal presence pulse detected on the 1-Wire
     *         Network indicating there is a device present.
     *         <li>2 (RESET_ALARM) alarming presence pulse detected on the 1-Wire
     *         Network indicating there is a device present and it is in the
     *         alarm condition. This is only provided by the DS1994/DS2404
     *         devices.
     *         <li>3 (RESET_SHORT) inticates 1-Wire appears shorted. This can be
     *         transient conditions in a 1-Wire Network. Not all adapter types
     *         can detect this condition.
     *         </ul>
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public int reset() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send reset command
                conn.output.writeByte(CMD_RESET);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from reset
                return conn.input.readInt();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sends a bit to the 1-Wire Network.
     *
     * @param bitValue the bit value to send to the 1-Wire Network.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void putBit(boolean bitValue) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send putBit command
                conn.output.writeByte(CMD_PUTBIT);
                // followed by the bit
                conn.output.writeBoolean(bitValue);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Gets a bit from the 1-Wire Network.
     *
     * @return the bit value recieved from the the 1-Wire Network.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean getBit() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send getBit command
                conn.output.writeByte(CMD_GETBIT);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from getBit
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sends a byte to the 1-Wire Network.
     *
     * @param byteValue the byte value to send to the 1-Wire Network.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void putByte(int byteValue) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send putByte command
                conn.output.writeByte(CMD_PUTBYTE);
                // followed by the byte
                conn.output.writeByte(byteValue);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Gets a byte from the 1-Wire Network.
     *
     * @return the byte value received from the the 1-Wire Network.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public int getByte() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send getByte command
                conn.output.writeByte(CMD_GETBYTE);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next parameter should be the return from getByte
                return conn.input.readByte() & 0x0FF;
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Gets a block of data from the 1-Wire Network.
     *
     * @param len length of data bytes to receive
     *
     * @return the data received from the 1-Wire Network.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public byte[] getBlock(int len) throws OneWireIOException, OneWireException {
        byte[] buffer = new byte[len];
        getBlock(buffer, 0, len);
        return buffer;
    }

    /**
     * Gets a block of data from the 1-Wire Network and write it into
     * the provided array.
     *
     * @param arr array in which to write the received bytes
     * @param len length of data bytes to receive
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void getBlock(byte[] arr, int len) throws OneWireIOException, OneWireException {
        getBlock(arr, 0, len);
    }

    /**
     * Gets a block of data from the 1-Wire Network and write it into
     * the provided array.
     *
     * @param arr array in which to write the received bytes
     * @param off offset into the array to start
     * @param len length of data bytes to receive
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void getBlock(byte[] arr, int off, int len) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send getBlock command
                conn.output.writeByte(CMD_GETBLOCK);
                // followed by the number of bytes to get
                conn.output.writeInt(len);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next should be the bytes
                conn.input.readFully(arr, off, len);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sends a block of data and returns the data received in the same array.
     * This method is used when sending a block that contains reads and writes.
     * The 'read' portions of the data block need to be pre-loaded with 0xFF's.
     * It starts sending data from the index at offset 'off' for length 'len'.
     *
     * @param dataBlock array of data to transfer to and from the 1-Wire Network.
     * @param off offset into the array of data to start
     * @param len length of data to send / receive starting at 'off'
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void dataBlock(byte[] dataBlock, int off, int len) throws OneWireIOException, OneWireException {
        if (DEBUG) {
            System.out.println("DataBlock called for " + len + " bytes");
        }
        try {
            synchronized (conn) {
                // send dataBlock command
                conn.output.writeByte(CMD_DATABLOCK);
                // followed by the number of bytes to block
                conn.output.writeInt(len);
                // followed by the bytes
                conn.output.write(dataBlock, off, len);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // next should be the bytes returned
                conn.input.readFully(dataBlock, off, len);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
        if (DEBUG) {
            System.out.println("   Done DataBlocking");
        }
    }

    // --------
    // -------- 1-Wire Network power methods
    // --------

    /**
     * Sets the duration to supply power to the 1-Wire Network.
     * This method takes a time parameter that indicates the program
     * pulse length when the method startPowerDelivery().
     * <p>
     *
     * Note: to avoid getting an exception,
     * use the canDeliverPower() and canDeliverSmartPower()
     * method to check it's availability.
     * <p>
     *
     * @param timeFactor
     *            <ul>
     *            <li>0 (DELIVERY_HALF_SECOND) provide power for 1/2 second.
     *            <li>1 (DELIVERY_ONE_SECOND) provide power for 1 second.
     *            <li>2 (DELIVERY_TWO_SECONDS) provide power for 2 seconds.
     *            <li>3 (DELIVERY_FOUR_SECONDS) provide power for 4 seconds.
     *            <li>4 (DELIVERY_SMART_DONE) provide power until the
     *            the device is no longer drawing significant power.
     *            <li>5 (DELIVERY_INFINITE) provide power until the
     *            setPowerNormal() method is called.
     *            </ul>
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void setPowerDuration(int timeFactor) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send setPowerDuration command
                conn.output.writeByte(CMD_SETPOWERDURATION);
                // followed by the timeFactor
                conn.output.writeInt(timeFactor);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sets the 1-Wire Network voltage to supply power to a 1-Wire device.
     * This method takes a time parameter that indicates whether the
     * power delivery should be done immediately, or after certain
     * conditions have been met.
     * <p>
     *
     * Note: to avoid getting an exception,
     * use the canDeliverPower() and canDeliverSmartPower()
     * method to check it's availability.
     * <p>
     *
     * @param changeCondition
     *            <ul>
     *            <li>0 (CONDITION_NOW) operation should occur immediately.
     *            <li>1 (CONDITION_AFTER_BIT) operation should be pending
     *            execution immediately after the next bit is sent.
     *            <li>2 (CONDITION_AFTER_BYTE) operation should be pending
     *            execution immediately after next byte is sent.
     *            </ul>
     *
     * @return <code>true</code> if the voltage change was successful,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public boolean startPowerDelivery(int changeCondition) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send startPowerDelivery command
                conn.output.writeByte(CMD_STARTPOWERDELIVERY);
                // followed by the changeCondition
                conn.output.writeInt(changeCondition);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // and get the return value from startPowerDelivery
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sets the duration for providing a program pulse on the
     * 1-Wire Network.
     * This method takes a time parameter that indicates the program
     * pulse length when the method startProgramPulse().
     * <p>
     *
     * Note: to avoid getting an exception,
     * use the canDeliverPower() method to check it's
     * availability.
     * <p>
     *
     * @param timeFactor
     *            <ul>
     *            <li>7 (DELIVERY_EPROM) provide program pulse for 480 microseconds
     *            <li>5 (DELIVERY_INFINITE) provide power until the
     *            setPowerNormal() method is called.
     *            </ul>
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     */
    @Override
    public void setProgramPulseDuration(int timeFactor) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send setProgramPulseDuration command
                conn.output.writeByte(CMD_SETPROGRAMPULSEDURATION);
                // followed by the timeFactor
                conn.output.writeInt(timeFactor);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sets the 1-Wire Network voltage to eprom programming level.
     * This method takes a time parameter that indicates whether the
     * power delivery should be done immediately, or after certain
     * conditions have been met.
     * <p>
     *
     * Note: to avoid getting an exception,
     * use the canProgram() method to check it's
     * availability.
     * <p>
     *
     * @param changeCondition
     *            <ul>
     *            <li>0 (CONDITION_NOW) operation should occur immediately.
     *            <li>1 (CONDITION_AFTER_BIT) operation should be pending
     *            execution immediately after the next bit is sent.
     *            <li>2 (CONDITION_AFTER_BYTE) operation should be pending
     *            execution immediately after next byte is sent.
     *            </ul>
     *
     * @return <code>true</code> if the voltage change was successful,
     *         <code>false</code> otherwise.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     *             or the adapter does not support this operation
     */
    @Override
    public boolean startProgramPulse(int changeCondition) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send startProgramPulse command
                conn.output.writeByte(CMD_STARTPROGRAMPULSE);
                // followed by the changeCondition
                conn.output.writeInt(changeCondition);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // and get the return value from startPowerDelivery
                return conn.input.readBoolean();
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Sets the 1-Wire Network voltage to 0 volts. This method is used
     * rob all 1-Wire Network devices of parasite power delivery to force
     * them into a hard reset.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     *             or the adapter does not support this operation
     */
    @Override
    public void startBreak() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send startBreak command
                conn.output.writeByte(CMD_STARTBREAK);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED);
        }
    }

    /**
     * Sets the 1-Wire Network voltage to normal level. This method is used
     * to disable 1-Wire conditions created by startPowerDelivery and
     * startProgramPulse. This method will automatically be called if
     * a communication method is called while an outstanding power
     * command is taking place.
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     *             or the adapter does not support this operation
     */
    @Override
    public void setPowerNormal() throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send startBreak command
                conn.output.writeByte(CMD_SETPOWERNORMAL);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    // --------
    // -------- 1-Wire Network speed methods
    // --------

    /**
     * Sets the new speed of data
     * transfer on the 1-Wire Network.
     * <p>
     *
     * @param speed
     *            <ul>
     *            <li>0 (SPEED_REGULAR) set to normal communciation speed
     *            <li>1 (SPEED_FLEX) set to flexible communciation speed used
     *            for long lines
     *            <li>2 (SPEED_OVERDRIVE) set to normal communciation speed to
     *            overdrive
     *            <li>3 (SPEED_HYPERDRIVE) set to normal communciation speed to
     *            hyperdrive
     *            <li>>3 future speeds
     *            </ul>
     *
     * @throws OneWireIOException on a 1-Wire communication error
     * @throws OneWireException on a setup error with the 1-Wire adapter
     *             or the adapter does not support this operation
     */
    @Override
    public void setSpeed(int speed) throws OneWireIOException, OneWireException {
        try {
            synchronized (conn) {
                // send startBreak command
                conn.output.writeByte(CMD_SETSPEED);
                // followed by the speed
                conn.output.writeInt(speed);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);
            }
        } catch (IOException ioe) {
            throw new OneWireException(COMM_FAILED + ioe.getMessage());
        }
    }

    /**
     * Returns the current data transfer speed on the 1-Wire Network.
     * <p>
     *
     * @return <code>int</code> representing the current 1-Wire speed
     *         <ul>
     *         <li>0 (SPEED_REGULAR) set to normal communication speed
     *         <li>1 (SPEED_FLEX) set to flexible communication speed used
     *         for long lines
     *         <li>2 (SPEED_OVERDRIVE) set to normal communication speed to
     *         overdrive
     *         <li>3 (SPEED_HYPERDRIVE) set to normal communication speed to
     *         hyperdrive
     *         <li>>3 future speeds
     *         </ul>
     */
    @Override
    public int getSpeed() {
        try {
            synchronized (conn) {
                // send startBreak command
                conn.output.writeByte(CMD_GETSPEED);
                conn.output.flush();

                // check return value for success
                checkReturnValue(conn);

                // and return the return value from getSpeed()
                return conn.input.readInt();
            }
        } catch (Exception e) {
            /* drain */
        }

        return -1;
    }
}