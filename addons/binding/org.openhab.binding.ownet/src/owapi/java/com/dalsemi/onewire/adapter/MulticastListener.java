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

/**
 * Generic Mulitcast broadcast listener.  Listens for a specific message and,
 * in response, gives the specified reply.  Used by NetAdapterHost for
 * automatic discovery of host components for the network-based DSPortAdapter.
 *
 * @author SH
 * @version 1.00
 */
public class MulticastListener implements Runnable
{
   /** boolean flag to turn on debug messages */
   private static final boolean DEBUG = false;

   /** timeout for socket receive */
   private static final int timeoutInSeconds = 3;

   /** multicast socket to receive datagram packets on */
   private MulticastSocket socket = null;
   /** the message we're expecting to receive on the multicast socket */
   private byte[] expectedMessage;
   /** the message we should reply with when we get the expected message */
   private byte[] returnMessage;

   /** boolean to stop the thread from listening for messages */
   private volatile boolean listenerStopped = false;
   /** boolean to check if the thread is still running */
   private volatile boolean listenerRunning = false;

   /**
    * Creates a multicast listener on the specified multicast port,
    * bound to the specified multicast group.  Whenever the byte[]
    * pattern specified by "expectedMessage" is received, the byte[]
    * pattern specifed by "returnMessage" is sent to the sender of
    * the "expected message".
    *
    * @param multicastPort Port to bind this listener to.
    * @param multicastGroup Group to bind this listener to.
    * @param expectedMessage the message to look for
    * @param returnMessage the message to reply with
    */
   public MulticastListener(int multicastPort, String multicastGroup,
                           byte[] expectedMessage, byte[] returnMessage)
      throws IOException,UnknownHostException
   {
      this.expectedMessage = expectedMessage;
      this.returnMessage = returnMessage;

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         System.out.println("DEBUG: Creating Multicast Listener");
         System.out.println("DEBUG:    Multicast port: " + multicastPort);
         System.out.println("DEBUG:    Multicast group: " + multicastGroup);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      // create multicast socket
      socket = new MulticastSocket(multicastPort);
      // set timeout at 3 seconds
      socket.setSoTimeout(timeoutInSeconds*1000);
      //join the multicast group
      InetAddress group = InetAddress.getByName(multicastGroup);
      socket.joinGroup(group);
   }

   /**
    * Run method waits for Multicast packets with the specified contents
    * and replies with the specified message.
    */
   public void run()
   {
      byte[] receiveBuffer = new byte[expectedMessage.length];

      listenerRunning = true;
      while(!listenerStopped)
      {
         try
         {
            // packet for receiving messages
            DatagramPacket inPacket = new DatagramPacket(receiveBuffer,
                                                     receiveBuffer.length);
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            if(DEBUG)
               System.out.println("DEBUG: waiting for multicast packet");
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            // blocks for message until timeout occurs
            socket.receive(inPacket);

            // check to see if the received data matches the expected message
            int length = inPacket.getLength();

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            if(DEBUG)
            {
               System.out.println("DEBUG: packet.length=" + length);
               System.out.println("DEBUG: expecting=" + expectedMessage.length);
            }
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

            if(length==expectedMessage.length)
            {
               boolean dataMatch = true;
               for(int i=0; dataMatch && i<length; i++)
               {
                  dataMatch = (expectedMessage[i]==receiveBuffer[i]);
               }
               // check to see if we received the expected message
               if(dataMatch)
               {
                  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
                  if(DEBUG)
                  {
                     System.out.println("DEBUG: packet match, replying");
                  }
                  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
                  // packet for sending messages
                  DatagramPacket outPacket
                     = new DatagramPacket(returnMessage, returnMessage.length,
                              inPacket.getAddress(), inPacket.getPort());
                  // send return message
                  socket.send(outPacket);
               }
            }
         }
         catch(IOException ioe){/* drain */}
      }
      listenerRunning = false;
   }

   /**
    * Waits for datagram listener to finish, with a timeout.
    */
   public void stopListener()
   {
      listenerStopped = true;
      int i = 0;
      int timeout = timeoutInSeconds*100;
      while(listenerRunning && i++<timeout)
         try{Thread.sleep(10);}catch(Exception e){;}
   }
}