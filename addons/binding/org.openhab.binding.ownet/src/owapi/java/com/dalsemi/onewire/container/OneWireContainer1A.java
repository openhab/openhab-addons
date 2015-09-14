
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

// imports
import com.dalsemi.onewire.adapter.DSPortAdapter;
import java.util.Vector;
import java.util.Enumeration;


/**
 * <P> 1-Wire container for 512 byte memory iButton with write cycle counters, DS1963L.  This container
 * encapsulates the functionality of the iButton family 
 * type <B>1A</B> (hex)</P>
 *
 * <P> This iButton is primarily used as a read/write portable memory device that
 * provides tamper detection when utilizing the write counters. </P>
 * 
 * <H3> Features </H3> 
 * <UL>
 *   <LI> 4096 bits (512 bytes) of read/write nonvolatile memory
 *   <LI> 256-bit (32-byte) scratchpad ensures integrity of data
 *        transfer
 *   <LI> Memory partitioned into 256-bit (32-byte) pages for
 *        packetizing data
 *   <LI> Data integrity assured with strict read/write
 *        protocols
 *   <LI> Overdrive mode boosts communication to
 *        142 kbits per second
 *   <LI> Four 32-bit read-only non rolling-over page
 *        write cycle counters
 *   <LI> 32 factory-preset tamper-detect bits to
 *        indicate physical intrusion
 *   <LI> On-chip 16-bit CRC generator for
 *        safeguarding data transfers
 *   <LI> Operating temperature range from -40&#176C to
 *        +70&#176C
 *   <LI> Over 10 years of data retention
 * </UL>
 * 
 * <H3> Memory </H3> 
 *  
 * <P> The memory can be accessed through the objects that are returned
 * from the {@link #getMemoryBanks() getMemoryBanks} method. </P>
 * 
 * The following is a list of the MemoryBank instances that are returned: 
 *
 * <UL>
 *   <LI> <B> Scratchpad Ex </B> 
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                   {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write not-general-purpose volatile
 *         <LI> <I> Pages</I> 1 pages of length 32 bytes 
 *         <LI> <I> Extra information for each page</I>  Target address, offset, length 3
 *      </UL> 
 *   <LI> <B> Main Memory </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 384 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 12 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC 
 *      </UL> 
 *   <LI> <B> Memory with write cycle counter </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 128 starting at physical address 384
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 4 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC 
 *         <LI> <I> Extra information for each page</I>  Write cycle counter, length 8
 *      </UL> 
 * </UL>
 * 
 * <H3> Usage </H3> 
 * 
 * <DL> 
 * <DD> See the usage example in 
 * {@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer}
 * to enumerate the MemoryBanks.
 * <DD> See the usage examples in 
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank} and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * for bank specific operations.
 * </DL>
 *
 * <H3> DataSheet </H3> 
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1963L.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1963L.pdf</A>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OneWireContainer18
 * 
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public class OneWireContainer1A
   extends OneWireContainer
{

   //--------
   //-------- Constructors
   //--------

   /**
    * Create an empty container that is not complete until after a call 
    * to <code>setupContainer</code>. <p>
    *
    * This is one of the methods to construct a container.  The others are
    * through creating a OneWireContainer with parameters.
    *
    * @see #setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) super.setupContainer()
    */
   public OneWireContainer1A ()
   {
      super();
   }

   /**
    * Create a container with the provided adapter instance
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter instance used to communicate with
    * this iButton
    * @param  newAddress        {@link com.dalsemi.onewire.utils.Address Address}  
    *                           of this 1-Wire device
    *
    * @see #OneWireContainer1A() OneWireContainer1A 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer1A (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with the provided adapter instance
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter instance used to communicate with
    * this 1-Wire device
    * @param  newAddress        {@link com.dalsemi.onewire.utils.Address Address}
    *                            of this 1-Wire device
    *
    * @see #OneWireContainer1A() OneWireContainer1A 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer1A (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with the provided adapter instance
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter instance used to communicate with
    * this 1-Wire device
    * @param  newAddress        {@link com.dalsemi.onewire.utils.Address Address}
    *                            of this 1-Wire device
    *
    * @see #OneWireContainer1A() OneWireContainer1A 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer1A (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Get the Dallas Semiconductor part number of the iButton
    * or 1-Wire Device as a string.  For example 'DS1992'.
    *
    * @return iButton or 1-Wire device name
    */
   public String getName ()
   {
      return "DS1963L";
   }

   /**
    * Get the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of 1-Wire Network devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * 'Crypto iButton'.
    *
    * @return 1-Wire device alternate names
    */
   public String getAlternateNames ()
   {
      return "Monetary iButton";
   }

   /**
    * Get a short description of the function of this iButton 
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "4096 bit read/write nonvolatile memory with "
             + "four 32-bit read-only non rolling-over page write "
             + "cycle counters and tamper-detect bits for small "
             + "money storage";
   }

   /**
    * Get the maximum speed this iButton or 1-Wire device can
    * communicate at.
    * Override this method if derived iButton type can go faster then
    * SPEED_REGULAR(0).
    *
    * @return maximum speed
    * @see com.dalsemi.onewire.container.OneWireContainer#setSpeed super.setSpeed
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#SPEED_REGULAR DSPortAdapter.SPEED_REGULAR
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#SPEED_OVERDRIVE DSPortAdapter.SPEED_OVERDRIVE
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#SPEED_FLEX DSPortAdapter.SPEED_FLEX
    */
   public int getMaxSpeed ()
   {
      return DSPortAdapter.SPEED_OVERDRIVE;
   }

   /**
    * Get an enumeration of memory bank instances that implement one or more
    * of the following interfaces:
    * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
    * and {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}. 
    * @return <CODE>Enumeration</CODE> of memory banks 
    */
   public Enumeration getMemoryBanks ()
   {
      Vector bank_vector = new Vector(3);

      // scratchpad
      MemoryBankScratch scratch = new MemoryBankScratchEx(this);

      bank_vector.addElement(scratch);

      // NVRAM (no counters)
      MemoryBankNV nv = new MemoryBankNVCRC(this, scratch);

      nv.numberPages     = 12;
      nv.size            = 384;
      nv.extraInfoLength = 8;

      bank_vector.addElement(nv);

      // NVRAM (with write cycle counters)
      nv                      = new MemoryBankNVCRC(this, scratch);
      nv.numberPages          = 4;
      nv.size                 = 128;
      nv.bankDescription      = "Memory with write cycle counter";
      nv.startPhysicalAddress = 384;
      nv.extraInfo            = true;
      nv.extraInfoDescription = "Write cycle counter";
      nv.extraInfoLength      = 8;

      bank_vector.addElement(nv);

      return bank_vector.elements();
   }
}
