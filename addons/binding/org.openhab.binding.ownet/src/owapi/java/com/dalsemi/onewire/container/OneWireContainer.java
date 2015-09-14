
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2004 Dallas Semiconductor Corporation, All Rights Reserved.
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
import com.dalsemi.onewire.utils.Address;
import java.util.Enumeration;
import java.util.Vector;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.OneWireException;


/**
 * A <code>OneWireContainer</code> encapsulates the <code>DSPortAdapter</code>,
 * the 1-Wire&#174 network address, and methods to manipulate a specific 1-Wire device. A
 * 1-Wire device may be in the form of a stainless steel armored can, called an iButton&#174,
 * or in standard IC plastic packaging.
 *
 * <p>General 1-Wire device container class with basic communication functions.
 * This class should only be used if a device specific class is not available
 * or known.  Most <code>OneWireContainer</code> classes will extend this basic class.
 *
 * <P> 1-Wire devices with memory can be accessed through the objects that
 * are returned from the {@link #getMemoryBanks() getMemoryBanks} method. See the
 * usage example below. </P>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example 1</H4>
 * Enumerate memory banks retrieved from the OneWireContainer
 * instance 'owd' and cast to the highest interface.  See the
 * interface descriptions
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank OTPMemoryBank}
 * for specific examples.
 * <PRE> <CODE>
 *  MemoryBank      mb;
 *  PagedMemoryBank pg_mb;
 *  OTPMemoryBank   otp_mb;
 *
 *  for(Enumeration bank_enum = owd.getMemoryBanks();
 *                      bank_enum.hasMoreElements(); )
 *  {
 *     // get the next memory bank, cast to MemoryBank
 *     mb = (MemoryBank)bank_enum.nextElement();
 *
 *     // check if has paged services
 *     if (mb instanceof PagedMemoryBank)
 *         pg_mb = (PagedMemoryBank)mb;
 *
 *     // check if has One-Time-Programable services
 *     if (mb instanceof OTPMemoryBank)
 *         otp_mb = (OTPMemoryBank)mb;
 *  }
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OTPMemoryBank
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
public class OneWireContainer
{
   //--------
   //-------- Variables
   //--------

   /**
    * Reference to the adapter that is needed to communicate with this
    * iButton or 1-Wire device.
    */
   protected DSPortAdapter adapter;

   /**
    * 1-Wire Network Address of this iButton or 1-Wire
    * device.
    * Family code is byte at offset 0.
    * @see com.dalsemi.onewire.utils.Address
    */
   protected byte[] address;

   /**
    * Temporary copy of 1-Wire Network Address of this
    * iButton or 1-Wire device.
    * @see com.dalsemi.onewire.utils.Address
    */
   private byte[] addressCopy;

   /**
    * Communication speed requested.
    * <ul>
    * <li>     0 (SPEED_REGULAR)
    * <li>     1 (SPEED_FLEX)
    * <li>     2 (SPEED_OVERDRIVE)
    * <li>     3 (SPEED_HYPERDRIVE)
    * <li>    >3 future speeds
    * </ul>
    *
    * @see DSPortAdapter#setSpeed
    */
   protected int speed;

   /**
    * Flag to indicate that falling back to a slower speed then requested
    * is OK.
    */
   protected boolean speedFallBackOK;

   //--------
   //-------- Constructors
   //--------

   /**
    * Create an empty container.  Must call <code>setupContainer</code> before
    * using this new container.<p>
    *
    * This is one of the methods to construct a container.  The others are
    * through creating a OneWireContainer with parameters.
    *
    * @see #OneWireContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer(DSPortAdapter,long)
    * @see #OneWireContainer(DSPortAdapter,String)
    * @see #setupContainer(DSPortAdapter,byte[])
    * @see #setupContainer(DSPortAdapter,long)
    * @see #setupContainer(DSPortAdapter,String)
    */
   public OneWireContainer ()
   {
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton.
    * @param  newAddress        address of this 1-Wire device
    * @see #OneWireContainer()
    * @see com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      this.setupContainer(sourceAdapter, newAddress);
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton.
    * @param  newAddress        address of this 1-Wire device
    * @see #OneWireContainer()
    * @see com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer (DSPortAdapter sourceAdapter, long newAddress)
   {
      this.setupContainer(sourceAdapter, newAddress);
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton.
    * @param  newAddress        address of this 1-Wire device
    * @see #OneWireContainer()
    * @see com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer (DSPortAdapter sourceAdapter, String newAddress)
   {
      this.setupContainer(sourceAdapter, newAddress);
   }

   //--------
   //-------- Setup and adapter methods
   //--------

   /**
    * Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer (DSPortAdapter sourceAdapter, byte[] newAddress)
   {

      // get a reference to the source adapter (will need this to communicate)
      adapter = sourceAdapter;

      // set the Address
      synchronized (this)
      {
         address     = new byte [8];
         addressCopy = new byte [8];

         System.arraycopy(newAddress, 0, address, 0, 8);
      }

      // set desired speed to be SPEED_REGULAR by default with no fallback
      speed           = DSPortAdapter.SPEED_REGULAR;
      speedFallBackOK = false;
   }

   /**
    * Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer (DSPortAdapter sourceAdapter, long newAddress)
   {

      // get a reference to the source adapter (will need this to communicate)
      adapter = sourceAdapter;

      // set the Address
      synchronized (this)
      {
         address     = Address.toByteArray(newAddress);
         addressCopy = new byte [8];
      }

      // set desired speed to be SPEED_REGULAR by default with no fallback
      speed           = DSPortAdapter.SPEED_REGULAR;
      speedFallBackOK = false;
   }

   /**
    * Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer (DSPortAdapter sourceAdapter, String newAddress)
   {

      // get a reference to the source adapter (will need this to communicate)
      adapter = sourceAdapter;

      // set the Address
      synchronized (this)
      {
         address     = Address.toByteArray(newAddress);
         addressCopy = new byte [8];
      }

      // set desired speed to be SPEED_REGULAR by default with no fallback
      speed           = DSPortAdapter.SPEED_REGULAR;
      speedFallBackOK = false;
   }

   /**
    * Retrieves the port adapter object used to create this container.
    *
    * @return port adapter instance
    */
   public DSPortAdapter getAdapter ()
   {
      return adapter;
   }

   //--------
   //-------- Device information methods
   //--------

   /**
    * Retrieves the Dallas Semiconductor part number of the 1-Wire device
    * as a <code>String</code>.  For example 'Crypto iButton' or 'DS1992'.
    *
    * @return 1-Wire device name
    */
   public String getName ()
   {
      synchronized (this)
      {
         return "Device type: "
                + (((address [0] & 0x0FF) < 16)
                   ? ("0" + Integer.toHexString(address [0] & 0x0FF))
                   : Integer.toHexString(address [0] & 0x0FF));
      }
   }

   /**
    * Retrieves the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of 1-Wire Network devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * 'Crypto iButton'.
    *
    * @return 1-Wire device alternate names
    */
   public String getAlternateNames ()
   {
      return "";
   }

   /**
    * Retrieves a short description of the function of the 1-Wire device type.
    *
    * @return device functional description
    */
   public String getDescription ()
   {
      return "No description available.";
   }

   /**
    * Sets the maximum speed for this container.  Note this may be slower then the
    * devices maximum speed.  This method can be used by an application
    * to restrict the communication rate due 1-Wire line conditions. <p>
    *
    * @param newSpeed
    * <ul>
    * <li>     0 (SPEED_REGULAR) set to normal communciation speed
    * <li>     1 (SPEED_FLEX) set to flexible communciation speed used
    *            for long lines
    * <li>     2 (SPEED_OVERDRIVE) set to normal communciation speed to
    *            overdrive
    * <li>     3 (SPEED_HYPERDRIVE) set to normal communciation speed to
    *            hyperdrive
    * <li>    >3 future speeds
    * </ul>
    *
    * @param fallBack boolean indicating it is OK to fall back to a slower
    *                 speed if true
    *
    */
   public void setSpeed (int newSpeed, boolean fallBack)
   {
      speed           = newSpeed;
      speedFallBackOK = fallBack;
   }

   /**
    * Returns the maximum speed this iButton or 1-Wire device can
    * communicate at.
    * Override this method if derived iButton type can go faster then
    * SPEED_REGULAR(0).
    *
    * @return maximum speed
    * @see DSPortAdapter#setSpeed
    */
   public int getMaxSpeed ()
   {
      return DSPortAdapter.SPEED_REGULAR;
   }

   /**
    * Gets the 1-Wire Network address of this device as an array of bytes.
    *
    * @return 1-Wire address
    * @see com.dalsemi.onewire.utils.Address
    */
   public byte[] getAddress ()
   {
      return address;
   }

   /**
    * Gets this device's 1-Wire Network address as a String.
    *
    * @return 1-Wire address
    * @see com.dalsemi.onewire.utils.Address
    */
   public String getAddressAsString ()
   {
      return Address.toString(address);
   }

   /**
    * Gets this device's 1-Wire Network address as a long.
    *
    * @return 1-Wire address
    * @see com.dalsemi.onewire.utils.Address
    */
   public long getAddressAsLong ()
   {
      return Address.toLong(address);
   }

   /**
    * Returns an <code>Enumeration</code> of <code>MemoryBank</code>.  Default is no memory banks.
    *
    * @return enumeration of memory banks to read and write memory
    *   on this iButton or 1-Wire device
    * @see MemoryBank
    */
   public Enumeration getMemoryBanks ()
   {
      return new Vector(0).elements();
   }

   //--------
   //-------- I/O Methods
   //--------

   /**
    * Verifies that the iButton or 1-Wire device is present on
    * the 1-Wire Network.
    *
    * @return  <code>true</code> if device present on the 1-Wire Network
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         a read back verification fails.
    * @throws OneWireException if adapter is not open
    */
   public boolean isPresent ()
      throws OneWireIOException,OneWireException
   {
      synchronized (this)
      {
         return adapter.isPresent(address);
      }
   }

   /**
    * Verifies that the iButton or 1-Wire device is present
    * on the 1-Wire Network and in an alarm state.  This does not
    * apply to all device types.
    *
    * @return  <code>true</code> if device present and in alarm condition
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         a read back verification fails.
    * @throws OneWireException if adapter is not open
    */
   public boolean isAlarming ()
      throws OneWireIOException,OneWireException
   {
      synchronized (this)
      {
         return adapter.isAlarming(address);
      }
   }

   /**
    * Go to the specified speed for this container.  This method uses the
    * containers selected speed (method setSpeed(speed, fallback)) and
    * will optionally fall back to a slower speed if communciation failed.
    * Only call this method once to get the device into the desired speed
    * as long as the device is still responding.
    *
    * @throws OneWireIOException WHEN selected speed fails and fallback
    *                                 is false
    * @throws OneWireException WHEN hypterdrive is selected speed
    * @see #setSpeed(int,boolean)
    */
   public void doSpeed ()
      throws OneWireIOException, OneWireException
   {
      boolean is_present = false;

      try
      {
         // check if already at speed and device present
         if ((speed == adapter.getSpeed()) && adapter.isPresent(address))
            return;
      }
      catch (OneWireIOException e)
      {
         // VOID
      }

      // speed Overdrive
      if (speed == DSPortAdapter.SPEED_OVERDRIVE)
      {
         try
         {
            // get this device and adapter to overdrive
            adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
            adapter.reset();
            adapter.putByte(( byte ) 0x69);
            adapter.setSpeed(DSPortAdapter.SPEED_OVERDRIVE);
         }
         catch (OneWireIOException e)
         {
            // VOID
         }

         // get copy of address
         synchronized (this)
         {
            System.arraycopy(address, 0, addressCopy, 0, 8);
            adapter.dataBlock(addressCopy, 0, 8);
         }

         try
         {
            is_present = adapter.isPresent(address);
         }
         catch (OneWireIOException e)
         {
            // VOID
         }

         // check if new speed is OK
         if (!is_present)
         {

            // check if allow fallback
            if (speedFallBackOK)
               adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
            else
               throw new OneWireIOException(
                  "Failed to get device to selected speed (overdrive)");
         }
      }
      // speed regular or flex
      else if ((speed == DSPortAdapter.SPEED_REGULAR)
               || (speed == DSPortAdapter.SPEED_FLEX))
         adapter.setSpeed(speed);
      // speed hyperdrive, don't know how to do this
      else
         throw new OneWireException(
            "Speed selected (hyperdrive) is not supported by this method");
   }

   //--------
   //-------- Object Methods
   //--------

   /**
    * Returns a hash code value for the object. This method is
    * supported for the benefit of hashtables such as those provided by
    * <code>java.util.Hashtable</code>.
    *
    * @return  a hash code value for this object.
    * @see     java.util.Hashtable
    */
   public int hashCode()
   {
      if(this.address==null) 
         return 0;
      else
         return (new Long(Address.toLong(this.address))).hashCode();
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    * @param   obj   the reference object with which to compare.
    * @return  <code>true</code> if this object is the same as the obj
    *          argument; <code>false</code> otherwise.
    */
   public boolean equals(Object obj)
   {
      if(obj==this)
         return true;

      if(obj instanceof OneWireContainer)
      {
         OneWireContainer owc = (OneWireContainer)obj;
         // don't claim that all subclasses of a specific container are
         // equivalent to the parent container
         if(owc.getClass()==this.getClass())
            return owc.getAddressAsLong()==this.getAddressAsLong();
      }

      return false;
   }

   /**
    * Returns a string representation of the object.
    *
    * @return  a string representation of the object.
    */
   public String toString()
   {
      return Address.toString(this.address) + " " + this.getName();
   }
}
