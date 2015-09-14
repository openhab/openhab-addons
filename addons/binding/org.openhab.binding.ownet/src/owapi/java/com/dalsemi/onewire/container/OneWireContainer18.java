
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.container;

import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.utils.IOHelper;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import java.util.Vector;
import java.util.Enumeration;


/**
 * <P> 1-Wire&#174 container for a SHA Transaction iButton, DS1963S.
 * This container encapsulates the functionality of the 1-Wire family type <B>18</B> (hex).
 * </P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> 4096 bits of read/write nonvolatile memory organized into 16 pages
 *   <li> 8 memory pages have 64-bit secrets and 32-bit read-only, non-rolling over write cycle counters
 *   <LI> Secrets are write only and have their own write cycle counters
 *   <li> On-chip 512-bit SHA-1 engine to compute 160-bit Message Authentication Codes
 *   <li> 256-bit scratchpad ensures integrity of data transfer
 *   <li> On-chip 16-bit CRC generator for safeguarding data transfers
 *   <li> Overdrive mode supports communication up to 142 kbits per second
 * </UL>
 *
 * <p>This container provides the functionality to use the raw power of the
 * DS1963S.  It does not immediately implement transactions and authentication.
 * The class {@link com.dalsemi.onewire.application.sha.SHAiButtonUser18 SHAiButton} does these.
 * The <code>SHAiButton</code> class exists on top of this class, making higher
 * level calls to implement transactions.</p>

 * <p>This container makes use of several optimizations to help it run fast
 * on TINI.  These optimizations do little for the PC, but they do not
 * slow down the PC.  Most methods are <code>synchronized</code> because they access
 * an instance byte array.  This is less expensive than creating new byte
 * arrays for every method, because there should
 * not be contention for the resources in this container between threads.
 * Threads should use the <code>com.dalsemi.onewire.adapter.DSPortAdapter</code> methods
 * <code>beginExclusive(boolean)</code> and <code>endExclusive()</code> to synchronize on the 1-Wire port.</p>
 *
 * <p>Notice that for maximum performance, programs should call the method <code>setSpeedCheck(boolean)</code>
 * with an argument <code>false</code> before any other methods that access the
 * 1-Wire.  A program that calls this function is assumed to understand and
 * control the speed of communication on the 1-Wire bus.  If the speed check is not disabled,
 * a call to the method <code>OneWireContainer.doSpeed()</code> will occur in almost every
 * function.  While this should guarantee that the 1-Wire bus is never at an unknown speed,
 * it will slow down throughput considerably.</p>
 *
 * <H3> Memory </H3>
 *
 * <p>In the interest of speed, this container has several methods to quickly access
 * the memory of a DS9163S.  These methods include:
 * <ul>
 *     <li> {@link #eraseScratchPad(int)                    <code>eraseScratchPad</code>}
 *     <li> {@link #readScratchPad(byte[],int)              <code>readScratchPad</code>}
 *     <li> {@link #writeScratchPad(int,int,byte[],int,int) <code>writeScratchPad</code>}
 *     <li> {@link #readMemoryPage(int,byte[],int)          <code>readMemoryPage</code>}
 *     <li> {@link #readAuthenticatedPage(int,byte[],int)   <code>readAuthenticatedPage</code>}
 *     <li> {@link #writeDataPage(int,byte[])               <code>writeDataPage</code>}
 * </ul></p>
 *
 * <P> The memory can also be accessed through the objects that are returned
 * from the {@link #getMemoryBanks() getMemoryBanks} method. </P>
 *
 * The following is a list of the MemoryBank instances that are returned:
 *
 * <UL>
 *   <LI> <B> Scratchpad with CRC and auto-hide</B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write not-general-purpose volatile
 *         <LI> <I> Pages</I> 1 page of length 32 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *         <li> <i> Extra information for each page</i>  Target address, offset, length 3
 *      </UL>
 *   <LI> <B> Main Memory </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 256 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 8 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC
 *      </UL>
 *   <LI> <B> Memory with write cycle counter </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 256 starting at physical address 256
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 8 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC
 *         <li> <i> Extra information for each page</i>  Write cycle counter, length 3
 *      </UL>
 *   <LI> <B> Write cycle counters and PRNG counter </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 96 starting at physical address 608
 *         <LI> <I> Features</I> Read-only not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 3 pages of length 32 bytes
 *      </UL>
 * </UL>
 *
 * <H3> Usage </H3>
 *
 * <p>For examples on using the SHA iButton for transactions and authentications, see
 * the examples in the {@link com.dalsemi.onewire.application.sha.SHAiButtonUser18 SHAiButton}
 * usage section.</p>
 *
 * For examples regarding memory operations,
 * <uL>
 * <li> See the usage example in
 * {@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer}
 * to enumerate the MemoryBanks.
 * <li> See the usage examples in
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank} and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * for bank specific operations.
 * </uL>
 *
 * <H3> DataSheet </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1963S.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1963S.pdf</A>
 * </DL>
 *
 * <p>Note that the protocol for transactions using SHA iButtons calls for
 * 2 iButtons.  One belongs to the system that grants authentication and
 * is authorized to validate money.  This iButton is usually called the
 * Coprocessor.  The other iButton is the device that contains digital
 * money that must be authenticated and verified to perform transactions.
 * This iButton is usually called a User iButton, Roaming iButton,
 * or Roving iButton.</p>
 *
 * @author KLA
 * @version 0.00, 28 Aug 2000
 * @see com.dalsemi.onewire.application.sha.SHAiButtonUser18
 * @see com.dalsemi.onewire.adapter.DSPortAdapter#beginExclusive(boolean)
 * @see com.dalsemi.onewire.adapter.DSPortAdapter#endExclusive()
 * @see OneWireContainer#doSpeed()
 */
