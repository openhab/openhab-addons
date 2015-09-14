
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

package com.dalsemi.onewire.adapter;

// imports
import java.util.Enumeration;
import com.dalsemi.onewire.OneWireException;
import java.util.Vector;
import java.lang.ClassNotFoundException;
import java.io.File;


/**
 * The DSPortAdapter class for all TMEX native adapters (Win32).
 *
 * Instances of valid DSPortAdapter's are retrieved from methods in
 * {@link com.dalsemi.onewire.OneWireAccessProvider OneWireAccessProvider}.
 *
 * <P>The TMEXAdapter methods can be organized into the following categories: </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #getAdapterName() getAdapterName}
 *       <LI> {@link #getPortTypeDescription() getPortTypeDescription}
 *       <LI> {@link #getClassVersion() getClassVersion}
 *       <LI> {@link #adapterDetected() adapterDetected}
 *       <LI> {@link #getAdapterVersion() getAdapterVersion}
 *       <LI> {@link #getAdapterAddress() getAdapterAddress}
 *     </UL>
 *   <LI> <B> Port Selection </B>
 *     <UL>
 *       <LI> {@link #getPortNames() getPortNames}
 *       <LI> {@link #selectPort(String) selectPort}
 *       <LI> {@link #getPortName() getPortName}
 *       <LI> {@link #freePort() freePort}
 *     </UL>
 *   <LI> <B> Adapter Capabilities </B>
 *     <UL>
 *       <LI> {@link #canOverdrive() canOverdrive}
 *       <LI> {@link #canHyperdrive() canHyperdrive}
 *       <LI> {@link #canFlex() canFlex}
 *       <LI> {@link #canProgram() canProgram}
 *       <LI> {@link #canDeliverPower() canDeliverPower}
 *       <LI> {@link #canDeliverSmartPower() canDeliverSmartPower}
 *       <LI> {@link #canBreak() canBreak}
 *     </UL>
 *   <LI> <B> 1-Wire Network Semaphore </B>
 *     <UL>
 *       <LI> {@link #beginExclusive(boolean) beginExclusive}
 *       <LI> {@link #endExclusive() endExclusive}
 *     </UL>
 *   <LI> <B> 1-Wire Device Discovery </B>
 *     <UL>
 *       <LI> Selective Search Options
 *         <UL>
 *          <LI> {@link #targetAllFamilies() targetAllFamilies}
 *          <LI> {@link #targetFamily(int) targetFamily(int)}
 *          <LI> {@link #targetFamily(byte[]) targetFamily(byte[])}
 *          <LI> {@link #excludeFamily(int) excludeFamily(int)}
 *          <LI> {@link #excludeFamily(byte[]) excludeFamily(byte[])}
 *          <LI> {@link #setSearchOnlyAlarmingDevices() setSearchOnlyAlarmingDevices}
 *          <LI> {@link #setNoResetSearch() setNoResetSearch}
 *          <LI> {@link #setSearchAllDevices() setSearchAllDevices}
 *         </UL>
 *       <LI> Search With Automatic 1-Wire Container creation
 *         <UL>
 *          <LI> {@link #getAllDeviceContainers() getAllDeviceContainers}
 *          <LI> {@link #getFirstDeviceContainer() getFirstDeviceContainer}
 *          <LI> {@link #getNextDeviceContainer() getNextDeviceContainer}
 *         </UL>
 *       <LI> Search With NO 1-Wire Container creation
 *         <UL>
 *          <LI> {@link #findFirstDevice() findFirstDevice}
 *          <LI> {@link #findNextDevice() findNextDevice}
 *          <LI> {@link #getAddress(byte[]) getAddress(byte[])}
 *          <LI> {@link #getAddressAsLong() getAddressAsLong}
 *          <LI> {@link #getAddressAsString() getAddressAsString}
 *         </UL>
 *       <LI> Manual 1-Wire Container creation
 *         <UL>
 *          <LI> {@link #getDeviceContainer(byte[]) getDeviceContainer(byte[])}
 *          <LI> {@link #getDeviceContainer(long) getDeviceContainer(long)}
 *          <LI> {@link #getDeviceContainer(String) getDeviceContainer(String)}
 *          <LI> {@link #getDeviceContainer() getDeviceContainer()}
 *         </UL>
 *     </UL>
 *   <LI> <B> 1-Wire Network low level access (usually not called directly) </B>
 *     <UL>
 *       <LI> Device Selection and Presence Detect
 *         <UL>
 *          <LI> {@link #isPresent(byte[]) isPresent(byte[])}
 *          <LI> {@link #isPresent(long) isPresent(long)}
 *          <LI> {@link #isPresent(String) isPresent(String)}
 *          <LI> {@link #isAlarming(byte[]) isAlarming(byte[])}
 *          <LI> {@link #isAlarming(long) isAlarming(long)}
 *          <LI> {@link #isAlarming(String) isAlarming(String)}
 *          <LI> {@link #select(byte[]) select(byte[])}
 *          <LI> {@link #select(long) select(long)}
 *          <LI> {@link #select(String) select(String)}
 *         </UL>
 *       <LI> Raw 1-Wire IO
 *         <UL>
 *          <LI> {@link #reset() reset}
 *          <LI> {@link #putBit(boolean) putBit}
 *          <LI> {@link #getBit() getBit}
 *          <LI> {@link #putByte(int) putByte}
 *          <LI> {@link #getByte() getByte}
 *          <LI> {@link #getBlock(int) getBlock(int)}
 *          <LI> {@link #getBlock(byte[], int) getBlock(byte[], int)}
 *          <LI> {@link #getBlock(byte[], int, int) getBlock(byte[], int, int)}
 *          <LI> {@link #dataBlock(byte[], int, int) dataBlock(byte[], int, int)}
 *         </UL>
 *       <LI> 1-Wire Speed and Power Selection
 *         <UL>
 *          <LI> {@link #setPowerDuration(int) setPowerDuration}
 *          <LI> {@link #startPowerDelivery(int) startPowerDelivery}
 *          <LI> {@link #setProgramPulseDuration(int) setProgramPulseDuration}
 *          <LI> {@link #startProgramPulse(int) startProgramPulse}
 *          <LI> {@link #startBreak() startBreak}
 *          <LI> {@link #setPowerNormal() setPowerNormal}
 *          <LI> {@link #setSpeed(int) setSpeed}
 *          <LI> {@link #getSpeed() getSpeed}
 *         </UL>
 *     </UL>
 *   <LI> <B> Advanced </B>
 *     <UL>
 *        <LI> {@link #registerOneWireContainerClass(int, Class) registerOneWireContainerClass}
 *     </UL>
 *  </UL>
 *
 * @see com.dalsemi.onewire.OneWireAccessProvider
 * @see com.dalsemi.onewire.container.OneWireContainer
 *
 * @version    0.01, 20 March 2001
 * @author     DS
 */
