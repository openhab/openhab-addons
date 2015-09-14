
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
 * <P> 1-Wire container for 2048 byte Add-Only memory (EPROM) iButton, DS1985 and 1-Wire Chip, DS2505. 
 * This container encapsulates the functionality of the 1-Wire family 
 * type <B>0B</B> (hex)</P>
 *
 * <P> The iButton package for this device is primarily used as a read/write portable memory device.  
 * The 1-Wire Chip version is used for small non-volatile storage. </P>
 * 
 * <H3> Features </H3> 
 * <UL>
 *   <LI> 16384 bits (2048 bytes) Electrically Programmable Read-Only
 *        Memory (EPROM) communicates with
 *        the economy of one signal plus ground
 *   <LI> EPROM partitioned into 256-bit (32-byte) pages
 *        for randomly accessing packetized data
 *   <LI> Each memory page can be permanently
 *        write-protected to prevent tampering
 *   <LI> Device is an "add only" memory where
 *        additional data can be programmed into
 *        EPROM without disturbing existing data
 *   <LI> Architecture allows software to patch data by
 *        superseding an old page in favor of a newly
 *        programmed page
 *   <LI> Reads over a wide voltage range of 2.8V to
 *        6.0V from -40&#176C to +85&#176C; programs at
 *        11.5V to 12.0V from -40&#176C to +50&#176C
 * </UL>
 * 
 * <H3> Alternate Names </H3>
 * <UL>
 *   <LI> D2505
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
 *   <LI> <B> Main Memory </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 2048 starting at physical address 0
 *         <LI> <I> Features</I> Write-once general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 64 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC pages-redirectable pages-lockable redirection-lockable
 *         <LI> <I> Extra information for each page </I>  Inverted redirection page, length 1
 *      </UL> 
 *   <LI> <B> Write protect pages </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 8 starting at physical address 0 (in STATUS memory area)
 *         <LI> <I> Features</I> Write-once not-general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 1 pages of length 8 bytes
 *         <LI> <I> Page Features </I> page-device-CRC 
 *      </UL> 
 *   <LI> <B> Write protect redirection </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 8 starting at physical address 32 (in STATUS memory area)
 *         <LI> <I> Features</I> Write-once not-general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 1 pages of length 8 bytes
 *         <LI> <I> Page Features </I> page-device-CRC 
 *      </UL> 
 *   <LI> <B> Bitmap of used pages for file structure </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 8 starting at physical address 64 (in STATUS memory area)
 *         <LI> <I> Features</I> Write-once not-general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 1 pages of length 8 bytes
 *         <LI> <I> Page Features </I> page-device-CRC 
 *      </UL> 
 *   <LI> <B> Page redirection bytes </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 64 starting at physical address 256 (in STATUS memory area)
 *         <LI> <I> Features</I> Write-once not-general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 8 pages of length 8 bytes
 *         <LI> <I> Page Features </I> page-device-CRC 
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
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, and
 * {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}
 * for bank specific operations.
 * </DL>
 *
 * <H3> DataSheets </H3> 
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2505.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2505.pdf</A>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1985.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1985.pdf</A>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OTPMemoryBank
 * @see com.dalsemi.onewire.container.OneWireContainer09
 * @see com.dalsemi.onewire.container.OneWireContainer0F
 * 
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public class OneWireContainer0B
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
   public OneWireContainer0B ()
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
    * @see #OneWireContainer0B() OneWireContainer0B 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer0B (DSPortAdapter sourceAdapter, byte[] newAddress)
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
    * @see #OneWireContainer0B() OneWireContainer0B 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer0B (DSPortAdapter sourceAdapter, long newAddress)
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
    * @see #OneWireContainer0B() OneWireContainer0B 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer0B (DSPortAdapter sourceAdapter, String newAddress)
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
      return "DS1985";
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
      return "DS2505";
   }

   /**
    * Get a short description of the function of this iButton 
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "16384 bit Electrically Programmable Read Only Memory "
             + "(EPROM) partitioned into sixty-four 256 bit pages."
             + "Each memory page can be permanently write-protected "
             + "to prevent tampering.  Architecture allows software "
             + "to patch data by supersending a used page in favor of "
             + "a newly programmed page.";
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
      Vector bank_vector = new Vector(5);

      // EPROM main bank
      MemoryBankEPROM mn = new MemoryBankEPROM(this);

      bank_vector.addElement(mn);

      // EPROM status write protect pages bank
      MemoryBankEPROM wp = new MemoryBankEPROM(this);

      wp.bankDescription      = "Write protect pages";
      wp.numberPages          = 1;
      wp.size                 = 8;
      wp.pageLength           = 8;
      wp.generalPurposeMemory = false;
      wp.extraInfo            = false;
      wp.extraInfoLength      = 0;
      wp.extraInfoDescription = null;
      wp.crcAfterAddress      = false;
      wp.READ_PAGE_WITH_CRC   = MemoryBankEPROM.STATUS_READ_PAGE_COMMAND;
      wp.WRITE_MEMORY_COMMAND = MemoryBankEPROM.STATUS_WRITE_COMMAND;

      bank_vector.addElement(wp);

      // EPROM status write protect redirection bank
      MemoryBankEPROM wpr = new MemoryBankEPROM(this);

      wpr.bankDescription      = "Write protect redirection";
      wpr.numberPages          = 1;
      wpr.size                 = 8;
      wpr.pageLength           = 8;
      wpr.generalPurposeMemory = false;
      wpr.extraInfo            = false;
      wpr.extraInfoLength      = 0;
      wpr.extraInfoDescription = null;
      wpr.crcAfterAddress      = false;
      wpr.READ_PAGE_WITH_CRC   = MemoryBankEPROM.STATUS_READ_PAGE_COMMAND;
      wpr.WRITE_MEMORY_COMMAND = MemoryBankEPROM.STATUS_WRITE_COMMAND;
      wpr.startPhysicalAddress = 32;

      bank_vector.addElement(wpr);

      // EPROM status bitmap
      MemoryBankEPROM bm = new MemoryBankEPROM(this);

      bm.bankDescription      = "Bitmap of used pages for file structure";
      bm.numberPages          = 1;
      bm.size                 = 8;
      bm.pageLength           = 8;
      bm.generalPurposeMemory = false;
      bm.extraInfo            = false;
      bm.extraInfoLength      = 0;
      bm.extraInfoDescription = null;
      bm.crcAfterAddress      = false;
      bm.startPhysicalAddress = 64;
      bm.READ_PAGE_WITH_CRC   = MemoryBankEPROM.STATUS_READ_PAGE_COMMAND;
      bm.WRITE_MEMORY_COMMAND = MemoryBankEPROM.STATUS_WRITE_COMMAND;

      bank_vector.addElement(bm);

      // EPROM status redirection
      MemoryBankEPROM rd = new MemoryBankEPROM(this);

      rd.bankDescription      = "Page redirection bytes";
      rd.generalPurposeMemory = false;
      rd.numberPages          = 8;
      rd.size                 = 64;
      rd.pageLength           = 8;
      rd.extraInfo            = false;
      rd.extraInfoLength      = 0;
      rd.extraInfoDescription = null;
      rd.crcAfterAddress      = false;
      rd.startPhysicalAddress = 256;
      rd.READ_PAGE_WITH_CRC   = MemoryBankEPROM.STATUS_READ_PAGE_COMMAND;
      rd.WRITE_MEMORY_COMMAND = MemoryBankEPROM.STATUS_WRITE_COMMAND;

      bank_vector.addElement(rd);

      // setup OTP features in main memory
      mn.mbLock           = wp;
      mn.mbRedirect       = rd;
      mn.mbLockRedirect   = wpr;
      mn.redirectPage     = true;
      mn.lockPage         = true;
      mn.lockRedirectPage = true;

      return bank_vector.elements();
   }
}