public class OneWireContainer18
   extends OneWireContainer
{

   //turns on extra debugging output in all 1-wire containers
   private static final boolean DEBUG = false;

   //--------
   //-------- Variables
   //--------

   /**
    * Scratchpad access memory bank
    */
   private MemoryBankScratch scratch;

   /**
    * Main memory access memory bank
    */
   private MemoryBankNVCRC memory;

   /**
    * Main memory with counter access memory bank
    */
   private MemoryBankNVCRC memoryPlus;

   /* number of times we wait for the 10101010... to appear
    * on commands such as read_auth_page, copy_scratch...
    */
   private int block_wait_count = 20;

   /* are we currently using resume?  some internal code
    * makes use of the resume function, but often we can't
    * tell from inside this class
    */
   private boolean resume = false;

   /* should we call doSpeed every time we do something?
    * this is the safe way to make sure we never loose
    * communication, but this stuff has really gotta
    * fly, so to make it fly, disable this before doing anything
    * else!
    */
   private boolean doSpeedEnable = true;

   private byte[] byte_buffer     = new byte [60];   //use this everywhere to communicate
   private byte[] private_address = null;


   static byte[] FF        = new byte [60];   //use this to fill an array with 0x0ff's
   static
   {
      for (int i = 0; i < FF.length; i++)
         FF [i] = ( byte ) 0x0ff;
   }
   //there's really no good reason for these to be public like they were in 0.00 OneWire release
   private byte TA1;
   private byte TA2;
   private byte ES;

   //--------
   //-------- PUBLIC STATIC FINAL's
   //--------

   /* 1-Wire Protocol command to read the DS1963S memory.
    * See the datasheet for more information.
    *
    * @see #readMemoryPage(int,byte[],int)
    */
   public static final byte READ_MEMORY = ( byte ) 0xF0;

   /* 1-Wire Protocol command to write the DS1963S scratchpad.
    * See the datasheet for more information.
    *
    * @see #writeScratchPad(int,int,byte[],int,int)
    */
   public static final byte WRITE_SCRATCHPAD = ( byte ) 0x0F;

   /* 1-Wire Protocol command to match a signature to
    * the one in the DS1963S scratchpad.  To verify a signature,
    * the DS1963S goes into hidden mode.  It can only be verified
    * with this command.  See the datasheet for more information.
    *
    * @see #matchScratchPad(byte[])
    */
   public static final byte MATCH_SCRATCHPAD = ( byte ) 0x3C;

   /* 1-Wire Protocol command to erase the DS1963S scratchpad.
    * Also brings the device out of hidden mode and back to
    * normal operation.  See the datasheet for more information.
    *
    * @see #eraseScratchPad(int)
    */
   public static final byte ERASE_SCRATCHPAD = ( byte ) 0xC3;

   /* 1-Wire Protocol command to read the DS1963S scratchpad.
    * See the datasheet for more information.
    *
    * @see #readScratchPad(byte[],int)
    */
   public static final byte READ_SCRATCHPAD = ( byte ) 0xAA;

   /* 1-Wire Protocol command to read an authenticated
    * DS1963S memory page.  The device generates a signature for
    * the page that is placed in the scratchpad.
    * See the datasheet for more information.
    *
    * @see #readAuthenticatedPage(int,byte[],int)
    */
   public static final byte READ_AUTHENTICATED_PAGE = ( byte ) 0xA5;

   /* 1-Wire Protocol command to copy the DS1963S scratchpad
    * to a memory location.  See the datasheet for more information.
    *
    * @see #copyScratchPad()
    */
   public static final byte COPY_SCRATCHPAD = ( byte ) 0x55;

   /* 1-Wire Protocol command to perform a SHA cryptographic
    * function on the DS1963S.  See the datasheet for more information.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    */
   public static final byte COMPUTE_SHA = ( byte ) 0x33;

   //SHA commands

   /* 1-Wire Protocol command to compute a master secret
    * on the DS1963S.  See the datasheet for more information.
    * This command should be used with the first partial secret.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    * @see #installMasterSecret(int,byte[],int)
    */
   public static final byte COMPUTE_FIRST_SECRET = ( byte ) 0x0F;

   /* 1-Wire Protocol command to compute a master secret
    * on the DS1963S.  See the datasheet for more information.
    * This command should be used with any partial secret
    * except the first.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    * @see #installMasterSecret(int,byte[],int)
    */
   public static final byte COMPUTE_NEXT_SECRET = ( byte ) 0xF0;

   /* 1-Wire Protocol command to verify signatures
    * on the DS1963S.  See the datasheet for more information.
    * This command should be used to verify authentication
    * and to verify user data.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    */
   public static final byte VALIDATE_DATA_PAGE = ( byte ) 0x3C;

   /* 1-Wire Protocol command to create a signature of a
    * selected data page on the DS1963S.  See the datasheet for more information.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    */
   public static final byte SIGN_DATA_PAGE = ( byte ) 0xC3;

   /* 1-Wire Protocol command to create a random challenge
    * using the DS1963S's pseudo random number generator.
    * See the datasheet for more information.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    * @see #SHAiButton#generateChallenge(int,int,byte[])
    */
   public static final byte COMPUTE_CHALLENGE = ( byte ) 0xCC;

   /* 1-Wire Protocol command to authenticate a host
    * on the DS1963S.  See the datasheet for more information.
    *
    * @see #SHAFunction(byte,int)
    * @see #SHAFunction(byte)
    */
   public static final byte AUTH_HOST = ( byte ) 0xAA;

   /* 1-Wire Protocol command that allows quick reselection
    * of the DS1963S.  Normally, selection involved a nine byte sequence:
    * one byte for the Select Command, and the eight byte address of the
    * 1-Wire device.  The 1963S remembers if it was the last device to
    * communicate with the master on the 1-Wire bus.  If it was, and it
    * receives the <code>RESUME</code> command, then it is selected.
    *
    * @see #useResume(boolean)
    */
   public static final byte RESUME = ( byte ) 0xA5;

   //--------
   //-------- Constructors
   //--------

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1963S SHA iButton.
    * Note that the method <code>setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer18(DSPortAdapter,byte[])
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer18(DSPortAdapter,long)
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer18(DSPortAdapter,String)
    */
   public OneWireContainer18 ()
   {
      super(null,0);

      if(private_address==null)
         private_address = new byte [8];

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1963S SHA iButton.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1963S SHA iButton
    *
    * @see #OneWireContainer18()
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer18(DSPortAdapter,long)
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer18(DSPortAdapter,String)
    */
   public OneWireContainer18 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      if(private_address==null)
         private_address = new byte [8];

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1963S SHA iButton.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1963S SHA iButton
    *
    * @see #OneWireContainer18()
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer18(DSPortAdapter,byte[])
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer18(DSPortAdapter,String)
    */
   public OneWireContainer18 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      if(private_address==null)
         private_address = new byte [8];

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1963S SHA iButton.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1963S SHA iButton
    *
    * @see #OneWireContainer18()
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer18(DSPortAdapter,byte[])
    * @see #OneWireContainer18(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer18(DSPortAdapter,long)
    */
   public OneWireContainer18 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      if(private_address==null)
         private_address = new byte [8];

      // initialize the memory banks
      initMem();
   }

   //--------
   //-------- Methods
   //--------

   /**
    * <p>Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.  Overrides the <code>OneWireContainer</code>
    * method for speed, as the <code>OneWireContainer</code> version has
    * a byte array allocation.  Since there is a call to <code>setupContainer()</code>
    * in the critical path of a transaction (when a roving SHA iButton is
    * introduced to the 1-Wire Bus), this must occur quickly.  This improves performance
    * on TINI.</p>
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    *
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      // get a reference to the source adapter (will need this to communicate)
      this.adapter = sourceAdapter;

      // set the Address
      synchronized (this)
      {
         if(private_address==null)
            private_address = new byte[8];

         System.arraycopy(newAddress, 0, private_address, 0, 8);

         this.address = private_address;
      }

      // set desired speed to be SPEED_REGULAR by default with no fallback
      speed           = DSPortAdapter.SPEED_REGULAR;
      speedFallBackOK = false;
   }

   /**
    * Get the Dallas Semiconductor part number of the iButton
    * or 1-Wire Device as a <code>java.lang.String</code>.
    * For example "DS1992".
    *
    * @return iButton or 1-Wire device name
    */
   public String getName ()
   {
      return "DS1963S";
   }

   /**
    * Retrieve the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of MicroLAN devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * "Crypto iButton".
    *
    * @return  the alternate names for this iButton or 1-Wire device
    */
   public String getAlternateNames ()
   {
      return "SHA-1 iButton";
   }

   /**
    * Get a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "4096 bits of read/write nonvolatile memory. Memory "
             + "is partitioned into sixteen pages of 256 bits each. "
             + "Has overdrive mode.  One-chip 512-bit SHA-1 engine "
             + "and secret storage.";
   }

   /**
    * Returns the maximum speed this iButton device can
    * communicate at.
    *
    * @return maximum speed
    * @see DSPortAdapter#setSpeed
    */
   public int getMaxSpeed ()
   {
      return DSPortAdapter.SPEED_OVERDRIVE;
   }

   /**
    * Gets an enumeration of memory bank instances that implement one or more
    * of the following interfaces:
    * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank},
    * and {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}.
    * @return <CODE>Enumeration</CODE> of memory banks
    */
   public Enumeration getMemoryBanks ()
   {
      Vector bank = new Vector(4);

      // scratchpad
      bank.addElement(scratch);

      // NVRAM (no write cycle)
      bank.addElement(memory);

      // NVRAM (with write cycle counters)
      bank.addElement(memoryPlus);

      // Page Write cycle counters
      MemoryBankNV cnt = new MemoryBankNV(this,
                                          ( MemoryBankScratch ) scratch);

      cnt.numberPages          = 3;
      cnt.size                 = 96;
      cnt.bankDescription      = "Write cycle counters and PRNG counter";
      cnt.startPhysicalAddress = 608;
      cnt.readOnly             = true;
      cnt.pageAutoCRC          = false;
      cnt.generalPurposeMemory = false;
      cnt.readWrite            = false;

      bank.addElement(cnt);

      return bank.elements();
   }

   /**
    * Construct the memory banks used for I/O.
    */
   private void initMem ()
   {

      // scratchpad
      scratch = new MemoryBankScratchSHA(this);

      // NVRAM (no write cycle)
      memory                      = new MemoryBankNVCRC(this,
              ( MemoryBankScratch ) scratch);
      memory.numberPages          = 8;
      memory.size                 = 256;
      memory.extraInfoLength      = 8;
      memory.readContinuePossible = false;
      memory.numVerifyBytes       = 8;

      // NVRAM (with write cycle counters)
      memoryPlus                      = new MemoryBankNVCRC(this,
              ( MemoryBankScratch ) scratch);
      memoryPlus.numberPages          = 8;
      memoryPlus.size                 = 256;
      memoryPlus.bankDescription      = "Memory with write cycle counter";
      memoryPlus.startPhysicalAddress = 256;
      memoryPlus.extraInfo            = true;
      memoryPlus.extraInfoDescription = "Write cycle counter";
      memoryPlus.extraInfoLength      = 8;
      memoryPlus.readContinuePossible = false;
      memoryPlus.numVerifyBytes       = 8;
   }

   ////////////////////////////////////////////////////////////////////
   //SHAiButton real methods!!!!!!
   ////////////////////////////////////////////////////////////////////

   /**
    * Directs the container to avoid the calls to doSpeed() in methods that communicate
    * with the SHA iButton. To ensure that all parts can talk to the 1-Wire bus
    * at their desired speed, each method contains a call
    * to <code>doSpeed()</code>.  However, this is an expensive operation.
    * If a user manages the bus speed in an
    * application,  call this method with <code>doSpeedCheck</code>
    * as <code>false</code>.  The default behavior is
    * to call <code>doSpeed()</code>.
    *
    * @param doSpeedCheck <code>true</code> for <code>doSpeed()</code> to be called before every
    * 1-Wire bus access, <code>false</code> to skip this expensive call
    *
    * @see OneWireContainer#doSpeed()
    */
   public synchronized void setSpeedCheck (boolean doSpeedCheck)
   {
      doSpeedEnable = doSpeedCheck;
   }

   /**
    * <p>Tells this <code>OneWireContainer18</code> whether it can use the <code>RESUME</code> command.
    * The <code>RESUME</code> command allows the DS1963S to be re-selected for communication
    * quickly by using a one <code>byte</code> <code>RESUME</code> command rather than a
    * nine <code>byte</code> selection sequence.  If another 1-Wire device is accessed,
    * <code>useResume(false)</code> must be called, a normal selection of the part
    * must be performed, and then <code>useResume(true)</code> can be called.</p>
    *
    * @param set <code>true</code> to use the one <code>byte</code> <code>RESUME</code> command
    * instead of the nine <code>byte</code> select command
    *
    * @see #RESUME
    */
   public synchronized void useResume (boolean set)
   {
      resume = set;
   }

   /**
    * <p>Erases the scratchpad of the DS1963S.  The cryptographic features
    * of the DS1963S leave the device in 'hidden mode', which makes most
    * memory functions inaccessible.  A call to <code>eraseScratchPad(int)</code>
    * brings the device out of 'hidden mode', filling the scratchpad
    * with 0x0FF <code>bytes</code>.</p>
    *
    * <p>The argument <code>page</code> is usually unimportant, except in cases
    * where a memory page needs to be erased.  Erase a memory page by calling
    * <code>eraseScratchPad(page_number)</code>, followed by <code>readScratchPad()</code>
    * and then <code>copyScratchPad()</code>.
    *
    * @param page the target page number
    *
    * @return true if successful, false if the operation failed while waiting for the
    * DS1963S's output to alternate (see the datasheet for a description)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #waitForSuccessfulFinish()
    * @see #readScratchPad(byte[],int)
    * @see #copyScratchPad()
    */
   public synchronized boolean eraseScratchPad (int page)
      throws OneWireIOException, OneWireException
   {
      if (doSpeedEnable && (!resume))
         doSpeed();

      // select the device
      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      // build block to send
      byte[] buffer = byte_buffer;

      buffer [0] = ERASE_SCRATCHPAD;
      buffer [1] = ( byte ) (page << 5);
      buffer [2] = ( byte ) (page >> 3);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Erase Scratchpad");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.write("target address: 0x");
         IOHelper.writeHex((byte)buffer [2]);
         IOHelper.writeLineHex((byte)buffer [1]);
         IOHelper.writeLine("adapter.getSpeed()="+adapter.getSpeed());
         IOHelper.writeLine("adapter.getPortTypeDescription()="+adapter.getPortTypeDescription());
         IOHelper.writeLine("this.speed="+this.speed);
         IOHelper.writeLine("device isPresent: "+adapter.isPresent(this.address));
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      System.arraycopy(FF, 0, buffer, 3, 3);

      if(DEBUG)
      {
         IOHelper.writeLine("buffer:");
         IOHelper.writeBytesHex(buffer,0,9);
      }

      // send block (check copy indication complete)
      adapter.dataBlock(buffer, 0, 6);

      if(DEBUG)
      {
         IOHelper.writeLine("buffer:");
         IOHelper.writeBytesHex(buffer,0,9);
         IOHelper.writeLine("------------------------------------");
      }

      if (buffer [5] == ( byte ) 0x0ff)
         return waitForSuccessfulFinish();
      else
         return true;
   }

   /**
    * <p>Waits for the DS1963S's output to alternate.  Several operations must
    * wait for the DS1963S to stop sending 0x0ff's and begin alternating its
    * output bits.  This method reads until it finds a non-0x0ff <code>byte</code> or until it
    * reaches a specified number of tries, indicating failure.</p>
    *
    * <p>This method can often be optimized away.  A normal 1-Wire transaction
    * involves writing and reading a known number of bytes.  If a few more bytes
    * are read, a program can check to see if the DS1963S has started alternating
    * its output much quicker than calling this method will.  For instance,
    * to copy the scratchpad, the source code might look like this:
    * <code><pre>
    *    buffer [0] = COPY_SCRATCHPAD;
    *    buffer [1] = TA1;
    *    buffer [2] = TA2;
    *    buffer [3] = ES;
    *
    *    adapter.dataBlock(buffer,0,4);
    *    return waitForSuccessfulFinish();
    * </pre></code>
    * To optimize the code, read more bytes than required:
    * <code><pre>
    *    buffer [0] = COPY_SCRATCHPAD;
    *    buffer [1] = TA1;
    *    buffer [2] = TA2;
    *    buffer [3] = ES;
    *
    *    //copy 0x0FF into the buffer, this effectively reads
    *    System.arraycopy(FF, 0, buffer, 4, 5);
    *
    *    //read 5 extra bytes
    *    adapter.dataBlock(buffer, 0, 9);
    *
    *    //if the last byte has not shown alternating output,
    *    //still call waitForSuccessfulFinish(), else
    *    //we are already done
    *    if (buffer [8] == ( byte ) 0x0ff)
    *         return waitForSuccessfulFinish();
    *     else
    *         return true;
    * </pre></code></p>
    *
    * <p>The second method is faster because it is more expensive to
    * invoke another method that goes down to the native access layer
    * than it is to just read a few more bytes while the program is
    * already at the native access layer.</p>
    *
    * <p>See the datasheet for which operations function in this manner.
    * Only call this method after another method which has successfully
    * communicated with the DS1963S.</p>
    *
    * @return <code>true</code> if the DS1963S completed its operation successfully
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #copyScratchPad()
    * @see #eraseScratchPad(int)
    * @see #readAuthenticatedPage(int,byte[],int)
    */
   public synchronized boolean waitForSuccessfulFinish ()
      throws OneWireIOException, OneWireException
   {

      //this method should be called after another method, so let's not worry
      //about making sure the speed is right, it should be already
      int count = 0;

      while (adapter.getByte() == 0xff)
      {
         count++;

         if (count == block_wait_count)
            return false;
      }

      return true;
   }

   /**
    * <p>Reads a memory page from the DS1963S.  Pages 0-15 are normal memory pages.
    * Pages 16 and 17 contain the secrets--the DS1963S will return all 1's (0x0ff
    * bytes).  Page 18 is the physical location of the scratchpad, and will only
    * return valid data if the part is not in hidden mode (0x0ff bytes are returned
    * otherwise).  Pages 19 and 20 contain the write cycle counters.
    * Page 21 contains the counter for the pseudo random number generator (PRNG).
    * Consult the datasheet for the memory maps of these special pages.</p>
    *
    * <p>Note that the same data can be returned through the <code>MemoryBank</code>s
    * returned by <code>getMemoryBanks()</code>.  However, this method contains
    * several enhancements for faster reading.</p>
    *
    * @param pageNum page number to read
    * @param data array for the return of the data (must be at least 32 bytes long)
    * @param start offset into the byte array to start copying page data
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #readAuthenticatedPage(int,byte[],int)
    * @see #getMemoryBanks()
    */
   public void readMemoryPage (int pageNum, byte[] data, int start)
      throws OneWireIOException, OneWireException
   {

      //don't need to be synchronized, since readMemoryPage(int, byte, int, byte[], int) is
      readMemoryPage(pageNum, READ_MEMORY, 32, data, start);
   }

   /*
    * read the contents of a data page
    */
   private synchronized void readMemoryPage (int pageNum, byte COMMAND,
                                             int bytes_to_read, byte[] data,
                                             int start)
      throws OneWireIOException, OneWireException
   {
      if (doSpeedEnable && (!resume))
         doSpeed();

      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;
      int    addr   = pageNum << 5;   //pageNumber * 32

      buffer [0] = COMMAND;
      buffer [1] = ( byte ) addr;
      buffer [2] = ( byte ) (addr >> 8);

      System.arraycopy(FF, 0, buffer, 3, bytes_to_read);
      adapter.dataBlock(buffer, 0, 3 + bytes_to_read);

      // copy data for return
      System.arraycopy(buffer, 3, data, start, bytes_to_read);
   }

   /**
    * <p>Reads and authenticates a page.  See <code>readMemoryPage()</code> for a description
    * of page numbers and their contents.  This method will also generate a signature for the
    * selected page, used in the authentication of roving (User) iButtons.
    * Extra data is returned with the page as well--such as the write cycle
    * counter for the page and the write cycle counter of the selected
    * secret page.</p>
    * <ul>
    *     <li>32 bytes of page data                </li>
    *     <li>4 bytes secret counter for page      </li>
    *     <li>4 bytes write cycle counter for page </li>
    *     <li>2 byte CRC                           </li>
    * </ul>
    * <p>Note that the CRC will be verified by this method, though it is returned with the data.</p>
    *
    * @param pageNum page number to read and authenticate
    * @param data array for the page data plus extra information (2 write cycle
    * counters of 4 bytes each, one 2 byte CRC, appended after 32 bytes of
    * the data page).  This byte array must be at least 42 bytes long.
    * @param start offset to copy into the array
    *
    * @return <code>true</code> if successful, <code>false</code> if the operation
    *         failed while waiting for the DS1963S's output to alternate
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #readMemoryPage(int,byte[],int)
    */
   public boolean readAuthenticatedPage (int pageNum, byte[] data, int start)
      throws OneWireIOException, OneWireException
   {

      //don't need to be synchronized, since readMemoryPage(int, byte, int, byte[], int) is
      //read 42 bytes = 32 page bytes + 4 bytes secret counter + 4 bytes page counter + 2 bytes CRC
      readMemoryPage(pageNum, READ_AUTHENTICATED_PAGE, 42, data, start);

      int crc = CRC16.compute(READ_AUTHENTICATED_PAGE);

      crc = CRC16.compute(( byte ) (pageNum << 5), crc);   //(pagenumber*32 = address) lower 8 bits
      crc = CRC16.compute(( byte ) (pageNum >>> 3), crc);   //pagenumber*32 is pagenumber<<5, but
      //we want the upper 8 bits, so we would just do
      // (pagenum<<5)>>>8, so just make it one op

      // check CRC16
      if (CRC16.compute(data, start, 42, crc) != 0xB001)
      {
         return false;
      }

      return (waitForSuccessfulFinish());
   }

   /**
    * <p>Writes data to the scratchpad.  In order to write to a data page using this method,
    * next call <code>readScratchPad()</code>, and then <code>copyScratchPad()</code>.
    * Note that the addresses passed to this method will be the addresses the data is
    * copied to if the <code>copyScratchPad()</code> method is called afterward.</p>
    *
    * <p>Also note that if too many bytes are written, this method will truncate the
    * data so that only a valid number of bytes will be sent.</p>
    *
    * @param targetPage the page number this data will eventually be copied to
    * @param targetPageOffset the offset on the page to copy this data to
    * @param inputbuffer the data that will be copied into the scratchpad
    * @param start offset into the input buffer for the data to write
    * @param length number of bytes to write
    *
    * @return <code>true</code> if successful, <code>false</code> on a CRC error
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #copyScratchPad()
    * @see #eraseScratchPad(int)
    * @see #readScratchPad(byte[],int)
    * @see #writeDataPage(int,byte[])
    */
   public synchronized boolean writeScratchPad (int targetPage,
           int targetPageOffset, byte[] inputbuffer, int start, int length)
      throws OneWireIOException, OneWireException
   {
      if (doSpeedEnable && (!resume))
         doSpeed();

      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;
      int    addr   = (targetPage << 5) + targetPageOffset;

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Writing Scratchpad of iButton");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("targetPage: " + targetPage);
         IOHelper.writeLine("targetPageOffset: " + targetPageOffset);
         IOHelper.write("target address: 0x");
         IOHelper.writeHex((byte)(addr>>8));
         IOHelper.writeLineHex((byte)addr);
         IOHelper.writeLine("inputbuffer");
         IOHelper.writeBytesHex(inputbuffer);
         IOHelper.writeLine("start: " + start);
         IOHelper.writeLine("length: " + length);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\


      buffer [0] = WRITE_SCRATCHPAD;
      buffer [1] = ( byte ) addr;
      buffer [2] = ( byte ) (addr >> 8);

      int maxbytes = 32 - (addr & 31);

      if (length > maxbytes)
         length = maxbytes;   //if we are going to go over the 32byte boundary

      //let's cut it
      System.arraycopy(inputbuffer, start, buffer, 3, length);

      buffer [3 + length] = ( byte ) 0xff;
      buffer [4 + length] = ( byte ) 0xff;   //leave space for the CRC

      //this nasty statement sends the length depending on if we are
      //going to get a CRC back.  you only get a CRC if you get to the end of
      //the scratchpad writing.  so we look at the address and add the length
      //that we are writing.  if this is not a multiple of 32 then we do not
      //finish reading at the scratchpad boundary (and we checked earlier to
      //make sure don't go over THEREFORE we have gone under).  if we are at
      //the boundary we need two extra bytes to read the crc
      adapter.dataBlock(buffer, 0,
                        ((((addr + length) & 31) == 0) ? length + 5
                                                       : length + 3));

      //if we dont check the CRC we are done
      if (((addr + length) & 31) != 0)
         return true;

      //else we need to do a CRC calculation
      if (CRC16.compute(buffer, 0, length + 5, 0) != 0xB001)
      {
         if(DEBUG)
            System.out.println("CRC Failed in Write Scratchpad");
         return false;
      }

      return true;
   }

   /**
    * <p>Verifies the hidden signature in the scratchpad of the DS1963S.
    * After a <code>VALIDATE_DATA_PAGE</code> command, the scratchpad
    * contains a signature that cannot be read, but must be verified.
    * This method gives the DS1963S a signature.  The iButton then
    * checks to see if it equals the hidden signature in its
    * scratchpad.</p>
    *
    * <p>The signature must be 20 bytes long, and it must start at offset 0
    * of the input array.</p>
    *
    * @param mac the signature starting at offset 0
    *
    * @return <code>true</code> if the signature matches
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #VALIDATE_DATA_PAGE
    */
   public synchronized boolean matchScratchPad (byte[] mac)
      throws OneWireIOException, OneWireException
   {
      if (doSpeedEnable && (!resume))
         doSpeed();

      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;

      buffer [0] = MATCH_SCRATCHPAD;

      System.arraycopy(mac, 0, buffer, 1, 20);

      buffer [21] = ( byte ) 0x0ff;   //CRC1
      buffer [22] = ( byte ) 0x0ff;   //CRC2
      buffer [23] = ( byte ) 0x0ff;   //status

      adapter.dataBlock(buffer, 0, 24);

      if (CRC16.compute(buffer, 0, 23, 0) != 0xB001)
      {
         return false;
      }

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Matching the scratchpad");
         IOHelper.writeLine("Address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("mac");
         IOHelper.writeBytesHex(mac);
         IOHelper.writeLine("matchScratchpad buffer");
         IOHelper.writeBytesHex(buffer,0,23);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      if (buffer [23] != ( byte ) 0x0ff)
         return true;

      return false;
   }

   /**
    * <p>Reads the contents of the DS1963S scratchpad.  If the device is in hidden
    * mode, all 1's (0x0ff bytes) will be returned.</p>
    *
    * <p>This method will return up to 32 bytes.  It may return less
    * if the target address stored internally on the DS1963S is
    * not a multiple of 32.</p>
    *
    * @param data array to hold the contents of the scratchpad
    * @param start offset to copy the scratchpad data into the <code>byte</code> array
    *
    * @return the number of bytes read, or -1 if there was a CRC failure
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #writeScratchPad(int,int,byte[],int,int)
    */
   public synchronized int readScratchPad (byte[] data, int start)
      throws OneWireIOException, OneWireException
   {
      if (doSpeedEnable && (!resume))
         doSpeed();

      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;

      buffer [0] = READ_SCRATCHPAD;

      System.arraycopy(FF, 0, buffer, 1, 37);
      adapter.dataBlock(buffer, 0, 38);

      TA1 = buffer [1];
      TA2 = buffer [2];
      ES  = buffer [3];

      int length = 32 - (TA1 & 31);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Read Scratchpad");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.write("target address: 0x");
         IOHelper.writeHex((byte)TA2);
         IOHelper.writeLineHex((byte)TA1);
         IOHelper.write("ES: 0x");
         IOHelper.writeLineHex((byte)ES);
         IOHelper.writeLine("data");
         IOHelper.writeBytesHex(buffer,4,length);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      if (CRC16.compute(buffer, 0, 6 + length, 0) != 0xB001)
      {
         return -1;
      }

      if (data != null)
         System.arraycopy(buffer, 4, data, start, length);

      return length;
   }

   /**
    * Copies the contents of the scratchpad to the target destination
    * that was specified in a call to <code>writeScratchPad()</code> or
    * <code>eraseScratchPad()</code>.  This method will not success unless
    * a call to <code>readScratchPad()</code> is made first to verify
    * the target address registers in the DS1963S.
    *
    * @return <code>true</code> if successful, <code>false</code> if the operation
    *         failed while waiting for the DS1963S's output to alternate
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #eraseScratchPad(int)
    * @see #readScratchPad(byte[],int)
    * @see #writeScratchPad(int,int,byte[],int,int)
    */
   public synchronized boolean copyScratchPad ()
      throws OneWireIOException, OneWireException
   {
      if (doSpeedEnable && (!resume))
         doSpeed();

      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;

      buffer [0] = COPY_SCRATCHPAD;
      buffer [1] = TA1;
      buffer [2] = TA2;
      buffer [3] = ES;

      System.arraycopy(FF, 0, buffer, 4, 5);

      //adapter.dataBlock(buffer,0,4);
      adapter.dataBlock(buffer, 0, 9);

      if (buffer [8] == ( byte ) 0x0ff)
         return waitForSuccessfulFinish();
      else
         return true;
   }

   /**
    * <p>Installs a secret on a DS1963S.  The secret is written in partial phrases
    * of 47 bytes (32 bytes to a memory page, 15 bytes to the scratchpad) and
    * is cumulative until the entire secret is processed.  Secrets are associated
    * with a page number.  See the datasheet for more information on this
    * association.</p>
    *
    * <p>In most cases, <code>page</code> should be equal to <code>secret_number</code>
    * or <code>secret_number+8</code>, based on the association of secrets and page numbers.
    * Secrets are stored across pages 16 and 17 of the DS1963S memory.  A secret is 8 bytes, so
    * there are 8 secrets.  These 8 secrets are associated with the first 16 pages of memory.</p>
    *
    * <p>On TINI, this method will be slightly faster if the secret's length is divisible by 47.
    * However, since secret key generation is a part of initialization, it is probably
    * not necessary.</p>
    *
    * @param page the page number used to write the partial secrets to
    * @param secret the entire secret, in partial phrases, to be installed
    * @param secret_number the secret 'page' to use (0 - 7)
    *
    * @return <code>true</code> if successful
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #bindSecretToiButton(int,byte[],byte[],int)
    */
   public synchronized boolean installMasterSecret (int page, byte[] secret,
           int secret_number)
      throws OneWireIOException, OneWireException
   {

      //47 is a magic number here because every time a partial secret
      //is to be computed, 32 bytes goes in the page and 15 goes in
      //the scratchpad, so it's going to be easier in the computations
      //if i know the input buffer length is divisible by 47
      if (secret.length == 0)
         return false;

      byte[] input_secret      = null;
      byte[] buffer            = byte_buffer;
      int    secret_mod_length = secret.length % 47;

      if (secret_mod_length == 0)   //if the length of the secret is divisible by 47
         input_secret = secret;
      else
      {

         /* i figure in the case where secret is not divisible by 47
            it will be quicker to just create a new array once and
            copy the data in, rather than on every partial secret
            calculation do bounds checking */
         input_secret = new byte [secret.length + (47 - secret_mod_length)];

         System.arraycopy(secret, 0, input_secret, 0, secret.length);
      }

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Installing Secret on iButton");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("page: " + page);
         IOHelper.writeLine("secret");
         IOHelper.writeBytesHex(secret);
         IOHelper.writeLine("input_secret");
         IOHelper.writeBytesHex(input_secret);
         IOHelper.writeLine("secret_number: " + (secret_number&7));
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //make sure the secret number is between 0 and 7
      secret_number = secret_number & 7;

      //the secret page is 16 for secrets 0-3, page 17 for secrets 4-7
      int secret_page = (secret_number > 3) ? 17
                                            : 16;

      //each page has 4 secrets, so look at 2 LS bits
      int    secret_offset = (secret_number & 3) << 3;
      int    offset        = 0;   //the current offset into the input_secret buffer
      byte[] sp_buffer     = new byte [32];

      while (offset < input_secret.length)
      {
         if (!eraseScratchPad(page))
            return false;

         if (!writeScratchPad(page, 0, input_secret, offset, 32))
            return false;   //first write the whole page

         if (readScratchPad(buffer, 0) < 0)
            return false;   //get the address registers

         if (!copyScratchPad())
            return false;   //copy the page into memory

         System.arraycopy(input_secret, offset + 32, sp_buffer, 8, 15);

         if (!writeScratchPad(page, 0, sp_buffer, 0, 32))
            return false;   //now write the scratchpad data

         if (!SHAFunction((offset == 0) ? COMPUTE_FIRST_SECRET
                                        : COMPUTE_NEXT_SECRET))
            return false;   //means a failure

         //here we have to write the scratchpad with 32 bytes of dummy data
         if (!write_read_copy_quick(secret_page, secret_offset))
            return false;

         offset += 47;
      }

      //now lets clean up - erase the scratchpad and data page
      writeDataPage(page, FF);

      /*
      //This fails for some parts, due to "well-known" problem with
      //erase scratchpad not setting up TA1,TA2 properly
      eraseScratchPad(page);
      readScratchPad(buffer, 0);
      copyScratchPad();*/

      return true;
   }

   //local cahce to make TINI fast
   private byte[] bind_code_temp     = new byte [32];
   private byte[] bind_code_alt_temp = new byte [32];
   private byte[] bind_data_temp     = new byte [32];

   /**
    * <p>Binds an installed secret to a DS1963S by using
    * well-known binding data and the DS1963S's unique
    * address.  This makes the secret unique
    * for this iButton.  Coprocessor iButtons use this method
    * to recreate the iButton's secret to verify authentication.
    * Roving iButtons use this method to finalize their secret keys.</p>
    *
    * <p>Note that unlike in the <code>installMasterSecret()</code> method,
    * the page number does not need to be equivalent to the <code>secret_number</code>
    * modulo 8.  The new secret (installed secret + binding code) is generated
    * from this page but can be copied into another secret.  User iButtons should
    * bind to the same page the secret was installed on.  Coprocessor iButtons
    * must copy to a new secret to preserve the general system authentication
    * secret.</p>
    *
    * <p>The binding should be either 7 bytes long or 15 bytes long.
    * A 15-length <code>byte</code> array is unaltered and placed in the scratchpad
    * for the binding.  A 7-length <code>byte</code> array is combined with the page
    * number and DS1963S unique address and then placed in the scratchpad.
    * Coprocessors should use a pre-formatted 15-length <code>byte</code> array.
    * User iButtons should let the method format for them (i.e.
    * use the 7-length <code>byte</code> array option).</p>
    *
    * @param page the page number that has the master secret already installed
    * @param bind_data 32 bytes of binding data used to bind the iButton to the system
    * @param bind_code the 7-byte or 15-byte binding code
    * @param secret_number secret number to copy the resulting secret to
    *
    * @return <code>true</code> if successful
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #installMasterSecret(int,byte[],int)
    */
   public synchronized boolean bindSecretToiButton (int page,
           byte[] bind_data, byte[] bind_code, int secret_number)
      throws OneWireIOException, OneWireException
   {
      if (bind_data.length != 32)
      {
         System.arraycopy(bind_data, 0, bind_data_temp, 0,
                          (bind_data.length > 32 ? 32
                                                 : bind_data.length));

         bind_data = bind_data_temp;
      }

      if (bind_code.length != 15)
      {
         if (bind_code.length == 7)
         {
            System.arraycopy(bind_code, 0, bind_code_alt_temp, 0, 4);

            bind_code_alt_temp [4] = ( byte ) page;

            System.arraycopy(address, 0, bind_code_alt_temp, 5, 7);
            System.arraycopy(bind_code, 4, bind_code_alt_temp, 12, 3);
         }
         else
         {
            System.arraycopy(bind_code, 0, bind_code_alt_temp, 0,
                             (bind_code.length > 15 ? 15
                                                    : bind_code.length));
         }

         bind_code = bind_code_alt_temp;
      }

      System.arraycopy(bind_code, 0, bind_code_temp, 8, 15);

      bind_code = bind_code_temp;

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Binding Secret to iButton");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("page: " + page);
         IOHelper.writeLine("secret_number: " + (secret_number&7));
         IOHelper.writeLine("bind_data");
         IOHelper.writeBytesHex(bind_data);
         IOHelper.writeLine("bind_code");
         IOHelper.writeBytesHex(bind_code);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      if (!writeDataPage(page, bind_data))
         return false;

      resume = true;

      if (!writeScratchPad(page, 0, bind_code, 0, 32))
      {
         resume = false;

         return false;
      }

      if (!SHAFunction(COMPUTE_NEXT_SECRET))
      {
         resume = false;

         return false;   //means a failure
      }

      //go ahead and set resume = false, but write_read_copy_quick doesn't
      //check resume, it automatically assumes it can resume
      resume = false;

      //make sure the secret number is between 0 and 7
      secret_number = secret_number & 7;

      //the secret page is 16 for secrets 0-3, page 17 for secrets 4-7
      int secret_page = (secret_number > 3) ? 17
                                            : 16;

      //each page has 4 secrets, so look at 2 LS bits
      int secret_offset = (secret_number & 3) << 3;

      return (write_read_copy_quick(secret_page, secret_offset));
   }

   //used when copying secrets from the scratchpad to a secret page
   private synchronized boolean write_read_copy_quick (int secret_page,
           int secret_offset)
      throws OneWireIOException, OneWireException
   {

      //don't worry about doSpeed here, this should never be called before something else
      //that would call doSpeed
      int    addr   = (secret_page << 5) + secret_offset;
      byte[] buffer = byte_buffer;

      //assume we can resume
      buffer [0] = ( byte ) RESUME;
      buffer [1] = ( byte ) WRITE_SCRATCHPAD;
      buffer [2] = ( byte ) addr;          //(secret_page << 5);
      buffer [3] = ( byte ) (addr >> 8);   // secret_offset;

      int length = 32 - secret_offset;

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("write_read_copy_quick");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("write scratchpad");
         IOHelper.write("target address: 0x");
         IOHelper.writeHex((byte)(addr>>8));
         IOHelper.writeLineHex((byte)addr);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //write the scratchpad
      adapter.reset();
      System.arraycopy(FF, 0, buffer, 4, length + 2);
      adapter.dataBlock(buffer, 0, length + 6);

      if (CRC16.compute(buffer, 1, length + 5, 0) != 0xB001)
      {
         return false;
      }

      //here we want to read the scratchpad WITHOUT reading the rest of the data
      buffer [1] = ( byte ) READ_SCRATCHPAD;

      System.arraycopy(FF, 0, buffer, 2, 8);
      adapter.reset();
      adapter.dataBlock(buffer, 0, 5);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("read scratchpad");
         IOHelper.write("target address: 0x");
         IOHelper.writeHex((byte)buffer[3]);
         IOHelper.writeLineHex((byte)buffer[2]);
         IOHelper.write("ES: 0x");
         IOHelper.writeLineHex((byte)buffer[4]);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //here we just shoot the buffer back out to call copyScratchpad
      buffer [1] = COPY_SCRATCHPAD;

      adapter.reset();

      //adapter.dataBlock(buffer,0,5);
      adapter.dataBlock(buffer, 0, 8);

      if (buffer [7] == ( byte ) 0x0ff)
         return waitForSuccessfulFinish();
      else
         return true;
   }

   /**
    * <p>Writes a data page to the DS1963S.  This method is the equivalent of calling:
    * <ul>
    *     <li><code>eraseScratchPad(0);                                     </code></li>
    *     <li><code>writeScratchPad(page_number,0,page_data_array,0,32);    </code></li>
    *     <li><code>readScratchPad(buffer,0);                               </code></li>
    *     <li><code>copyScratchPad();                                       </code></li>
    * </ul>
    * However, this method makes several optimizations to help it
    * run faster.  Because of the optimizations, this is the preferred
    * way of writing data to a normal memory page on the DS1963S.</p>
    *
    * @param page_number page number to write
    * @param page_data page data to write (must be at least 32 bytes long)
    *
    * @return <code>true</code> if successful, <code>false</code> if the operation
    *         failed on a CRC error or while waiting for the DS1963S's output
    *         to alternate
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #eraseScratchPad(int)
    * @see #writeScratchPad(int,int,byte[],int,int)
    * @see #readScratchPad(byte[],int)
    * @see #copyScratchPad()
    */
   public synchronized boolean writeDataPage (int page_number,
                                              byte[] page_data)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Writing Data Page to iButton");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("page_number: " + page_number);
         IOHelper.writeLine("page_data");
         IOHelper.writeBytesHex(page_data);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      if (doSpeedEnable && (!resume))
         doSpeed();

      //first we need to erase the scratchpad
      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;

      buffer [1] = ERASE_SCRATCHPAD;
      buffer [2] = ( byte ) 0;
      buffer [3] = ( byte ) 0;

      // send block (check copy indication complete)
      System.arraycopy(FF, 0, buffer, 4, 3);
      adapter.dataBlock(buffer, 1, 6);

      if (buffer [6] == ( byte ) 0x0ff)
         if (!waitForSuccessfulFinish())
            return false;

      //then we need to write the scratchpad
      int addr = page_number << 5;

      buffer [0] = ( byte ) RESUME;
      buffer [1] = WRITE_SCRATCHPAD;
      buffer [2] = ( byte ) addr;
      buffer [3] = ( byte ) (addr >> 8);

      System.arraycopy(page_data, 0, buffer, 4, 32);

      buffer [36] = ( byte ) 0xff;
      buffer [37] = ( byte ) 0xff;   //leave space for the CRC

      adapter.reset();

      //adapter.putByte(RESUME);
      adapter.dataBlock(buffer, 0, 38);

      if (CRC16.compute(buffer, 1, 37, 0) != 0xB001)
      {
         return false;
      }

      //then we need to do a 'read' to get out TA1,TA2,E/S
      adapter.reset();

      //buffer[0] = RESUME;
      buffer [1] = READ_SCRATCHPAD;

      System.arraycopy(FF, 0, buffer, 2, 37);
      adapter.dataBlock(buffer, 0, 39);

      //match the scratchpad contents before copying
      for(int i=0; i<32; i++)
      {
         if(page_data[i]!=buffer[5+i])
         {
            return false;
         }
      }

      //Don't skip this!
      TA1 = buffer[2];
      TA2 = buffer[3];
      ES  = buffer[4];
      int length = 32 - (TA1 & 31);

      if (CRC16.compute(buffer, 1, 6 + length, 0) != 0xB001)
      {
         return false;
      }

      //now we can copy the scratchpad
      adapter.reset();

      //buffer[0] still has the resume command in it
      buffer [1] = COPY_SCRATCHPAD;

      //TA1,TA2,ES area already in buffer
      System.arraycopy(FF, 0, buffer, 5, 3);
      adapter.dataBlock(buffer, 0, 8);

      if (buffer [7] == ( byte ) 0x0ff)
         return waitForSuccessfulFinish();

      return true;
   }

   /**
    * <p>Performs one of the DS1963S's cryptographic functions.  See the datasheet for
    * more information on these functions.</p>
    *
    * Valid parameters for the <code>function</code> argument are:
    * <ul>
    *    <li> COMPUTE_FIRST_SECRET  </li>
    *    <li> COMPUTE_NEXT_SECRET   </li>
    *    <li> VALIDATE_DATA_PAGE    </li>
    *    <li> SIGN_DATA_PAGE        </li>
    *    <li> COMPUTE_CHALLENGE     </li>
    *    <li> AUTH_HOST             </li>
    * </ul>
    * <p>This method uses the last target address used by this
    * <code>OneWireContainer</code>.  These are read in the
    * <code>copyScratchPad()</code> and <code>readScratchPad()</code>
    * methods.</p>
    *
    * @param function the SHA function code
    *
    * @return <code>true</code> if the function successfully completed,
    *         <code>false</code> if the operation failed on a CRC error
    *         or while waiting for the DS1963S's output to alternate
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAFunction(byte,int)
    * @see #copyScratchPad()
    * @see #readScratchPad(byte[],int)
    */
   public boolean SHAFunction (byte function)
      throws OneWireIOException, OneWireException
   {
      return SHAFunction(function, (TA1 & 0x0ff) | (TA2 << 8));
   }

   /**
    * <p>Performs one of the DS1963S's cryptographic functions.  See the datasheet for
    * more information on these functions.</p>
    *
    * Valid parameters for the <code>function</code> argument are:
    * <ul>
    *    <li> COMPUTE_FIRST_SECRET  </li>
    *    <li> COMPUTE_NEXT_SECRET   </li>
    *    <li> VALIDATE_DATA_PAGE    </li>
    *    <li> SIGN_DATA_PAGE        </li>
    *    <li> COMPUTE_CHALLENGE     </li>
    *    <li> AUTH_HOST             </li>
    * </ul>
    * <p>This method uses the last target address used by this
    * <code>OneWireContainer</code>.  These are read in the
    * <code>copyScratchPad()</code> and <code>readScratchPad()</code>
    * methods.</p>
    *
    * @param function the SHA function code
    * @param T the raw target address for the operation
    *
    * @return <code>true</code> if the function successfully completed,
    *         <code>false</code> if the operation failed on a CRC error
    *         or while waiting for the DS1963S's output to alternate
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAFunction(byte)
    * @see #copyScratchPad()
    * @see #readScratchPad(byte[],int)
    */
   public synchronized boolean SHAFunction (byte function, int T)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("SHA Function");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(address);
         IOHelper.writeLine("adapter.getSpeed()="+adapter.getSpeed());
         IOHelper.writeLine("adapter.getPortTypeDescription()="+adapter.getPortTypeDescription());
         IOHelper.writeLine("this.speed="+this.speed);
         IOHelper.writeLine("device isPresent: "+adapter.isPresent(this.address));
         IOHelper.write("function: 0x");
         IOHelper.writeLineHex(function);
         IOHelper.write("target address: 0x");
         IOHelper.writeHex((byte)(T>>8));
         IOHelper.writeLineHex((byte)T);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      if (doSpeedEnable && (!resume))
         doSpeed();

      if (!resume)
         adapter.select(address);
      else
      {
         adapter.reset();
         adapter.putByte(RESUME);
      }

      byte[] buffer = byte_buffer;

      buffer [0] = COMPUTE_SHA;
      buffer [1] = ( byte ) T;
      buffer [2] = ( byte ) (T >> 8);
      buffer [3] = function;

      System.arraycopy(FF, 0, buffer, 4, 5);

      if(DEBUG)
      {
         IOHelper.writeLine("buffer:");
         IOHelper.writeBytesHex(buffer,0,9);
      }

      adapter.dataBlock(buffer, 0, 9);

      if(DEBUG)
      {
         IOHelper.writeLine("buffer:");
         IOHelper.writeBytesHex(buffer,0,9);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if (CRC16.compute(buffer, 0, 6, 0) != 0xB001)
      {
         return false;
      }

      if (buffer [8] == ( byte ) 0x0ff)
         return waitForSuccessfulFinish();

      return true;
   }
}