public class TMEXAdapter
   extends DSPortAdapter
{

   //--------
   //-------- Variables
   //--------

   /** flag to intidate if native driver got loaded */
   private static boolean driverLoaded = false;

   /** TMEX port type number (0-15) */
   protected int portType;

   /** Current 1-Wire Network Address */
   protected byte[] RomDta = new byte [8];

   /** Flag to indicate next search will look only for alarming devices */
   private boolean doAlarmSearch = false;

   /** Flag to indicate next search will be a 'first' */
   private boolean resetSearch = true;

   /** Flag to indicate next search will not be preceeded by a 1-Wire reset */
   private boolean skipResetOnSearch = false;

   //--------
   //-------- Constructors/Destructor
   //--------

   /**
    * Constructs a default adapter
    *
    * @throws ClassNotFoundException
    */
   public TMEXAdapter ()
      throws ClassNotFoundException
   {

      // check if native driver got loaded
      if (!driverLoaded)
         throw new ClassNotFoundException(
            "native driver 'ibtmjava.dll' not loaded");

      // set default port type
      portType = getDefaultTypeNumber();

      // attempt to set the portType, will throw exception if does not exist
      if (!setPortType_Native(portType))
         throw new ClassNotFoundException("TMEX adapter type does not exist");
   }

   /**
    * Constructs with a specified port type
    *
    *
    * @param newPortType
    * @throws ClassNotFoundException
    */
   public TMEXAdapter (int newPortType)
      throws ClassNotFoundException
   {

      // set default port type
      portType = newPortType;

      // check if native driver got loaded
      if (!driverLoaded)
         throw new ClassNotFoundException(
            "native driver 'ibtmjava.dll' not loaded");

      // attempt to set the portType, will throw exception if does not exist
      if (!setPortType_Native(portType))
         throw new ClassNotFoundException("TMEX adapter type does not exist");
   }

   /**
    * Finalize to Cleanup native
    */
   protected void finalize ()
   {
      cleanup_Native();
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Retrieve the name of the port adapter as a string.  The 'Adapter'
    * is a device that connects to a 'port' that allows one to
    * communicate with an iButton or other 1-Wire device.  As example
    * of this is 'DS9097U'.
    *
    * @return  <code>String</code> representation of the port adapter.
    */
   public native String getAdapterName ();

   /**
    * Retrieve a description of the port required by this port adapter.
    * An example of a 'Port' would 'serial communication port'.
    *
    * @return  <code>String</code> description of the port type required.
    */
   public native String getPortTypeDescription ();

   /**
    * Retrieve a version string for this class.
    *
    * @return  version string
    */
   public String getClassVersion ()
   {
      return new String("0.01, native: " + getVersion_Native());
   }

   //--------
   //-------- Port Selection
   //--------

   /**
    * Retrieve a list of the platform appropriate port names for this
    * adapter.  A port must be selected with the method 'selectPort'
    * before any other communication methods can be used.  Using
    * a communcation method before 'selectPort' will result in
    * a <code>OneWireException</code> exception.
    *
    * @return  enumeration of type <code>String</code> that contains the port
    * names
    */
   public Enumeration getPortNames ()
   {
      Vector portVector = new Vector();
      String header     = getPortNameHeader_Native();

      for (int i = 0; i < 16; i++)
         portVector.addElement(new String(header + Integer.toString(i)));

      return (portVector.elements());
   }

   /**
    * Specify a platform appropriate port name for this adapter.  Note that
    * even though the port has been selected, it's ownership may be relinquished
    * if it is not currently held in a 'exclusive' block.  This class will then
    * try to re-aquire the port when needed.  If the port cannot be re-aquired
    * ehen the exception <code>PortInUseException</code> will be thrown.
    *
    * @param  portName  name of the target port, retrieved from
    * getPortNames()
    *
    * @return <code>true</code> if the port was aquired, <code>false</code>
    * if the port is not available.
    *
    * @throws OneWireIOException If port does not exist, or unable to communicate with port.
    * @throws OneWireException If port does not exist
    */
   public native boolean selectPort (String portName)
      throws OneWireIOException, OneWireException;

   /**
    * Free ownership of the selected port if it is currently owned back
    * to the system.  This should only be called if the recently
    * selected port does not have an adapter or at the end of
    * your application's use of the port.
    *
    * @throws OneWireException If port does not exist
    */
   public native void freePort ()
      throws OneWireException;

   /**
    * Retrieve the name of the selected port as a <code>String</code>.
    *
    * @return  <code>String</code> of selected port
    *
    * @throws OneWireException if valid port not yet selected
    */
   public native String getPortName ()
      throws OneWireException;

   //--------
   //-------- Adapter detection
   //--------

   /**
    * Detect adapter presence on the selected port.
    *
    * @return  <code>true</code> if the adapter is confirmed to be connected to
    * the selected port, <code>false</code> if the adapter is not connected.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public native boolean adapterDetected ()
      throws OneWireIOException, OneWireException;

   /**
    * Retrieve the version of the adapter.
    *
    * @return  <code>String</code> of the adapter version.  It will return
    * "<na>" if the adapter version is not or cannot be known.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public native String getAdapterVersion ()
      throws OneWireIOException, OneWireException;

   /**
    * Retrieve the address of the adapter if it has one.
    *
    * @return  <code>String</code> of the adapter address.  It will return "<na>" if
    * the adapter does not have an address.  The address is a string representation of an
    * 1-Wire address.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    * @see    com.dalsemi.onewire.utils.Address
    */
   public String getAdapterAddress ()
      throws OneWireIOException, OneWireException
   {
      return "<na>";   //??? implement later
   }

   //--------
   //-------- Adapter features
   //--------

   /* The following interogative methods are provided so that client code
    * can react selectively to underlying states without generating an
    * exception.
    */

   /**
    * Returns whether adapter can physically support overdrive mode.
    *
    * @return  <code>true</code> if this port adapter can do OverDrive,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canOverdrive ()
      throws OneWireIOException, OneWireException;

   /**
    * Returns whether the adapter can physically support hyperdrive mode.
    *
    * @return  <code>true</code> if this port adapter can do HyperDrive,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canHyperdrive ()
      throws OneWireIOException, OneWireException;

   /**
    * Returns whether the adapter can physically support flex speed mode.
    *
    * @return  <code>true</code> if this port adapter can do flex speed,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canFlex ()
      throws OneWireIOException, OneWireException;

   /**
    * Returns whether adapter can physically support 12 volt power mode.
    *
    * @return  <code>true</code> if this port adapter can do Program voltage,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canProgram ()
      throws OneWireIOException, OneWireException;

   /**
    * Returns whether the adapter can physically support strong 5 volt power
    * mode.
    *
    * @return  <code>true</code> if this port adapter can do strong 5 volt
    * mode, <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canDeliverPower ()
      throws OneWireIOException, OneWireException;

   /**
    * Returns whether the adapter can physically support "smart" strong 5
    * volt power mode.  "smart" power delivery is the ability to deliver
    * power until it is no longer needed.  The current drop it detected
    * and power delivery is stopped.
    *
    * @return  <code>true</code> if this port adapter can do "smart" strong
    * 5 volt mode, <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canDeliverSmartPower ()
      throws OneWireIOException, OneWireException;

   /**
    * Returns whether adapter can physically support 0 volt 'break' mode.
    *
    * @return  <code>true</code> if this port adapter can do break,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error with the adapter
    * @throws OneWireException on a setup error with the 1-Wire
    *         adapter
    */
   public native boolean canBreak ()
      throws OneWireIOException, OneWireException;

   //--------
   //-------- Finding iButtons and 1-Wire devices
   //--------

   /**
    * Returns <code>true</code> if the first iButton or 1-Wire device
    * is found on the 1-Wire Network.
    * If no devices are found, then <code>false</code> will be returned.
    *
    * @return  <code>true</code> if an iButton or 1-Wire device is found.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public boolean findFirstDevice ()
      throws OneWireIOException, OneWireException
   {

      // reset the internal rom buffer
      resetSearch = true;

      return findNextDevice();
   }

   /**
    * Returns <code>true</code> if the next iButton or 1-Wire device
    * is found. The previous 1-Wire device found is used
    * as a starting point in the search.  If no more devices are found
    * then <code>false</code> will be returned.
    *
    * @return  <code>true</code> if an iButton or 1-Wire device is found.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public boolean findNextDevice ()
      throws OneWireIOException, OneWireException
   {
      boolean retval;

      while (true)
      {
         retval = romSearch_Native(skipResetOnSearch, resetSearch,
                                   doAlarmSearch, RomDta);

         if (retval)
         {
            resetSearch = false;

            // check if this is an OK family type
            if (isValidFamily(RomDta))
               return true;

            // Else, loop to the top and do another search.
         }
         else
         {
            resetSearch = true;

            return false;
         }
      }
   }

   /**
    * Copies the 'current' iButton address being used by the adapter into
    * the array.  This address is the last iButton or 1-Wire device found
    * in a search (findNextDevice()...).
    * This method copies into a user generated array to allow the
    * reuse of the buffer.  When searching many iButtons on the one
    * wire network, this will reduce the memory burn rate.
    *
    * @param  address An array to be filled with the current iButton address.
    * @see    com.dalsemi.onewire.utils.Address
    */
   public void getAddress (byte[] address)
   {
      System.arraycopy(RomDta, 0, address, 0, 8);
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present on
    * the 1-Wire Network. This does not affect the 'current' device
    * state information used in searches (findNextDevice...).
    *
    * @param  address  device address to verify is present
    *
    * @return  <code>true</code> if device is present else
    *         <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see    com.dalsemi.onewire.utils.Address
    */
   public native boolean isPresent (byte[] address)
      throws OneWireIOException, OneWireException;

   /**
    * Verifies that the iButton or 1-Wire device specified is present
    * on the 1-Wire Network and in an alarm state. This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * @param  address  device address to verify is present and alarming
    *
    * @return  <code>true</code> if device is present and alarming else
    * <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see    com.dalsemi.onewire.utils.Address
    */
   public native boolean isAlarming (byte[] address)
      throws OneWireIOException, OneWireException;

   /**
    * Selects the specified iButton or 1-Wire device by broadcasting its
    * address.  This operation is refered to a 'MATCH ROM' operation
    * in the iButton and 1-Wire device data sheets.  This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * Warning, this does not verify that the device is currently present
    * on the 1-Wire Network (See isPresent).
    *
    * @param  address     iButton to select
    *
    * @return  <code>true</code> if device address was sent,<code>false</code>
    * otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[] address)
    * @see  com.dalsemi.onewire.utils.Address
    */
   public native boolean select (byte[] address)
      throws OneWireIOException, OneWireException;

   //--------
   //-------- Finding iButton/1-Wire device options
   //--------

   /**
    * Set the 1-Wire Network search to find only iButtons and 1-Wire
    * devices that are in an 'Alarm' state that signals a need for
    * attention.  Not all iButton types
    * have this feature.  Some that do: DS1994, DS1920, DS2407.
    * This selective searching can be canceled with the
    * 'setSearchAllDevices()' method.
    *
    * @see #setNoResetSearch
    */
   public void setSearchOnlyAlarmingDevices ()
   {
      doAlarmSearch = true;
   }

   /**
    * Set the 1-Wire Network search to not perform a 1-Wire
    * reset before a search.  This feature is chiefly used with
    * the DS2409 1-Wire coupler.
    * The normal reset before each search can be restored with the
    * 'setSearchAllDevices()' method.
    */
   public void setNoResetSearch ()
   {
      skipResetOnSearch = true;
   }

   /**
    * Set the 1-Wire Network search to find all iButtons and 1-Wire
    * devices whether they are in an 'Alarm' state or not and
    * restores the default setting of providing a 1-Wire reset
    * command before each search. (see setNoResetSearch() method).
    *
    * @see #setNoResetSearch
    */
   public void setSearchAllDevices ()
   {
      doAlarmSearch     = false;
      skipResetOnSearch = false;
   }

   //--------
   //-------- 1-Wire Network Semaphore methods
   //--------

   /**
    * Gets exclusive use of the 1-Wire to communicate with an iButton or
    * 1-Wire Device.
    * This method should be used for critical sections of code where a
    * sequence of commands must not be interrupted by communication of
    * threads with other iButtons, and it is permissible to sustain
    * a delay in the special case that another thread has already been
    * granted exclusive access and this access has not yet been
    * relinquished. <p>
    *
    * It can be called through the OneWireContainer
    * class by the end application if they want to ensure exclusive
    * use.  If it is not called around several methods then it
    * will be called inside each method.
    *
    * @param blocking <code>true</code> if want to block waiting
    *                 for an excluse access to the adapter
    * @return <code>true</code> if blocking was false and a
    *         exclusive session with the adapter was aquired
    *
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public native boolean beginExclusive (boolean blocking)
      throws OneWireException;

   /**
    * Relinquishes exclusive control of the 1-Wire Network.
    * This command dynamically marks the end of a critical section and
    * should be used when exclusive control is no longer needed.
    */
   public native void endExclusive ();

   //--------
   //-------- Primitive 1-Wire Network data methods
   //--------

   /**
    * Sends a bit to the 1-Wire Network.
    *
    * @param  bitValue  the bit value to send to the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public void putBit (boolean bitValue)
      throws OneWireIOException, OneWireException
   {
      if (dataBit_Native(bitValue) != bitValue)
         throw new OneWireIOException("Error during putBit()");
   }

   /**
    * Gets a bit from the 1-Wire Network.
    *
    * @return  the bit value recieved from the the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public boolean getBit ()
      throws OneWireIOException, OneWireException
   {
      return dataBit_Native(true);
   }

   /**
    * Sends a byte to the 1-Wire Network.
    *
    * @param  byteValue  the byte value to send to the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public void putByte (int byteValue)
      throws OneWireIOException, OneWireException
   {
      if (dataByte_Native(byteValue & 0x00FF) != ((0x00FF) & byteValue))
         throw new OneWireIOException(
            "Error during putByte(), echo was incorrect ");
   }

   /**
    * Gets a byte from the 1-Wire Network.
    *
    * @return  the byte value received from the the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public int getByte ()
      throws OneWireIOException, OneWireException
   {
      return dataByte_Native(0x00FF);
   }

   /**
    * Get a block of data from the 1-Wire Network.
    *
    * @param  len  length of data bytes to receive
    *
    * @return  the data received from the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public byte[] getBlock (int len)
      throws OneWireIOException, OneWireException
   {
      byte[] barr = new byte [len];

      getBlock(barr, 0, len);

      return barr;
   }

   /**
    * Get a block of data from the 1-Wire Network and write it into
    * the provided array.
    *
    * @param  arr     array in which to write the received bytes
    * @param  len     length of data bytes to receive
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public void getBlock (byte[] arr, int len)
      throws OneWireIOException, OneWireException
   {
      getBlock(arr, 0, len);
   }

   /**
    * Get a block of data from the 1-Wire Network and write it into
    * the provided array.
    *
    * @param  arr     array in which to write the received bytes
    * @param  off     offset into the array to start
    * @param  len     length of data bytes to receive
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public native void getBlock (byte[] arr, int off, int len)
      throws OneWireIOException, OneWireException;

   /**
    * Sends a block of data and returns the data received in the same array.
    * This method is used when sending a block that contains reads and writes.
    * The 'read' portions of the data block need to be pre-loaded with 0xFF's.
    * It starts sending data from the index at offset 'off' for length 'len'.
    *
    * @param  dataBlock  array of data to transfer to and from the 1-Wire Network.
    * @param  off        offset into the array of data to start
    * @param  len        length of data to send / receive starting at 'off'
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public native void dataBlock (byte dataBlock [], int off, int len)
      throws OneWireIOException, OneWireException;

   /**
    * Sends a Reset to the 1-Wire Network.
    *
    * @return  the result of the reset. Potential results are:
    * <ul>
    * <li> 0 (RESET_NOPRESENCE) no devices present on the 1-Wire Network.
    * <li> 1 (RESET_PRESENCE) normal presence pulse detected on the 1-Wire
    *        Network indicating there is a device present.
    * <li> 2 (RESET_ALARM) alarming presence pulse detected on the 1-Wire
    *        Network indicating there is a device present and it is in the
    *        alarm condition.  This is only provided by the DS1994/DS2404
    *        devices.
    * <li> 3 (RESET_SHORT) inticates 1-Wire appears shorted.  This can be
    *        transient conditions in a 1-Wire Network.  Not all adapter types
    *        can detect this condition.
    * </ul>
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public native int reset ()
      throws OneWireIOException, OneWireException;

   //--------
   //-------- 1-Wire Network power methods
   //--------

   /**
    * Sets the duration to supply power to the 1-Wire Network.
    * This method takes a time parameter that indicates the program
    * pulse length when the method startPowerDelivery().<p>
    *
    * Note: to avoid getting an exception,
    * use the canDeliverPower() and canDeliverSmartPower()
    * method to check it's availability. <p>
    *
    * @param timeFactor
    * <ul>
    * <li>   0 (DELIVERY_HALF_SECOND) provide power for 1/2 second.
    * <li>   1 (DELIVERY_ONE_SECOND) provide power for 1 second.
    * <li>   2 (DELIVERY_TWO_SECONDS) provide power for 2 seconds.
    * <li>   3 (DELIVERY_FOUR_SECONDS) provide power for 4 seconds.
    * <li>   4 (DELIVERY_SMART_DONE) provide power until the
    *          the device is no longer drawing significant power.
    * <li>   5 (DELIVERY_INFINITE) provide power until the
    *          setPowerNormal() method is called.
    * </ul>
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public void setPowerDuration (int timeFactor)
      throws OneWireIOException, OneWireException
   {

      // Right now we only support infinite pull up.
      if (timeFactor != DELIVERY_INFINITE)
         throw new OneWireException(
            "No support for other than infinite power duration");
   }

   /**
    * Sets the 1-Wire Network voltage to supply power to an iButton device.
    * This method takes a time parameter that indicates whether the
    * power delivery should be done immediately, or after certain
    * conditions have been met. <p>
    *
    * Note: to avoid getting an exception,
    * use the canDeliverPower() and canDeliverSmartPower()
    * method to check it's availability. <p>
    *
    * @param changeCondition
    * <ul>
    * <li>   0 (CONDITION_NOW) operation should occur immediately.
    * <li>   1 (CONDITION_AFTER_BIT) operation should be pending
    *           execution immediately after the next bit is sent.
    * <li>   2 (CONDITION_AFTER_BYTE) operation should be pending
    *           execution immediately after next byte is sent.
    * </ul>
    *
    * @return <code>true</code> if the voltage change was successful,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public native boolean startPowerDelivery (int changeCondition)
      throws OneWireIOException, OneWireException;

   /**
    * Sets the duration for providing a program pulse on the
    * 1-Wire Network.
    * This method takes a time parameter that indicates the program
    * pulse length when the method startProgramPulse().<p>
    *
    * Note: to avoid getting an exception,
    * use the canDeliverPower() method to check it's
    * availability. <p>
    *
    * @param timeFactor
    * <ul>
    * <li>   6 (DELIVERY_EPROM) provide program pulse for 480 microseconds
    * <li>   5 (DELIVERY_INFINITE) provide power until the
    *          setPowerNormal() method is called.
    * </ul>
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public void setProgramPulseDuration (int timeFactor)
      throws OneWireIOException, OneWireException
   {
      if (timeFactor != DELIVERY_EPROM)
         throw new OneWireException(
            "Only support EPROM length program pulse duration");
   }

   /**
    * Sets the 1-Wire Network voltage to eprom programming level.
    * This method takes a time parameter that indicates whether the
    * power delivery should be done immediately, or after certain
    * conditions have been met. <p>
    *
    * Note: to avoid getting an exception,
    * use the canProgram() method to check it's
    * availability. <p>
    *
    * @param changeCondition
    * <ul>
    * <li>   0 (CONDITION_NOW) operation should occur immediately.
    * <li>   1 (CONDITION_AFTER_BIT) operation should be pending
    *           execution immediately after the next bit is sent.
    * <li>   2 (CONDITION_AFTER_BYTE) operation should be pending
    *           execution immediately after next byte is sent.
    * </ul>
    *
    * @return <code>true</code> if the voltage change was successful,
    * <code>false</code> otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *         or the adapter does not support this operation
    */
   public native boolean startProgramPulse (int changeCondition)
      throws OneWireIOException, OneWireException;

   /**
    * Sets the 1-Wire Network voltage to 0 volts.  This method is used
    * rob all 1-Wire Network devices of parasite power delivery to force
    * them into a hard reset.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *         or the adapter does not support this operation
    */
   public native void startBreak ()
      throws OneWireIOException, OneWireException;

   /**
    * Sets the 1-Wire Network voltage to normal level.  This method is used
    * to disable 1-Wire conditions created by startPowerDelivery and
    * startProgramPulse.  This method will automatically be called if
    * a communication method is called while an outstanding power
    * command is taking place.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *         or the adapter does not support this operation
    */
   public native void setPowerNormal ()
      throws OneWireIOException, OneWireException;

   //--------
   //-------- 1-Wire Network speed methods
   //--------

   /**
    * This method takes an int representing the new speed of data
    * transfer on the 1-Wire Network. <p>
    *
    * @param speed
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
    * @param desiredSpeed
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *         or the adapter does not support this operation
    */
   public native void setSpeed (int desiredSpeed)
      throws OneWireIOException, OneWireException;

   /**
    * This method returns the current data transfer speed through a
    * port to a 1-Wire Network. <p>
    *
    * @return
    * <ul>
    * <li>     0 (SPEED_REGULAR) set to normal communication speed
    * <li>     1 (SPEED_FLEX) set to flexible communication speed used
    *            for long lines
    * <li>     2 (SPEED_OVERDRIVE) set to normal communication speed to
    *            overdrive
    * <li>     3 (SPEED_HYPERDRIVE) set to normal communication speed to
    *            hyperdrive
    * <li>    >3 future speeds
    * </ul>
    */
   public native int getSpeed ();

   //--------
   //-------- Misc
   //--------

   /**
    * Select the TMEX specified port type (0 to 15)  Use this
    * method if the constructor with the PortType cannot be used.
    *
    *
    * @param newPortType
    * @return  true if port type valid.  Instance is only usable
    *          if this returns false.
    */
   public boolean setTMEXPortType (int newPortType)
   {

      // set default port type
      portType = newPortType;

      // attempt to set the portType, return result
      return setPortType_Native(portType);
   }

   //--------
   //-------- Additional Native Methods
   //--------

   /**
    * CleanUp the native state for classes owned by the provided
    * thread.
    */
   public static native void CleanUpByThread (Thread thread);

   /**
    * Get the default Adapter Name.
    *
    * @return  String containing the name of the default adapter
    */
   public static native String getDefaultAdapterName ();

   /**
    * Get the default Adapter Port name.
    *
    * @return  String containing the name of the default adapter port
    */
   public static native String getDefaultPortName ();

   /**
    * Get the default Adapter Type number.
    *
    * @return  int, the default adapter type
    */
   private static native int getDefaultTypeNumber ();

   /**
    * Attempt to set the desired TMEX Port type.  This native
    * call will attempt to get a session handle to verify that
    * the portType exists.
    *
    * @return  true if portType exists, false if not
    */
   private native boolean setPortType_Native (int portType);

   /**
    * Perform a 1-Wire bit operation
    *
    * @param  bitValue  boolean bit value, true=1, false=0 to send
    *                   to 1-Wire net
    *
    * @return  boolean true for 1 return , false for 0 return
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   private native boolean dataBit_Native (boolean bitValue)
      throws OneWireIOException, OneWireException;

   /**
    * Perform a 1-Wire byte operation
    *
    * @param  byteValue  integer with ls byte containing the 8 bits value
    *                    to send to the 1-Wire net
    *
    * @return  int containing the 1-Wire return 8 bits in the ls byte.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   private native int dataByte_Native (int byteValue)
      throws OneWireIOException, OneWireException;

   /**
    * Get the TMEX main and porttype version strings concatinated
    *
    * @return  string containing the TMEX version
    */
   private native String getVersion_Native ();

   /**
    * Peform a search
    *
    * @param skipResetOnSearch  boolean, true to skip 1-Wire reset on search
    * @param resetSearch  boolean, true to reset search (First)
    * @param doAlarmSearch boolean, true if only want to find alarming
    * @param RomDta       byte array to hold ROM of device found
    *
    * @return  boolean, true if search found a device else false
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   private native boolean romSearch_Native (boolean skipResetOnSearch,
                                            boolean resetSearch,
                                            boolean doAlarmSearch,
                                            byte[] RomDta)
      throws OneWireIOException, OneWireException;

   /**
    * Return the port name header (taken from porttype version)
    *
    * @return  String containing the port name header
    */
   private native String getPortNameHeader_Native ();

   /**
    * Cleanup native (called on finalize of this instance)
    */
   private native void cleanup_Native ();

   //--------
   //-------- Native driver loading
   //--------

   /**
    * Static method called before instance is created.  Attempt
    * verify native driver's installed and to load the
    * driver (IBTMJAVA.DLL).
    */
   static
   {
      driverLoaded = false;

      // check if on OS that can have native TMEX drivers
      if ((System.getProperty("os.arch").indexOf("86") != -1)
              && (System.getProperty("os.name").indexOf("Windows") != -1))
      {

         // check if TMEX native drivers installed
         int     index       = 0, last_index = 0;
         String  search_path = System.getProperty("java.library.path");
         String  path;
         File    file;
         boolean tmex_loaded = false;

         // check for a path to search
         if (search_path != null)
         {
            // loop to look through the library search path
            do
            {
               index = search_path.indexOf(File.pathSeparatorChar, last_index);

               if (index > -1)
               {
                  path = search_path.substring(last_index, index);

                  // look to see if IBFS32.DLL is in this path
                  file = new File(path + File.separator + "IBFS32.DLL");

                  if (file.exists())
                  {
                     tmex_loaded = true;

                     break;
                  }
               }

               last_index = index + 1;
            }
            while (index > -1);
         }
         // jdk must not support "java.library.path" so assume it is loaded
         else
            tmex_loaded = true;

         if (tmex_loaded)
         {
            try
            {
               System.loadLibrary("ibtmjava");

               driverLoaded = true;
            }
            catch (UnsatisfiedLinkError e)
            {
               if (search_path != null)
               {
                  System.err.println(
                     "Could not load Java to TMEX-native bridge driver: ibtmjava.dll");
               }
               else
               {
                  System.err.println(
                     "Native drivers not found, download iButton-TMEX RTE Win32 from www.ibutton.com");
               }
            }
         }
         else
            System.err.println(
               "Native drivers not found, download iButton-TMEX RTE Win32 from www.ibutton.com");
      }
   }
}
