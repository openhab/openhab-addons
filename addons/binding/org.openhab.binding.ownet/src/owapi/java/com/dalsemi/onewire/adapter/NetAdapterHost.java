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

import java.io.*;
import java.net.*;
import java.util.*;

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.utils.*;

/**
 * <P>NetAdapterHost is the host (or server) component for a network-based
 * DSPortAdapter.  It actually wraps the hardware DSPortAdapter and handles
 * connections from outside sources (NetAdapter) who want to access it.</P>
 *
 * <P>NetAdapterHost is designed to be run in a thread, waiting for incoming
 * connections.  You can run this in the same thread as your main program or
 * you can establish the connections yourself (presumably using some higher
 * level of security) and then call the <code>handleConnection(Socket)</code>
 * {@see #handleConnection(Socket)}.</P>
 *
 * <P>Once a NetAdapter is connected with the host, a version check is performed
 * followed by a simple authentication step.  The authentication is dependent
 * upon a secret shared between the NetAdapter and the host.  Both will use
 * a default value, that each will agree with if you don't provide a secret
 * of your own.  To set the secret, add the following line to your
 * onewire.properties file:
 * <ul>
 *    <li>NetAdapter.secret="This is my custom secret"</li>
 * </ul>
 * Optionally, the secret can be set by calling the <code>setSecret(String)</code>
 * {@see #setSecret(String)}</P>
 *
 * <P>The NetAdapter and NetAdapterHost support multicast broadcasts for
 * automatic discovery of compatible servers on your LAN.  To start the
 * multicast listener for this NetAdapterHost, call the
 * <code>createMulticastListener()</code> method
 * {@see #createMulticastListener()}.</P>
 *
 * <P>For information on creating the client component, see the JavaDocs
 * for the  {@link com.dalsemi.onewire.adapter.NetAdapter NetAdapter}.
 *
 * @see NetAdapter
 *
 * @author SH
 * @version    1.00, 9 Jan 2002
 */
