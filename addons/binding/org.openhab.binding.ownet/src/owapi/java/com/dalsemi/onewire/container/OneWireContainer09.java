
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
 * <P> 1-Wire container for 128 byte Add-Only memory (EPROM) iButton, DS1982 and 1-Wire Chip, DS2502. 
 * This container encapsulates the functionality of the 1-Wire family 
 * type <B>09</B> (hex)</P>
 *
 * <P> The iButton package for this device is primarily used as a read/write portable memory device.  
 * The 1-Wire Chip version is used for small non-volatile storage. </P>
 * 
 * <H3> Features </H3> 
 * <UL>
 *   <LI> 1024 bits (128 bytes) Electrically Programmable Read-Only
 *        Memory (EPROM) communicates with
 *        the economy of one signal plus ground
 *   <LI> EPROM partitioned into four 256-bit (32-byte) pages
 *        for randomly accessing packetized data
 *   <LI> Each memory page can be permanently
 *        write-protected to prevent tampering
 *   <LI> Device is an "add only" memory where
 *        additional data can be programmed into
 *        EPROM without disturbing existing data
 *   <LI> Architecture allows software to patch data by
 *        superseding an old page in favor of a newly
 *        programmed page
 *   <LI> Overdrive mode boosts communication to
 *        142 kbits per second
 *   <LI> Reads over a wide voltage range of 2.8V to
 *        6.0V from -40&#176C to +85&#176C; programs at
 *        11.5V to 12.0V from -40&#176C to +50&#176C
 * </UL>
 * 
 * <H3> Alternate Names </H3>
 * <UL>
 *   <LI> D2502
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
 *         <LI> <I> Size </I> 128 starting at physical address 0
 *         <LI> <I> Features</I> Write-once general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 4 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC pages-redirectable pages-lockable
 *      </UL> 
 *   <LI> <B> Write protect pages and Page redirection </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 8 starting at physical address 0 (in STATUS memory area)
 *         <LI> <I> Features</I> Write-once not-general-purpose non-volatile needs-program-pulse
 *         <LI> <I> Pages</I> 1 pages of length 8 bytes
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
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2502.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2502.pdf</A>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1982.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1982.pdf</A>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OTPMemoryBank
 * @see com.dalsemi.onewire.container.OneWireContainer0B
 * @see com.dalsemi.onewire.container.OneWireContainer0F
 * 
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public class OneWireContainer09
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
   public OneWireContainer09 ()
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
    * @see #OneWireContainer09() OneWireContainer09 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer09 (DSPortAdapter sourceAdapter, byte[] newAddress)
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
    * @see #OneWireContainer09() OneWireContainer09 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer09 (DSPortAdapter sourceAdapter, long newAddress)
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
    * @see #OneWireContainer09() OneWireContainer09 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer09 (DSPortAdapter sourceAdapter, String newAddress)
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
      return "DS1982";
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
      return "DS2502";
   }

   /**
    * Get a short description of the function of this iButton 
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "1024 bit Electrically Programmable Read Only Memory "
             + "(EPROM) partitioned into four 256 bit pages."
             + "Each memory page can be permanently write-protected "
             + "to prevent tampering.  Architecture allows software "
             + "to patch data by supersending a used page in favor of "
             + "a newly programmed page.";
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
      Vector bank_vector = new Vector(2);

      // EPROM main bank
      MemoryBankEPROM mn = new MemoryBankEPROM(this);

      mn.numberPages          = 4;
      mn.size                 = 128;
      mn.pageLength           = 32;
      mn.extraInfo            = false;
      mn.extraInfoLength      = 0;
      mn.extraInfoDescription = null;
      mn.numCRCBytes          = 1;
      mn.normalReadCRC        = true;
      mn.READ_PAGE_WITH_CRC   = ( byte ) 0xC3;

      bank_vector.addElement(mn);

      // EPROM status write protect pages bank
      MemoryBankEPROM st = new MemoryBankEPROM(this);

      st.bankDescription      = "Write protect pages and Page redirection";
      st.numberPages          = 1;
      st.size                 = 8;
      st.pageLength           = 8;
      st.generalPurposeMemory = false;
      st.extraInfo            = false;
      st.extraInfoLength      = 0;
      st.extraInfoDescription = null;
      st.numCRCBytes          = 1;
      st.normalReadCRC        = true;
      st.READ_PAGE_WITH_CRC   = MemoryBankEPROM.STATUS_READ_PAGE_COMMAND;
      st.WRITE_MEMORY_COMMAND = MemoryBankEPROM.STATUS_WRITE_COMMAND;

      bank_vector.addElement(st);

      // setup OTP features in main memory
      mn.mbLock         = st;
      mn.lockPage       = true;
      mn.mbRedirect     = st;
      mn.redirectOffset = 1;
      mn.redirectPage   = true;

      return bank_vector.elements();
   }
}
