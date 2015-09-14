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

/**
 * Interface for holding all constants related to Network Adapter communications.
 * This interface is used by both NetAdapterHost and the NetAdapter.  In
 * addition, the common utility class <code>Connection</code> is defined here.
 *
 * @author SH
 * @version 1.00
 */
public interface NetAdapterConstants
{
   /** Debug message flag */
   static final boolean DEBUG = false;

   /** version UID, used to detect incompatible host */
   static int versionUID = 1;

   /** Indicates whether or not to buffer the output (probably always true!) */
   static final boolean BUFFERED_OUTPUT = true;

   /** Default port for NetAdapter TCP/IP connection */
   static final int DEFAULT_PORT = 6161;

   /** default secret for authentication with the server */
   static final String DEFAULT_SECRET = "Adapter Secret Default";

   /** Address for Multicast group for NetAdapter Datagram packets */
   static final String DEFAULT_MULTICAST_GROUP = "228.5.6.7";

   /** Default port for NetAdapter Datagram packets */
   static final int DEFAULT_MULTICAST_PORT = 6163;

   /*------------------------------------------------------------*/
   /*----- Method Return codes ----------------------------------*/
   /*------------------------------------------------------------*/
   static final byte RET_SUCCESS                      = (byte)0x0FF;
   static final byte RET_FAILURE                      = (byte)0x0F0;
   /*------------------------------------------------------------*/

   /*------------------------------------------------------------*/
   /*----- Method command bytes ---------------------------------*/
   /*------------------------------------------------------------*/
   static final byte CMD_CLOSECONNECTION              = 0x08;
   static final byte CMD_PINGCONNECTION               = 0x09;
   /*------------------------------------------------------------*/
   /* Raw Data methods ------------------------------------------*/
   static final byte CMD_RESET                        = 0x10;
   static final byte CMD_PUTBIT                       = 0x11;
   static final byte CMD_PUTBYTE                      = 0x12;
   static final byte CMD_GETBIT                       = 0x13;
   static final byte CMD_GETBYTE                      = 0x14;
   static final byte CMD_GETBLOCK                     = 0x15;
   static final byte CMD_DATABLOCK                    = 0x16;
   /*------------------------------------------------------------*/
   /* Power methods ---------------------------------------------*/
   static final byte CMD_SETPOWERDURATION             = 0x17;
   static final byte CMD_STARTPOWERDELIVERY           = 0x18;
   static final byte CMD_SETPROGRAMPULSEDURATION      = 0x19;
   static final byte CMD_STARTPROGRAMPULSE            = 0x1A;
   static final byte CMD_STARTBREAK                   = 0x1B;
   static final byte CMD_SETPOWERNORMAL               = 0x1C;
   /*------------------------------------------------------------*/
   /* Speed methods ---------------------------------------------*/
   static final byte CMD_SETSPEED                     = 0x1D;
   static final byte CMD_GETSPEED                     = 0x1E;
   /*------------------------------------------------------------*/
   /* Network Semaphore methods ---------------------------------*/
   static final byte CMD_BEGINEXCLUSIVE               = 0x1F;
   static final byte CMD_ENDEXCLUSIVE                 = 0x20;
   /*------------------------------------------------------------*/
   /* Searching methods -----------------------------------------*/
   static final byte CMD_FINDFIRSTDEVICE              = 0x21;
   static final byte CMD_FINDNEXTDEVICE               = 0x22;
   static final byte CMD_GETADDRESS                   = 0x23;
   static final byte CMD_SETSEARCHONLYALARMINGDEVICES = 0x24;
   static final byte CMD_SETNORESETSEARCH             = 0x25;
   static final byte CMD_SETSEARCHALLDEVICES          = 0x26;
   static final byte CMD_TARGETALLFAMILIES            = 0x27;
   static final byte CMD_TARGETFAMILY                 = 0x28;
   static final byte CMD_EXCLUDEFAMILY                = 0x29;
   /*------------------------------------------------------------*/
   /* feature methods -------------------------------------------*/
   static final byte CMD_CANBREAK                     = 0x2A;
   static final byte CMD_CANDELIVERPOWER              = 0x2B;
   static final byte CMD_CANDELIVERSMARTPOWER         = 0x2C;
   static final byte CMD_CANFLEX                      = 0x2D;
   static final byte CMD_CANHYPERDRIVE                = 0x2E;
   static final byte CMD_CANOVERDRIVE                 = 0x2F;
   static final byte CMD_CANPROGRAM                   = 0x30;
   /*------------------------------------------------------------*/

   /**
    * An inner utility class for coupling Socket with I/O streams
    */
   static final class Connection
   {
      /** socket to host */
      public java.net.Socket sock = null;
      /** input stream from socket */
      public java.io.DataInputStream input = null;
      /** output stream from socket */
      public java.io.DataOutputStream output = null;
   }

   /** instance for an empty connection, basically it's a NULL object
    *  that's safe to synchronize on. */
   static final Connection EMPTY_CONNECTION = new Connection();
}