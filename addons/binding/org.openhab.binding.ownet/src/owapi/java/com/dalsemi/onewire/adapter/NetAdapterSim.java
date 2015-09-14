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
 * <P>NetAdapterSim is the host (or server) component for a network-based
 * DSPortAdapter.  It actually wraps the hardware DSPortAdapter and handles
 * connections from outside sources (NetAdapter) who want to access it.</P>
 *
 * <P>NetAdapterSim is designed to be run in a thread, waiting for incoming
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
 * <P>The NetAdapter and NetAdapterSim support multicast broadcasts for
 * automatic discovery of compatible servers on your LAN.  To start the
 * multicast listener for this NetAdapterSim, call the
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
public class NetAdapterSim
   implements Runnable, NetAdapterConstants
{
   protected static boolean SIM_DEBUG = false;

   /** random number generator, used to issue challenges to client */
   protected static final Random rand = new Random();

   /** Log file */
   protected PrintWriter logFile;

   /** exec command, command string to start the simulator */
   protected String execCommand;

   protected Process process;
   protected BufferedReader processOutput;
   protected BufferedReader processError;
   protected OutputStreamWriter processInput;

   /** fake address, returned from all search or getAddress commands */
   protected byte[] fakeAddress = null;

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
    * <P>Creates an instance of a NetAdapterSim which wraps the provided
    * adapter.  The host listens on the default port as specified by
    * NetAdapterConstants.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterSim will proxy
    * commands to.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterSim(String execCmd, String logFilename)
      throws IOException
   {
      this(execCmd, logFilename, DEFAULT_PORT, false);
   }

   /**
    * <P>Creates a single-threaded instance of a NetAdapterSim which wraps the
    * provided adapter.  The host listens on the specified port.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterSim will proxy
    * commands to.
    * @param listenPort the TCP/IP port to listen on for incoming connections
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterSim(String execCmd, byte[] fakeAddress, String logFile,
                        int listenPort)
      throws IOException
   {
      this(execCmd, logFile, listenPort, false);
   }

   /**
    * <P>Creates an (optionally multithreaded) instance of a NetAdapterSim
    * which wraps the provided adapter.  The listen port is set to the
    * default port as defined in NetAdapterConstants.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterSim will proxy
    * commands to.
    * @param multiThread if true, multiple TCP/IP connections are allowed
    * to interact simulataneously with this adapter.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterSim(String execCmd, String logFilename,
                        boolean multiThread)
      throws IOException
   {
      this(execCmd, logFilename, DEFAULT_PORT, multiThread);
   }

   /**
    * <P>Creates an (optionally multi-threaded) instance of a NetAdapterSim which
    * wraps the provided adapter.  The host listens on the specified port.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterSim will proxy
    * commands to.
    * @param listenPort the TCP/IP port to listen on for incoming connections
    * @param multiThread if true, multiple TCP/IP connections are allowed
    * to interact simulataneously with this adapter.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterSim(String execCmd, String logFilename,
                        int listenPort, boolean multiThread)
      throws IOException
   {
      // save references to file and command
      this.execCommand = execCmd;
      this.process = Runtime.getRuntime().exec(execCmd);
      this.processOutput = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
      this.processError = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
      this.processInput = new OutputStreamWriter(this.process.getOutputStream());

      // wait until process is ready
      int complete = 0;
      while(complete<2)
      {
         String line = processOutput.readLine();
         if(complete==0 && line.indexOf("read ok (data=17)")>=0)
         {
            complete++;
            continue;
         }
         if(complete==1 && line.indexOf(PROMPT)>=0)
         {
            complete++;
            continue;
         }
      }

      if(logFilename!=null)
         this.logFile = new PrintWriter(new FileWriter(logFilename), true);

       // Make sure we loaded the address of the device
      simulationGetAddress();

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
    * <P>Creates an instance of a NetAdapterSim which wraps the provided
    * adapter.  The host listens on the default port as specified by
    * NetAdapterConstants.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterSim will proxy
    * commands to.
    * @param serverSock the ServerSocket for incoming connections
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterSim(String execCmd, String logFilename,
                        ServerSocket serverSock)
      throws IOException
   {
      this(execCmd, logFilename, serverSock, false);
   }

   /**
    * <P>Creates an (optionally multi-threaded) instance of a NetAdapterSim which
    * wraps the provided adapter.  The host listens on the specified port.</P>
    *
    * <P>Note that the secret used for authentication is the value specified
    * in the onewire.properties file as "NetAdapter.secret=mySecret".
    * To set the secret to another value, use the
    * <code>setSecret(String)</code> method.</P>
    *
    * @param adapter DSPortAdapter that this NetAdapterSim will proxy
    * commands to.
    * @param serverSock the ServerSocket for incoming connections
    * @param multiThread if true, multiple TCP/IP connections are allowed
    * to interact simulataneously with this adapter.
    *
    * @throws IOException if a network error occurs or the listen socket
    * cannot be created on the specified port.
    */
   public NetAdapterSim(String execCmd, String logFilename,
                        ServerSocket serverSock, boolean multiThread)
      throws IOException
   {
      // save references to file and command
      this.execCommand = execCmd;
      this.process = Runtime.getRuntime().exec(execCmd);
      this.processOutput = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
      this.processError = new BufferedReader(new InputStreamReader(this.process.getErrorStream()));
      this.processInput = new OutputStreamWriter(this.process.getOutputStream());

      // wait  until process is ready
      int complete = 0;
      while(complete<2)
      {
         String line = processOutput.readLine();
         if(complete==0 && line.indexOf("read ok (data=17)")>=0)
         {
            complete++;
            continue;
         }
         if(complete==1 && line.indexOf(PROMPT)>=0)
         {
            complete++;
            continue;
         }
      }

      if(logFilename!=null)
         this.logFile = new PrintWriter(new FileWriter(logFilename), true);

       // Make sure we loaded the address of the device
      simulationGetAddress();

      // save reference to the server socket
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
    * this NetAdapterSim automatically.  Uses defaults for Multicast group
    * and port.
    */
   public void createMulticastListener()
      throws IOException,UnknownHostException
   {
      createMulticastListener(DEFAULT_MULTICAST_PORT);
   }

   /**
    * Creates a Multicast Listener to allow NetAdapter clients to discover
    * this NetAdapterSim automatically.  Uses default for Multicast group.
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
    * this NetAdapterSim automatically.
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
    * Run method for threaded NetAdapterSim.  Maintains server socket which
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
            // reset time of last command, so we don't simulate a bunch of
            // unneccessary time
            timeOfLastCommand = System.currentTimeMillis();
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
      //adapter.endExclusive();
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

   protected long timeOfLastCommand = 0;
   protected static final long IGNORE_TIME_MIN = 2;
   protected static final long IGNORE_TIME_MAX = 1000;
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
      if(logFile!=null)
         logFile.println("\n------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      // get the next command
      byte cmd = 0x00;

      cmd = conn.input.readByte();

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(logFile!=null)
         logFile.println("CMD received: " + Integer.toHexString(cmd));
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      if(cmd == CMD_PINGCONNECTION)
      {
         // no-op, might update timer of some sort later
         simulationPing(1000);
         conn.output.writeByte(RET_SUCCESS);
         conn.output.flush();
      }
      else
      {
         long timeDelta = System.currentTimeMillis() - timeOfLastCommand;
         if(SIM_DEBUG && logFile!=null)
            logFile.println("general: timeDelta=" + timeDelta);
         if(timeDelta>=IGNORE_TIME_MIN && timeDelta<=IGNORE_TIME_MAX)
         {
            // do something with timeDelta
            simulationPing(timeDelta);
         }

         try
         {
            // ... and fire the appropriate method
            switch(cmd)
            {
               /* Connection keep-alive and close commands */
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
                  if(SIM_DEBUG && logFile!=null)
                     logFile.println("Unkown command received: " + cmd);
                  break;
            }
         }
         catch(OneWireException owe)
         {
            if(SIM_DEBUG && logFile!=null)
               logFile.println("Exception: " + owe.toString());
            conn.output.writeByte(RET_FAILURE);
            conn.output.writeUTF(owe.toString());
            conn.output.flush();
         }
         timeOfLastCommand = System.currentTimeMillis();
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
      //adapter.endExclusive();
   }

   //--------
   //-------- Finding iButton/1-Wire device options
   //--------

   private void adapterFindFirstDevice (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = true;//adapter.findFirstDevice();

      if(logFile!=null)
      {
         logFile.println("   findFirstDevice returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterFindNextDevice (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = false;//adapter.findNextDevice();

      if(logFile!=null)
      {
         logFile.println("   findNextDevice returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   private void adapterGetAddress (Connection conn)
      throws IOException
   {
      if(logFile!=null)
      {
         logFile.println("   adapter.getAddress(byte[]) called");
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.write(fakeAddress, 0, 8);
      conn.output.flush();
   }

   private void adapterSetSearchOnlyAlarmingDevices (Connection conn)
      throws IOException
   {
      if(logFile!=null)
      {
         logFile.println("   setSearchOnlyAlarmingDevices called");
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterSetNoResetSearch (Connection conn)
      throws IOException
   {
      if(logFile!=null)
      {
         logFile.println("   setNoResetSearch called");
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterSetSearchAllDevices (Connection conn)
      throws IOException
   {
      if(logFile!=null)
      {
         logFile.println("   setSearchAllDevices called");
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterTargetAllFamilies (Connection conn)
      throws IOException
   {
      if(logFile!=null)
      {
         logFile.println("   targetAllFamilies called");
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   // TODO
   private void adapterTargetFamily (Connection conn)
      throws IOException
   {
      // get the number of family codes to expect
      int len = conn.input.readInt();
      // get the family codes
      byte[] family = new byte[len];
      conn.input.readFully(family, 0, len);

      if(logFile!=null)
      {
         logFile.println("   targetFamily called");
         logFile.println("      families: " + Convert.toHexString(family));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   // TODO
   private void adapterExcludeFamily (Connection conn)
      throws IOException
   {
      // get the number of family codes to expect
      int len = conn.input.readInt();
      // get the family codes
      byte[] family = new byte[len];
      conn.input.readFully(family, 0, len);

      if(logFile!=null)
      {
         logFile.println("   excludeFamily called");
         logFile.println("      families: " + Convert.toHexString(family));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   //--------
   //-------- 1-Wire Network Semaphore methods
   //--------

   // TODO
   private void adapterBeginExclusive(Connection conn)
      throws IOException, OneWireException
   {
      if(logFile!=null)
      {
         logFile.println("   adapter.beginExclusive called");
      }

      // get blocking boolean
      //boolean blocking = 
         conn.input.readBoolean();
      // call beginExclusive
      boolean b = true;

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();

      if(logFile!=null)
      {
         logFile.println("      adapter.beginExclusive returned " + b);
      }
   }

   // TODO
   private void adapterEndExclusive(Connection conn)
      throws IOException, OneWireException
   {
      if(logFile!=null)
      {
         logFile.println("   adapter.endExclusive called");
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   //--------
   //-------- Primitive 1-Wire Network data methods
   //--------

   private void adapterReset(Connection conn)
      throws IOException, OneWireException
   {
      int i = 1;// return 1 for presence pulse

      if(logFile!=null)
      {
         logFile.println("   reset returned " + i);
      }

      simulationReset();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeInt(i);
      conn.output.flush();
   }

   //TODO
   private void adapterPutBit(Connection conn)
      throws IOException, OneWireException
   {
      // get the value of the bit
      boolean bit = conn.input.readBoolean();

      if(logFile!=null)
      {
         logFile.println("   putBit called");
         logFile.println("      bit=" + bit);
      }

      simulationPutBit(bit);
      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterPutByte(Connection conn)
      throws IOException, OneWireException
   {
      // get the value of the byte
      byte b = conn.input.readByte();

      if(logFile!=null)
      {
         logFile.println("   putByte called");
         logFile.println("      byte=" + Convert.toHexString(b));
      }

      simulationPutByte(b);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   private void adapterGetBit(Connection conn)
      throws IOException, OneWireException
   {
      boolean bit = simulationGetBit();

      if(logFile!=null)
      {
         logFile.println("   getBit called");
         logFile.println("      bit=" + bit);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(bit);
      conn.output.flush();
   }

   private void adapterGetByte(Connection conn)
      throws IOException, OneWireException
   {
      int b = simulationGetByte();

      if(logFile!=null)
      {
         logFile.println("   getByte called");
         logFile.println("      byte=" + Convert.toHexString((byte)b));
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
      if(logFile!=null)
      {
         logFile.println("   getBlock called");
         logFile.println("      len=" + len);
      }

      // get the bytes
      byte[] b = new byte[len];
      for(int i=0; i<len; i++)
      {
         b[i] = simulationGetByte();
      }

      if(logFile!=null)
      {
         logFile.println("      returned: " + Convert.toHexString(b));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.write(b, 0, len);
      conn.output.flush();
   }

   private void adapterDataBlock(Connection conn)
      throws IOException, OneWireException
   {
      if(logFile!=null)
      {
         logFile.println("   DataBlock called");
      }
      // get the number to block
      int len = conn.input.readInt();
      // get the bytes to block
      byte[] b = new byte[len];
      conn.input.readFully(b, 0, len);

      if(logFile!=null)
      {
         logFile.println("      " + len + " bytes");
         logFile.println("      Send: " + Convert.toHexString(b));
      }

      // do the block
      for(int i=0; i<len; i++)
      {
         if(b[i]==(byte)0x0FF)
         {
            b[i] = simulationGetByte();
         }
         else
         {
            simulationPutByte(b[i]);
         }
      }

      if(logFile!=null)
      {
         logFile.println("      Recv: " + Convert.toHexString(b));
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.write(b, 0, len);
      conn.output.flush();
   }

   //--------
   //-------- 1-Wire Network power methods
   //--------

   // TODO
   private void adapterSetPowerDuration(Connection conn)
      throws IOException, OneWireException
   {
      // get the time factor value
      int timeFactor = conn.input.readInt();

      if(logFile!=null)
      {
         logFile.println("   setPowerDuration called");
         logFile.println("      timeFactor=" + timeFactor);
      }

      // call setPowerDuration
      //adapter.setPowerDuration(timeFactor);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   // TODO
   private void adapterStartPowerDelivery(Connection conn)
      throws IOException, OneWireException
   {
      // get the change condition value
      int changeCondition = conn.input.readInt();

      if(logFile!=null)
      {
         logFile.println("   startPowerDelivery called");
         logFile.println("      changeCondition=" + changeCondition);
      }

      // call startPowerDelivery
      boolean success = true;//adapter.startPowerDelivery(changeCondition);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(success);
      conn.output.flush();
   }

   // TODO
   private void adapterSetProgramPulseDuration(Connection conn)
      throws IOException, OneWireException
   {
      // get the time factor value
      int timeFactor = conn.input.readInt();

      if(logFile!=null)
      {
         logFile.println("   setProgramPulseDuration called");
         logFile.println("      timeFactor=" + timeFactor);
      }

      // call setProgramPulseDuration
      //adapter.setProgramPulseDuration(timeFactor);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   // TODO
   private void adapterStartProgramPulse(Connection conn)
      throws IOException, OneWireException
   {
      // get the change condition value
      int changeCondition = conn.input.readInt();

      if(logFile!=null)
      {
         logFile.println("   startProgramPulse called");
         logFile.println("      changeCondition=" + changeCondition);
      }

      // call startProgramPulse();
      boolean success = true;//adapter.startProgramPulse(changeCondition);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(success);
      conn.output.flush();
   }

   // TODO
   private void adapterStartBreak(Connection conn)
      throws IOException, OneWireException
   {
      if(logFile!=null)
      {
         logFile.println("   startBreak called");
      }

      // call startBreak();
      //adapter.startBreak();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   // TODO
   private void adapterSetPowerNormal(Connection conn)
      throws IOException, OneWireException
   {
      if(logFile!=null)
      {
         logFile.println("   setPowerNormal called");
      }

      // call setPowerNormal
      //adapter.setPowerNormal();

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   //--------
   //-------- 1-Wire Network speed methods
   //--------

   // TODO
   private void adapterSetSpeed(Connection conn)
      throws IOException, OneWireException
   {
      // get the value of the new speed
      int speed = conn.input.readInt();

      if(logFile!=null)
      {
         logFile.println("   setSpeed called");
         logFile.println("      speed=" + speed);
      }

      // do the setSpeed
      //adapter.setSpeed(speed);

      conn.output.writeByte(RET_SUCCESS);
      conn.output.flush();
   }

   // TODO
   private void adapterGetSpeed(Connection conn)
      throws IOException, OneWireException
   {
      // get the adapter speed
      int speed = 0;//adapter.getSpeed();

      if(logFile!=null)
      {
         logFile.println("   getSpeed called");
         logFile.println("      speed=" + speed);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeInt(speed);
      conn.output.flush();
   }


   //--------
   //-------- Adapter feature methods
   //--------

   // TODO
   private void adapterCanOverdrive (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = false;//adapter.canOverdrive();

      if(logFile!=null)
      {
         logFile.println("   canOverdrive returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   // TODO
   private void adapterCanHyperdrive (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = false;//adapter.canHyperdrive();

      if(logFile!=null)
      {
         logFile.println("   canHyperDrive returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   // TODO
   private void adapterCanFlex (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = false;//adapter.canFlex();

      if(logFile!=null)
      {
         logFile.println("   canFlex returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   // TODO
   private void adapterCanProgram (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = true;//adapter.canProgram();

      if(logFile!=null)
      {
         logFile.println("   canProgram returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   // TODO
   private void adapterCanDeliverPower (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = true;//adapter.canDeliverPower();

      if(logFile!=null)
      {
         logFile.println("   canDeliverPower returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   // TODO
   private void adapterCanDeliverSmartPower (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = true;//adapter.canDeliverSmartPower();

      if(logFile!=null)
      {
         logFile.println("   canDeliverSmartPower returned " + b);
      }

      conn.output.writeByte(RET_SUCCESS);
      conn.output.writeBoolean(b);
      conn.output.flush();
   }

   // TODO
   private void adapterCanBreak (Connection conn)
      throws IOException, OneWireException
   {
      boolean b = true;//adapter.canBreak();

      if(logFile!=null)
      {
         logFile.println("   canBreak returned " + b);
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
            if(logFile!=null)
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

   // -----------------------------------------------------------------------
   // Simulation Methods
   //
   // -----------------------------------------------------------------------

   private static final String OW_RESET_RESULT = "onewire reset at time";
   private static final String OW_RESET_CMD = "task tb.xow_master.ow_reset";
   private static final int OW_RESET_RUN_LENGTH = 1000000;

   private static final String OW_WRITE_BYTE_ARG = "deposit tb.xow_master.ow_write_byte.data = 8'h"; // ie 8'hFF
   private static final String OW_WRITE_BYTE_CMD = "task tb.xow_master.ow_write_byte";
   private static final int OW_WRITE_BYTE_RUN_LENGTH = 520000;

   private static final String OW_READ_RESULT = "(data=";
   private static final String OW_READ_BYTE_CMD = "task tb.xow_master.ow_read_byte";
   private static final int OW_READ_BYTE_RUN_LENGTH = 632009;

   private static final String OW_READ_SLOT_CMD = "task tb.xow_master.ow_read_slot";
   private static final int OW_READ_SLOT_RUN_LENGTH = 80000;

   private static final String OW_WRITE_ZERO_CMD = "task tb.xow_master.ow_write0";
   private static final int OW_WRITE_ZERO_RUN_LENGTH = 80000;

   private static final String OW_WRITE_ONE_CMD = "task tb.xow_master.ow_write1";
   private static final int OW_WRITE_ONE_RUN_LENGTH = 80000;

   private static final String GENERIC_CMD_END = "Ran until";


   private static final long PING_MS_RUN_LENGTH = 1000000L;

   private static final String RUN = "run ";
   private static final String LINE_DELIM = "\r\n";
   private static final String PROMPT = "ncsim> ";

   private void simulationReset()
      throws IOException
   {
      if(SIM_DEBUG && logFile!=null)
      {
         logFile.println("reset: Writing=" + OW_RESET_CMD);
         logFile.println("reset: Writing=" + RUN + OW_RESET_RUN_LENGTH);
      }
      processInput.write(OW_RESET_CMD + LINE_DELIM);
      processInput.write(RUN + OW_RESET_RUN_LENGTH + LINE_DELIM);
      processInput.flush();

      // wait for it to complete
      int complete = 0;
      while(complete<2)
      {
         String line = processOutput.readLine();
         if(SIM_DEBUG && logFile!=null)
            logFile.println("reset: complete=" + complete + ", read=" + line);
         if(complete==0 && line.indexOf(OW_RESET_RESULT)>=0)
         {
            complete++;
            continue;
         }
         if(complete==1 && line.indexOf(GENERIC_CMD_END)>=0)
         {
            complete++;
            continue;
         }
      }
      if(SIM_DEBUG && logFile!=null)
         logFile.println("reset: Complete");
   }

   private boolean simulationGetBit()
      throws java.io.IOException
   {
      boolean bit = true;

      if(SIM_DEBUG && logFile!=null)
      {
         logFile.println("getBit: Writing=" + OW_READ_SLOT_CMD);
         logFile.println("getBit: Writing=" + RUN + OW_READ_SLOT_RUN_LENGTH);
      }
      processInput.write(OW_READ_SLOT_CMD + LINE_DELIM);
      processInput.write(RUN + OW_READ_SLOT_RUN_LENGTH + LINE_DELIM);
      processInput.flush();

      // wait for it to complete
      int complete = 0;
      while(complete<3)
      {
         String line = processOutput.readLine();
         if(SIM_DEBUG && logFile!=null)
            logFile.println("getBit: complete=" + complete + ", read=" + line);
         if(complete==0 && line.indexOf("OW = 1'b0")>=0)
         {
            complete++;
            continue;
         }
         if(complete==1 && line.indexOf("OW = 1'b0")>=0)
         {
            bit = false;
            complete++;
            continue;
         }
         if(complete==1 && line.indexOf("OW = 1'b1")>=0)
         {
            bit = true;
            complete++;
            continue;
         }
         if(complete==2 && line.indexOf(GENERIC_CMD_END)>=0)
         {
            complete++;
            continue;
         }
      }
      if(SIM_DEBUG && logFile!=null)
         logFile.println("getBit: Complete");
      return bit;
   }

   private byte simulationGetByte()
      throws java.io.IOException
   {
      byte bits = 0;

      if(SIM_DEBUG && logFile!=null)
      {
         logFile.println("getByte: Writing=" + OW_READ_BYTE_CMD);
         logFile.println("getByte: Writing=" + RUN + OW_READ_BYTE_RUN_LENGTH);
      }
      processInput.write(OW_READ_BYTE_CMD + LINE_DELIM);
      processInput.write(RUN + OW_READ_BYTE_RUN_LENGTH + LINE_DELIM);
      processInput.flush();

      // wait for it to complete
      try
      {
         int complete = 0;
         while(complete<2)
         {
            String line = processOutput.readLine();
            if(SIM_DEBUG && logFile!=null)
               logFile.println("getByte: complete=" + complete + ", read=" + line);
            if(complete==0 && line.indexOf(OW_READ_RESULT)>=0)
            {
               int i = line.indexOf(OW_READ_RESULT) + OW_READ_RESULT.length();
               String bitstr = line.substring(i, i+2);
               if(SIM_DEBUG && logFile!=null)
                  logFile.println("getByte: bitstr=" + bitstr);
               bits = (byte)(Convert.toInt(bitstr)&0x0FF);
               complete++;
               continue;
            }
            if(complete==1 && line.indexOf(GENERIC_CMD_END)>=0)
            {
               complete++;
               continue;
            }
         }
         if(SIM_DEBUG && logFile!=null)
            logFile.println("getByte: complete");
      }
      catch(Convert.ConvertException ce)
      {
         if(SIM_DEBUG && logFile!=null)
            logFile.println("Error during hex string conversion: " + ce);
      }
      return bits;
   }

   private void simulationPutBit(boolean bit)
      throws java.io.IOException
   {
      if(bit)
      {
         if(SIM_DEBUG && logFile!=null)
         {
            logFile.println("putBit: Writing=" + OW_WRITE_ONE_CMD);
            logFile.println("putBit: Writing=" + RUN + OW_WRITE_ONE_RUN_LENGTH);
         }
         processInput.write(OW_WRITE_ONE_CMD + LINE_DELIM);
         processInput.write(RUN + OW_WRITE_ONE_RUN_LENGTH + LINE_DELIM);
      }
      else
      {
         if(SIM_DEBUG && logFile!=null)
         {
            logFile.println("putBit: Writing=" + OW_WRITE_ZERO_CMD);
            logFile.println("putBit: Writing=" + RUN + OW_WRITE_ZERO_RUN_LENGTH);
         }
         processInput.write(OW_WRITE_ZERO_CMD + LINE_DELIM);
         processInput.write(RUN + OW_WRITE_ZERO_RUN_LENGTH + LINE_DELIM);
      }
      processInput.flush();

      // wait for it to complete
      int complete = 0;
      while(complete<1)
      {
         String line = processOutput.readLine();
         if(SIM_DEBUG && logFile!=null)
            logFile.println("putBit: complete=" + complete + ", read=" + line);
         if(complete==0 && line.indexOf(GENERIC_CMD_END)>=0)
         {
            complete++;
            continue;
         }
      }
      if(SIM_DEBUG && logFile!=null)
         logFile.println("putBit: complete");
   }

   private void simulationPutByte(byte b)
      throws IOException
   {
      if(SIM_DEBUG && logFile!=null)
      {
         logFile.println("putByte: Writing=" + OW_WRITE_BYTE_ARG + Convert.toHexString(b));
         logFile.println("putByte: Writing=" + OW_WRITE_BYTE_CMD);
         logFile.println("putByte: Writing=" + RUN + OW_WRITE_BYTE_RUN_LENGTH);
      }
      processInput.write(OW_WRITE_BYTE_ARG + Convert.toHexString(b) + LINE_DELIM);
      processInput.write(OW_WRITE_BYTE_CMD + LINE_DELIM);
      processInput.write(RUN + OW_WRITE_BYTE_RUN_LENGTH + LINE_DELIM);
      processInput.flush();

      // wait for it to complete
      int complete = 0;
      while(complete<1)
      {
         String line = processOutput.readLine();
         if(SIM_DEBUG && logFile!=null)
            logFile.println("putByte: complete=" + complete + ", read=" + line);
         if(complete==0 && line.indexOf(GENERIC_CMD_END)>=0)
         {
            complete++;
            continue;
         }
      }
      if(SIM_DEBUG && logFile!=null)
         logFile.println("putByte: complete");
   }

   private void simulationPing(long timeDelta)
      throws IOException
   {
      if(SIM_DEBUG && logFile!=null)
      {
         logFile.println("ping: timeDelta=" + timeDelta);
         logFile.println("ping: Writing=" + RUN + (PING_MS_RUN_LENGTH*timeDelta));
      }
      processInput.write(RUN + (PING_MS_RUN_LENGTH*timeDelta) + LINE_DELIM);
      processInput.flush();

      // wait for it to complete
      int complete = 0;
      while(complete<1)
      {
         String line = processOutput.readLine();
         if(SIM_DEBUG && logFile!=null)
            logFile.println("ping: complete=" + complete + ", read=" + line);
         if(complete==0 && line.indexOf(GENERIC_CMD_END)>=0)
         {
            complete++;
            continue;
         }
      }
      if(SIM_DEBUG && logFile!=null)
         logFile.println("ping: complete");
   }

   private void simulationGetAddress()
      throws IOException
   {
      this.fakeAddress = new byte[8];
      // reset the simulated part
      simulationReset();
      // put the Read Rom command
      simulationPutByte((byte)0x33);
      // get the Rom ID
      for(int i=0; i<8; i++)
         this.fakeAddress[i] = simulationGetByte();
   }

   //--------
   //-------- Default Main Method, for launching server with defaults
   //--------
   /**
    * A Default Main Method, for launching NetAdapterSim getting the
    * default adapter with the OneWireAccessProvider and listening on
    * the default port specified by DEFAULT_PORT.
    */
   public static void main(String[] args)
      throws Exception
   {
      System.out.println("NetAdapterSim");
      if(args.length<1)
      {
         System.out.println("");
         System.out.println("   java com.dalsemi.onewire.adapter.NetAdapterSim <execCmd> <logFilename> <simDebug>");
         System.out.println("");
         System.out.println("   execCmd     - the command to start the simulator");
         System.out.println("   logFilename - the name of the file to log output to");
         System.out.println("   simDebug    - 'true' or 'false', turns on debug output from simulation");
         System.out.println("");
         System.exit(1);
      }

      String execCmd = args[0];
      System.out.println("   Executing: " + execCmd);
      String logFilename = null;
      if(args.length>1)
      {
         if(!args[1].toLowerCase().equals("false"))
         {
            logFilename = args[1];
            System.out.println("   Logging data to file: " + logFilename);
         }
      }
      if(args.length>2)
      {
         NetAdapterSim.SIM_DEBUG = args[2].toLowerCase().equals("true");
         System.out.println("   Simulation Debugging is: "
                            + (NetAdapterSim.SIM_DEBUG?"enabled":"disabled"));
      }


      NetAdapterSim host = new NetAdapterSim(execCmd, logFilename);
      System.out.println("Device Address=" + Address.toString(host.fakeAddress));

      System.out.println("Starting Multicast Listener...");
      host.createMulticastListener();

      System.out.println("Starting NetAdapter Host...");
      (new Thread(host)).start();
      System.out.println("NetAdapter Host Started");
   }
}
