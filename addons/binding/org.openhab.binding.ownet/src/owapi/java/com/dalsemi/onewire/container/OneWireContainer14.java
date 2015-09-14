
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
 * <P> 1-Wire container for 32 byte EEPROM memory iButton, DS1971 and 1-Wire Chip, DS2430A. 
 * This container encapsulates the functionality of the 1-Wire family 
 * type <B>14</B> (hex)</P>
 *
 * <P> The iButton package for this device is primarily used as a read/write portable memory device.  
 * The 1-Wire Chip version is used for small non-volatile storage. </P>
 * 
 * <H3> Features </H3> 
 * <UL>
 *   <LI> 256 bits (32 bytes) Electrically Erasable Programmable Read Only Memory
 *        (EEPROM)
 *   <LI> Memory organized as one 256-bit (32-byte) page
 *   <LI> 64-bit one-time programmable application register is automatically 
 *        write-protected after programming
 *   <LI> Reduces control, address, data and power to
 *        a single data pin
 *   <LI> Reads and writes over a wide voltage range
 *        of 2.8V to 6.0V from -40&#176C to +85&#176C environments
 * </UL>
 * 
 * <H3> Alternate Names </H3>
 * <UL>
 *   <LI> DS2430A 
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
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile needs-power-delivery
 *         <LI> <I> Pages</I> 1 pages of length 32 bytes giving 29 bytes Packet data payload
 *      </UL> 
 *   <LI> <B> Application register, non-volatile when locked </B>
 *      <UL> 
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 *                  {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} 
 *         <LI> <I> Size </I> 8 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose volatile needs-power-delivery
 *         <LI> <I> Pages</I> 1 pages of length 8 bytes giving 6 bytes Packet data payload pages-lockable
 *         <LI> <I> Extra information for each page</I>  Page Locked flag, length 1
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
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1971.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1971.pdf</A>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2430A.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2430A.pdf</A>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OneWireContainer14
 * 
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public class OneWireContainer14
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
   public OneWireContainer14 ()
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
    * @see #OneWireContainer14() OneWireContainer14 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer14 (DSPortAdapter sourceAdapter, byte[] newAddress)
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
    * @see #OneWireContainer14() OneWireContainer14 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer14 (DSPortAdapter sourceAdapter, long newAddress)
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
    * @see #OneWireContainer14() OneWireContainer14 
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer14 (DSPortAdapter sourceAdapter, String newAddress)
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
      return "DS1971";
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
      return "DS2430A";
   }

   /**
    * Get a short description of the function of this iButton 
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "Electrically Erasable Programmable Read Only Memory "
             + "(EEPROM) organized as one page of 256 bits and 64 bit "
             + "one-time programmable application register.";
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
      Vector bank_vector = new Vector(1);

      // EEPROM
      bank_vector.addElement(new MemoryBankEE(this));

      // Application register (lockable)
      bank_vector.addElement(new MemoryBankAppReg(this));

      return bank_vector.elements();
   }
}