public class NetAdapterHost
   implements Runnable, NetAdapterConstants
{
   /** random number generator, used to issue challenges to client */
   protected static final Random rand = new Random();

   /** The adapter this NetAdapter will proxy too */
   protected DSPortAdapter adapter = null;

   /** The server socket for listening for connections */
   protected ServerSocket serverSocket = null;

   /** secret for authentication with the server */
   protected byte[] netAdapterSecret = null;

   /** boolean flags for stopping the host */
   protected volatile boolean hostStopped = false, hostRunning = false;

   /** boolean flag to indicate whether or not the host is single or multi-threaded */
   protected boolean singleThreaded = true;

   /** Map of all Service threads created, only for multi-threaded */
   protected Hashtable hashHandlers = null;

   /** Optional, listens for datagram packets from potential clients */
   protected MulticastListener multicastListener = null;

   /** timeout for socket receive, in seconds */
   protected int timeoutInSeconds = 30;

   /**
    * <P>Creates an instance of a NetAdapterHost which wraps the provided
    * adapter.  The host listens on the default port as specified by
    * NetAdapterConstants.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterHost will proxy
    * commands to.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterHost(DSPortAdapter adapter)
      throws IOException
   {
      this(adapter, DEFAULT_PORT, false);
   }

   /**
    * <P>Creates a single-threaded instance of a NetAdapterHost which wraps the
    * provided adapter.  The host listens on the specified port.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterHost will proxy
    * commands to.
    * @param listenPort the TCP/IP port to listen on for incoming connections
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterHost(DSPortAdapter adapter, int listenPort)
      throws IOException
   {
      this(adapter, listenPort, false);
   }

   /**
    * <P>Creates an (optionally multithreaded) instance of a NetAdapterHost
    * which wraps the provided adapter.  The listen port is set to the
    * default port as defined in NetAdapterConstants.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterHost will proxy
    * commands to.
    * @param multiThread if true, multiple TCP/IP connections are allowed
    * to interact simulataneously with this adapter.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterHost(DSPortAdapter adapter, boolean multiThread)
      throws IOException
   {
      this(adapter, DEFAULT_PORT, multiThread);
   }

   /**
    * <P>Creates an (optionally multi-threaded) instance of a NetAdapterHost which
    * wraps the provided adapter.  The host listens on the specified port.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterHost will proxy
    * commands to.
    * @param listenPort the TCP/IP port to listen on for incoming connections
    * @param multiThread if true, multiple TCP/IP connections are allowed
    * to interact simulataneously with this adapter.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterHost(DSPortAdapter adapter, int listenPort,
                         boolean multiThread)
      throws IOException
   {
      //save reference to adapter
      this.adapter = adapter;

      // create the server socket
      this.serverSocket = new ServerSocket(listenPort);

      // set multithreaded flag
      this.singleThreaded = !multiThread;
      if(multiThread)
      {
         this.hashHandlers = new Hashtable();
         this.timeoutInSeconds = 0;
      }

      // get the shared secret
      String secret = OneWireAccessProvider.getProperty("NetAdapter.secret");
      if(secret!=null)
         netAdapterSecret = secret.getBytes();
      else
         netAdapterSecret = DEFAULT_SECRET.getBytes();
   }

   /**
    * <P>Creates an instance of a NetAdapterHost which wraps the provided
    * adapter.  The host listens on the default port as specified by
    * NetAdapterConstants.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterHost will proxy
    * commands to.
    * @param serverSock the ServerSocket for incoming connections
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterHost(DSPortAdapter adapter, ServerSocket serverSock)
      throws IOException
   {
      this(adapter, serverSock, false);
   }

   /**
    * <P>Creates an (optionally multi-threaded) instance of a NetAdapterHost which
    * wraps the provided adapter.  The host listens on the specified port.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterHost will proxy
    * commands to.
    * @param serverSock the ServerSocket for incoming connections
    * @param multiThread if true, multiple TCP/IP connections are allowed
    * to interact simulataneously with this adapter.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterHost(DSPortAdapter adapter, ServerSocket serverSock,
                         boolean multiThread)
      throws IOException
   {
      //save reference to adapter
      this.adapter = adapter;

      // create the server socket
      this.serverSocket = serverSock;

      // set multithreaded flag
      this.singleThreaded = !multiThread;
      if(multiThread)
      {
         this.hashHandlers = new Hashtable();
         this.timeoutInSeconds = 0;
      }

      // get the shared secret
      String secret = OneWireAccessProvider.getProperty("NetAdapter.secret");
      if(secret!=null)
         netAdapterSecret = secret.getBytes();
      else
         netAdapterSecret = DEFAULT_SECRET.getBytes();
   }

   /**
    * Sets the secret used for authenticating incoming client connections.
    *
    * @param secret The shared secret information used for authenticating
    *               incoming client connections.
    */
   public void setSecret(String secret)
   {
      netAdapterSecret = secret.getBytes();
   }

   /**
    * Creates a Multicast Listener to allow NetAdapter clients to discover
    * this NetAdapterHost automatically.  Uses defaults for Multicast group
    * and port.
    */
   public void createMulticastListener()
      throws IOException,UnknownHostException
   {
      createMulticastListener(DEFAULT_MULTICAST_PORT);
   }

   /**
    * Creates a Multicast Listener to allow NetAdapter clients to discover
    * this NetAdapterHost automatically.  Uses default for Multicast group.
    *
    * @param port The port the Multicast socket will receive packets on
    */
   public void createMulticastListener(int port)
      throws IOException,UnknownHostException
   {
      String group
         = OneWireAccessProvider.getProperty("NetAdapter.MulticastGroup");
      if(group==null)
         group = DEFAULT_MULTICAST_GROUP;
      createMulticastListener(port, group);
   }

   /**
    * Creates a Multicast Listener to allow NetAdapter clients to discover
    * this NetAdapterHost automatically.
    *
    * @param port The port the Multicast socket will receive packets on
    * @param group The group the Multicast socket will join
    */
   public void createMulticastListener(int port, String group)
      throws IOException,UnknownHostException
   {
      if(multicastListener==null)
      {
         // 4 bytes for integer versionUID
         byte[] versionBytes = Convert.toByteArray(versionUID);

         // this byte array is 5 because length is used to determine different
         // packet types by client
         byte[] listenPortBytes = new byte[5];
         Convert.toByteArray(serverSocket.getLocalPort(),
                             listenPortBytes, 0, 4);
         listenPortBytes[4] = (byte)0x0FF;

         multicastListener = new MulticastListener(port, group,
                                           versionBytes, listenPortBytes);
         (new Thread(multicastListener)).start();
      }
   }


   /**
    * Run method for threaded NetAdapterHost.  Maintains server socket which
    * waits for incoming connections.  Whenever a connection is received
    * launches it services the socket or (optionally) launches a new thread
    * for servicing the socket.
    */
   public void run()
   {
      hostRunning = true;
      while(!hostStopped)
      {
         Socket sock = null;
         try
         {
            sock = serverSocket.accept();
            handleConnection(sock);
         }
         catch(IOException ioe1)
         {
            try
            {
               if(sock!=null)
                  sock.close();
            }
            catch(IOException ioe2)
            {;}
         }
      }
      hostRunning = false;
   }

   /**
    * Handles a socket connection.  If single-threaded, the connection is
    * serviced in the current thread.  If multi-threaded, a new thread is
    * created for servicing this connection.
    */
   public void handleConnection(Socket sock)
       throws IOException
   {
      SocketHandler sh = new SocketHandler(sock);
      if(singleThreaded)
      {
         // single-threaded
         sh.run();
      }
      else
      {
         // multi-threaded
         Thread t = new Thread(sh);
         t.start();
         synchronized(hashHandlers)
         {
            hashHandlers.put(t, sh);
         }
      }
   }
   /**
    * Stops all threads and kills the server socket.
    */
   public void stopHost()
   {
      this.hostStopped = true;
      try
      {
         this.serverSocket.close();
      }
      catch(IOException ioe)
      {;}

      // wait for run method to quit, with a timeout of 1 second
      int i = 0;
      while(hostRunning && i++<100)
         try{Thread.sleep(10);}catch(Exception e){;}

      if(!singleThreaded)
      {
         synchronized(hashHandlers)
         {
            Enumeration e = hashHandlers.elements();
            while(e.hasMoreElements())
               ((SocketHandler)e.nextElement()).stopHandler();
         }
      }

      if(multicastListener!=null)
         multicastListener.stopListener();

      // ensure that there is no exclusive use of the adapter
      adapter.endExclusive();
   }

   /**
    * Transmits the versionUID of the current NetAdapter protocol to
    * the client connection.  If it matches the clients versionUID,
    * the client returns RET_SUCCESS.
    *
    * @param conn The connection to send/receive data.
    * @return <code>true</code> if the versionUID matched.
    */
   private boolean sendVersionUID(Connection conn)
      throws IOException
   {
      // write server version
      conn.output.writeInt(versionUID);
      conn.output.flush();

      byte retVal = conn.input.readByte();

      return (retVal==RET_SUCCESS);
   }

   /**
    * Reads in command from client and calls the appropriate handler function.
    *
    * @param conn The connection to send/receive data.
    *
    */
   private void processRequests(Connection conn)
      throws IOException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
         System.out.println("\n------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      // get the next command
      byte cmd = 0x00;

      cmd = conn.input.readByte();

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
         System.out.println("CMD received: " + Integer.toHexString(cmd));
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      try
      {
         // ... and fire the appropriate method
         switch(cmd)
         {
            /* Connection keep-alive and close commands */
            case CMD_PINGCONNECTION:
               // no-op, might update timer of some sort later
               conn.output.writeByte(RET_SUCCESS);
               conn.output.flush();
               break;
            case CMD_CLOSECONNECTION:
               close(conn);
               break;
            /* Raw Data commands */
            case CMD_RESET:
               adapterReset(conn);
               break;
            case CMD_PUTBIT:
               adapterPutBit(conn);
               break;
            case CMD_PUTBYTE:
               adapterPutByte(conn);
               break;
            case CMD_GETBIT:
               adapterGetBit(conn);
               break;
            case CMD_GETBYTE:
               adapterGetByte(conn);
               break;
            case CMD_GETBLOCK:
               adapterGetBlock(conn);
               break;
            case CMD_DATABLOCK:
               adapterDataBlock(conn);
               break;
            /* Power methods */
            case CMD_SETPOWERDURATION:
               adapterSetPowerDuration(conn);
               break;
            case CMD_STARTPOWERDELIVERY:
               adapterStartPowerDelivery(conn);
               break;
            case CMD_SETPROGRAMPULSEDURATION:
               adapterSetProgramPulseDuration(conn);
               break;
            case CMD_STARTPROGRAMPULSE:
               adapterStartProgramPulse(conn);
               break;
            case CMD_STARTBREAK:
               adapterStartBreak(conn);
               break;
            case CMD_SETPOWERNORMAL:
               adapterSetPowerNormal(conn);
               break;
            /* Speed methods */
            case CMD_SETSPEED:
               adapterSetSpeed(conn);
               break;
            case CMD_GETSPEED:
               adapterGetSpeed(conn);
               break;
            /* Network Semaphore methods */
            case CMD_BEGINEXCLUSIVE:
               adapterBeginExclusive(conn);
               break;
            case CMD_ENDEXCLUSIVE:
               adapterEndExclusive(conn);
               break;
            /* Searching methods */
            case CMD_FINDFIRSTDEVICE:
               adapterFindFirstDevice(conn);
               break;
            case CMD_FINDNEXTDEVICE:
               adapterFindNextDevice(conn);
               break;
            case CMD_GETADDRESS:
               adapterGetAddress(conn);
               break;
            case CMD_SETSEARCHONLYALARMINGDEVICES:
               adapterSetSearchOnlyAlarmingDevices(conn);
               break;
            case CMD_SETNORESETSEARCH:
               adapterSetNoResetSearch(conn);
               break;
            case CMD_SETSEARCHALLDEVICES:
               adapterSetSearchAllDevices(conn);
               break;
            case CMD_TARGETALLFAMILIES:
               adapterTargetAllFamilies(conn);
               break;
            case CMD_TARGETFAMILY:
               adapterTargetFamily(conn);
               break;
            case CMD_EXCLUDEFAMILY:
               adapterExcludeFamily(conn);
               break;
            /* feature methods */
            case CMD_CANBREAK:
               adapterCanBreak(conn);
               break;
            case CMD_CANDELIVERPOWER:
               adapterCanDeliverPower(conn);
               break;
            case CMD_CANDELIVERSMARTPOWER:
               adapterCanDeliverSmartPower(conn);
               break;
            case CMD_CANFLEX:
               adapterCanFlex(conn);
               break;
            case CMD_CANHYPERDRIVE:
               adapterCanHyperdrive(conn);
               break;
            case CMD_CANOVERDRIVE:
               adapterCanOverdrive(conn);
               break;
            case CMD_CANPROGRAM:
               adapterCanProgram(conn);
               break;
            default:
               //System.out.println("Unkown command: " + cmd);
               break;
         }
      }
      catch(OneWireException owe)
      {
         conn.output.writeByte(RET_FAILURE);
         conn.output.writeUTF(owe.toString());
         conn.output.flush();
      }
   }

   /**
    * Closes the provided connection.
    *
    * @param conn The connection to send/receive data.
    */
   private void close(Connection conn)
   {
      try
      {
         if(conn.sock!=null)
         {
            conn.sock.close();
         }
      }
      catch(IOException ioe)
      { /*drain*/; }

      conn.sock = null;
      conn.input = null;
      conn.output = null;

      // ensure that there is no exclusive use of the adapter
      adapter.endExclusive();
   }

   //--------
   //-------- Finding iButton/1-Wire device options
   //--------

   private void adapterFindFirstDevice (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.findFirstDevice();

      if(DEBUG)
      {
         System.out.println("   findFirstDevice returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterFindNextDevice (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.findNextDevice();

      if(DEBUG)
      {
         System.out.println("   findNextDevice returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterGetAddress (Connection conn)
      throws IOException
   {
      // read in the address
      byte[] address = new byte[8];
      // call getAddress
      adapter.getAddress(address);

      if(DEBUG)
      {
         System.out.println("   adapter.getAddress(byte[]) called, speed=" + adapter.getSpeed());
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.write(address, 0, 8);
      conn.output.flush();
   }

   private void adapterSetSearchOnlyAlarmingDevices (Connection conn)
      throws IOException
   {
      if(DEBUG)
      {
         System.out.println("   setSearchOnlyAlarmingDevices called, speed=" + adapter.getSpeed());
      }

      adapter.setSearchOnlyAlarmingDevices();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterSetNoResetSearch (Connection conn)
      throws IOException
   {
      if(DEBUG)
      {
         System.out.println("   setNoResetSearch called, speed=" + adapter.getSpeed());
      }

      adapter.setNoResetSearch();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterSetSearchAllDevices (Connection conn)
      throws IOException
   {
      if(DEBUG)
      {
         System.out.println("   setSearchAllDevices called, speed=" + adapter.getSpeed());
      }

      adapter.setSearchAllDevices();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterTargetAllFamilies (Connection conn)
      throws IOException
   {
      if(DEBUG)
      {
         System.out.println("   targetAllFamilies called, speed=" + adapter.getSpeed());
      }

      adapter.targetAllFamilies();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterTargetFamily (Connection conn)
      throws IOException
   {
      // get the number of family codes to expect
      int len = conn.input.readInt();
      // get the family codes
      byte[] family = new byte[len];
      conn.input.readFully(family, 0, len);

      if(DEBUG)
      {
         System.out.println("   targetFamily called, speed=" + adapter.getSpeed());
         System.out.println("      families: " + Convert.toHexString(family));
      }

      // call targetFamily
      adapter.targetFamily(family);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterExcludeFamily (Connection conn)
      throws IOException
   {
      // get the number of family codes to expect
      int len = conn.input.readInt();
      // get the family codes
      byte[] family = new byte[len];
      conn.input.readFully(family, 0, len);

      if(DEBUG)
      {
         System.out.println("   excludeFamily called, speed=" + adapter.getSpeed());
         System.out.println("      families: " + Convert.toHexString(family));
      }

      // call excludeFamily
      adapter.excludeFamily(family);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   //--------
   //-------- 1-Wire Network Semaphore methods
   //--------

   private void adapterBeginExclusive(Connection conn)
      throws IOException, OneWireException
   {
      if(DEBUG)
      {
         System.out.println("   adapter.beginExclusive called, speed=" + adapter.getSpeed());
      }

      // get blocking boolean
      boolean blocking = conn.input.readBoolean();
      // call beginExclusive
      boolean b = adapter.beginExclusive(blocking);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();

      if(DEBUG)
      {
         System.out.println("      adapter.beginExclusive returned " + b);
      }
   }

   private void adapterEndExclusive(Connection conn)
      throws IOException, OneWireException
   {
      if(DEBUG)
      {
         System.out.println("   adapter.endExclusive called, speed=" + adapter.getSpeed());
      }

      // call endExclusive
      adapter.endExclusive();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   //--------
   //-------- Primitive 1-Wire Network data methods
   //--------

   private void adapterReset(Connection conn)
      throws IOException, OneWireException
   {
      int i = adapter.reset();

      if(DEBUG)
      {
         System.out.println("   reset, speed=" + adapter.getSpeed() + ", returned " + i);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeInt(i);
      conn.output.flush();
   }

   private void adapterPutBit(Connection conn)
      throws IOException, OneWireException
   {
      // get the value of the bit
      boolean bit = conn.input.readBoolean();

      if(DEBUG)
      {
         System.out.println("   putBit called, speed=" + adapter.getSpeed());
         System.out.println("      bit=" + bit);
      }

      adapter.putBit(bit);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterPutByte(Connection conn)
      throws IOException, OneWireException
   {
      // get the value of the byte
      byte b = conn.input.readByte();

      if(DEBUG)
      {
         System.out.println("   putByte called, speed=" + adapter.getSpeed());
         System.out.println("      byte=" + Convert.toHexString(b));
      }

      adapter.putByte(b);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterGetBit(Connection conn)
      throws IOException, OneWireException
   {
      boolean bit = adapter.getBit();

      if(DEBUG)
      {
         System.out.println("   getBit called, speed=" + adapter.getSpeed());
         System.out.println("      bit=" + bit);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(bit);
      conn.output.flush();
   }

   private void adapterGetByte(Connection conn)
      throws IOException, OneWireException
   {
      int b = adapter.getByte();

      if(DEBUG)
      {
         System.out.println("   getByte called, speed=" + adapter.getSpeed());
         System.out.println("      byte=" + Convert.toHexString((byte)b));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeByte(b);
      conn.output.flush();
   }
   private void adapterGetBlock(Connection conn)
      throws IOException, OneWireException
   {
      // get the number requested
      int len = conn.input.readInt();
      if(DEBUG)
      {
         System.out.println("   getBlock called, speed=" + adapter.getSpeed());
         System.out.println("      len=" + len);
      }

      // get the bytes
      byte[] b = adapter.getBlock(len);

      if(DEBUG)
      {
         System.out.println("      returned: " + Convert.toHexString(b));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.write(b, 0, len);
      conn.output.flush();
   }

   private void adapterDataBlock(Connection conn)
      throws IOException, OneWireException
   {
      if(DEBUG)
      {
         System.out.println("   DataBlock called, speed=" + adapter.getSpeed());
      }
      // get the number to block
      int len = conn.input.readInt();
      // get the bytes to block
      byte[] b = new byte[len];
      conn.input.readFully(b, 0, len);

      if(DEBUG)
      {
         System.out.println("      " + len + " bytes");
         System.out.println("      Send: " + Convert.toHexString(b));
      }

      // do the block
      adapter.dataBlock(b, 0, len);

      if(DEBUG)
      {
         System.out.println("      Recv: " + Convert.toHexString(b));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.write(b, 0, len);
      conn.output.flush();
   }

   //--------
   //-------- 1-Wire Network power methods
   //--------

   private void adapterSetPowerDuration(Connection conn)
      throws IOException, OneWireException
   {
      // get the time factor value
      int timeFactor = conn.input.readInt();

      if(DEBUG)
      {
         System.out.println("   setPowerDuration called, speed=" + adapter.getSpeed());
         System.out.println("      timeFactor=" + timeFactor);
      }

      // call setPowerDuration
      adapter.setPowerDuration(timeFactor);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterStartPowerDelivery(Connection conn)
      throws IOException, OneWireException
   {
      // get the change condition value
      int changeCondition = conn.input.readInt();

      if(DEBUG)
      {
         System.out.println("   startPowerDelivery called, speed=" + adapter.getSpeed());
         System.out.println("      changeCondition=" + changeCondition);
      }

      // call startPowerDelivery
      boolean success = adapter.startPowerDelivery(changeCondition);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(success);
      conn.output.flush();
   }

   private void adapterSetProgramPulseDuration(Connection conn)
      throws IOException, OneWireException
   {
      // get the time factor value
      int timeFactor = conn.input.readInt();

      if(DEBUG)
      {
         System.out.println("   setProgramPulseDuration called, speed=" + adapter.getSpeed());
         System.out.println("      timeFactor=" + timeFactor);
      }

      // call setProgramPulseDuration
      adapter.setProgramPulseDuration(timeFactor);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterStartProgramPulse(Connection conn)
      throws IOException, OneWireException
   {
      // get the change condition value
      int changeCondition = conn.input.readInt();

      if(DEBUG)
      {
         System.out.println("   startProgramPulse called, speed=" + adapter.getSpeed());
         System.out.println("      changeCondition=" + changeCondition);
      }

      // call startProgramPulse();
      boolean success = adapter.startProgramPulse(changeCondition);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(success);
      conn.output.flush();
   }

   private void adapterStartBreak(Connection conn)
      throws IOException, OneWireException
   {
      if(DEBUG)
      {
         System.out.println("   startBreak called, speed=" + adapter.getSpeed());
      }

      // call startBreak();
      adapter.startBreak();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterSetPowerNormal(Connection conn)
      throws IOException, OneWireException
   {
      if(DEBUG)
      {
         System.out.println("   setPowerNormal called, speed=" + adapter.getSpeed());
      }

      // call setPowerNormal
      adapter.setPowerNormal();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   //--------
   //-------- 1-Wire Network speed methods
   //--------

   private void adapterSetSpeed(Connection conn)
      throws IOException, OneWireException
   {
      // get the value of the new speed
      int speed = conn.input.readInt();

      if(DEBUG)
      {
         System.out.println("   setSpeed called, speed=" + adapter.getSpeed());
         System.out.println("      speed=" + speed);
      }

      // do the setSpeed
      adapter.setSpeed(speed);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterGetSpeed(Connection conn)
      throws IOException, OneWireException
   {
      // get the adapter speed
      int speed = adapter.getSpeed();

      if(DEBUG)
      {
         System.out.println("   getSpeed called, speed=" + adapter.getSpeed());
         System.out.println("      speed=" + speed);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeInt(speed);
      conn.output.flush();
   }


   //--------
   //-------- Adapter feature methods
   //--------

   private void adapterCanOverdrive (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canOverdrive();

      if(DEBUG)
      {
         System.out.println("   canOverdrive returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterCanHyperdrive (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canHyperdrive();

      if(DEBUG)
      {
         System.out.println("   canHyperDrive returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterCanFlex (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canFlex();

      if(DEBUG)
      {
         System.out.println("   canFlex returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterCanProgram (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canProgram();

      if(DEBUG)
      {
         System.out.println("   canProgram returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterCanDeliverPower (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canDeliverPower();

      if(DEBUG)
      {
         System.out.println("   canDeliverPower returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterCanDeliverSmartPower (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canDeliverSmartPower();

      if(DEBUG)
      {
         System.out.println("   canDeliverSmartPower returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterCanBreak (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = adapter.canBreak();

      if(DEBUG)
      {
         System.out.println("   canBreak returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   //--------
   //-------- Inner classes
   //--------

   /**
    * Private inner class for servicing new connections.
    * Can be run in it's own thread or in the same thread.
    */
   private class SocketHandler implements Runnable
   {
      /**
       * The connection that is being serviced.
       */
      private Connection conn;

      /**
       * indicates whether or not the handler is currently running
       */
      private volatile boolean handlerRunning = false;

      /**
       * Constructor for socket servicer.  Creates the input and output
       * streams and send's the version of this host to the client
       * connection.
       */
      public SocketHandler(Socket sock)
         throws IOException
      {
         // set socket timeout to 10 seconds
         sock.setSoTimeout(timeoutInSeconds*1000);

         // create the connection object
         conn = new Connection();
         conn.sock = sock;
         conn.input = new DataInputStream(conn.sock.getInputStream());
         if(BUFFERED_OUTPUT)
         {
            conn.output = new DataOutputStream(new BufferedOutputStream(
                                                 conn.sock.getOutputStream()));
         }
         else
         {
            conn.output = new DataOutputStream(conn.sock.getOutputStream());
         }

         // first thing transmitted should be version info
         if(!sendVersionUID(conn))
         {
            throw new IOException("send version failed");
         }

         // authenticate the client
         byte[] chlg = new byte[8];
         rand.nextBytes(chlg);
         conn.output.write(chlg);
         conn.output.flush();

         // compute the crc of the secret and the challenge
         int crc = CRC16.compute(netAdapterSecret, 0);
         crc = CRC16.compute(chlg, crc);
         int answer = conn.input.readInt();
         if(answer!=crc)
         {
            conn.output.writeByte(RET_FAILURE);
            conn.output.writeUTF("Client Authentication Failed");
            conn.output.flush();
            throw new IOException("authentication failed");
         }
         else
         {
            conn.output.writeByte(RET_SUCCESS);
            conn.output.flush();
         }
      }

      /**
       * Run method for socket Servicer.
       */
      public void run()
      {
         handlerRunning = true;
         try
         {
            while(!hostStopped && conn.sock!=null)
            {
               processRequests(conn);
            }
         }
         catch(Throwable t)
         {
            if(DEBUG)
               t.printStackTrace();
            close(conn);
         }
         handlerRunning = false;

         if(!hostStopped && !singleThreaded)
         {
            synchronized(hashHandlers)
            {
               // thread finished running without being stopped.
               // politely remove it from the hashtable.
               hashHandlers.remove(Thread.currentThread());
            }
         }
      }

      /**
       * Waits for handler to finish, with a timeout.
       */
      public void stopHandler()
      {
         int i = 0;
         int timeout = 3000;
         while(handlerRunning && i++<timeout)
            try{Thread.sleep(10);}catch(Exception e){;}
      }
   }

   //--------
   //-------- Default Main Method, for launching server with defaults
   //--------
   /**
    * A Default Main Method, for launching NetAdapterHost getting the
    * default adapter with the OneWireAccessProvider and listening on
    * the default port specified by DEFAULT_PORT.
    */
   public static void main(String[] args)
      throws Exception
   {
      DSPortAdapter adapter
         = com.dalsemi.onewire.OneWireAccessProvider.getDefaultAdapter();

      NetAdapterHost host = new NetAdapterHost(adapter, true);

      System.out.println("Starting Multicast Listener");
      host.createMulticastListener();

      System.out.println("Starting NetAdapter Host");
      (new Thread(host)).start();

      //if(System.in!=null)
      //{
      //   System.out.println("\nPress Enter to Shutdown");
      //   (new BufferedReader(new InputStreamReader(System.in))).readLine();
      //   host.stopHost();
      //   System.exit(1);
      //}
   }
}
