
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
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.utils.*;
import com.dalsemi.onewire.OneWireException;
import java.util.Vector;
import java.util.Hashtable;


/**
 * The abstract base class for all 1-Wire port
 * adapter objects.  An implementation class of this type is therefore
 * independent of the adapter type.  Instances of valid DSPortAdapter's are
 * retrieved from methods in
 * {@link com.dalsemi.onewire.OneWireAccessProvider OneWireAccessProvider}.
 *
 * <P>The DSPortAdapter methods can be organized into the following categories: </P>
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
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public abstract class DSPortAdapter
{

   //--------
   //-------- Finals
   //--------

   /** Speed modes for 1-Wire Network, regular                    */
   public static final int SPEED_REGULAR = 0;

   /** Speed modes for 1-Wire Network, flexible for long lines    */
   public static final int SPEED_FLEX = 1;

   /** Speed modes for 1-Wire Network, overdrive                  */
   public static final int SPEED_OVERDRIVE = 2;

   /** Speed modes for 1-Wire Network, hyperdrive                 */
   public static final int SPEED_HYPERDRIVE = 3;

   /** 1-Wire Network level, normal (weak 5Volt pullup)                            */
   public static final char LEVEL_NORMAL = 0;

   /** 1-Wire Network level, (strong 5Volt pullup, used for power delivery) */
   public static final char LEVEL_POWER_DELIVERY = 1;

   /** 1-Wire Network level, (strong pulldown to 0Volts, reset 1-Wire)      */
   public static final char LEVEL_BREAK = 2;

   /** 1-Wire Network level, (strong 12Volt pullup, used to program eprom ) */
   public static final char LEVEL_PROGRAM = 3;

   /** 1-Wire Network reset result = no presence */
   public static final int RESET_NOPRESENCE = 0x00;

   /** 1-Wire Network reset result = presence    */
   public static final int RESET_PRESENCE = 0x01;

   /** 1-Wire Network reset result = alarm       */
   public static final int RESET_ALARM = 0x02;

   /** 1-Wire Network reset result = shorted     */
   public static final int RESET_SHORT = 0x03;

   /** Condition for power state change, immediate                      */
   public static final int CONDITION_NOW = 0;

   /** Condition for power state change, after next bit communication   */
   public static final int CONDITION_AFTER_BIT = 1;

   /** Condition for power state change, after next byte communication  */
   public static final int CONDITION_AFTER_BYTE = 2;

   /** Duration used in delivering power to the 1-Wire, 1/2 second         */
   public static final int DELIVERY_HALF_SECOND = 0;

   /** Duration used in delivering power to the 1-Wire, 1 second           */
   public static final int DELIVERY_ONE_SECOND = 1;

   /** Duration used in delivering power to the 1-Wire, 2 seconds          */
   public static final int DELIVERY_TWO_SECONDS = 2;

   /** Duration used in delivering power to the 1-Wire, 4 second           */
   public static final int DELIVERY_FOUR_SECONDS = 3;

   /** Duration used in delivering power to the 1-Wire, smart complete     */
   public static final int DELIVERY_SMART_DONE = 4;

   /** Duration used in delivering power to the 1-Wire, infinite           */
   public static final int DELIVERY_INFINITE = 5;

   /** Duration used in delivering power to the 1-Wire, current detect     */
   public static final int DELIVERY_CURRENT_DETECT = 6;

   /** Duration used in delivering power to the 1-Wire, 480 us             */
   public static final int DELIVERY_EPROM = 7;

   //--------
   //-------- Variables
   //--------

   /**
    * Hashtable to contain the user replaced OneWireContainers
    */
   private Hashtable registeredOneWireContainerClasses = new Hashtable(5);

   /**
    * Byte array of families to include in search
    */
   private byte[] include;

   /**
    * Byte array of families to exclude from search
    */
   private byte[] exclude;

   //--------
   //-------- Methods
   //--------

   /**
    * Retrieves the name of the port adapter as a string.  The 'Adapter'
    * is a device that connects to a 'port' that allows one to
    * communicate with an iButton or other 1-Wire device.  As example
    * of this is 'DS9097U'.
    *
    * @return  <code>String</code> representation of the port adapter.
    */
   public abstract String getAdapterName ();

   /**
    * Retrieves a description of the port required by this port adapter.
    * An example of a 'Port' would 'serial communication port'.
    *
    * @return  <code>String</code> description of the port type required.
    */
   public abstract String getPortTypeDescription ();

   /**
    * Retrieves a version string for this class.
    *
    * @return  version string
    */
   public abstract String getClassVersion ();

   //--------
   //-------- Port Selection
   //--------

   /**
    * Retrieves a list of the platform appropriate port names for this
    * adapter.  A port must be selected with the method 'selectPort'
    * before any other communication methods can be used.  Using
    * a communcation method before 'selectPort' will result in
    * a <code>OneWireException</code> exception.
    *
    * @return  <code>Enumeration</code> of type <code>String</code> that contains the port
    * names
    */
   public abstract Enumeration getPortNames ();

   /**
    * Registers a user provided <code>OneWireContainer</code> class.
    * Using this method will override the Dallas Semiconductor provided
    * container class when using the getDeviceContainer() method.  The
    * registered container state is only stored for the current
    * instance of <code>DSPortAdapter</code>, and is not statically shared.
    * The <code>OneWireContainerClass</code> must extend
    * <code>com.dalsemi.onewire.container.OneWireContainer</code> otherwise a <code>ClassCastException</code>
    * will be thrown.
    * The older duplicate family will be removed from registration when
    * a collision occurs.
    * Passing null as a parameter for the <code>OneWireContainerClass</code> will result
    * in the removal of any entry associated with the family.
    *
    * @param family   the code of the family type to associate with this class.
    * @param OneWireContainerClass  User provided class
    *
    * @throws OneWireException If <code>OneWireContainerClass</code> is not found.
    * @throws ClassCastException If user supplied <code>OneWireContainer</code> does not
    * extend <code>com.dalsemi.onewire.container.OneWireContainer</code>.
    */
   public void registerOneWireContainerClass (int family,
                                              Class OneWireContainerClass)
      throws OneWireException
   {
      Class defaultibc = null;

      try
      {
         defaultibc =
            Class.forName("com.dalsemi.onewire.container.OneWireContainer");
      }
      catch (ClassNotFoundException e)
      {
         throw new OneWireException("Could not find OneWireContainer class");
      }

      Integer familyInt = new Integer(family);

      if (OneWireContainerClass == null)
      {

         // If a null is passed, remove the old container class.
         registeredOneWireContainerClasses.remove(familyInt);
      }
      else
      {
         if (defaultibc.isAssignableFrom(OneWireContainerClass))
         {

            // Put the new container class in the hashtable, replacing any old one.
            registeredOneWireContainerClasses.put(familyInt,
                                                  OneWireContainerClass);
         }
         else
         {
            throw new ClassCastException(
               "Does not extend com.dalsemi.onewire.container.OneWireContainer");
         }
      }
   }

   /**
    * Specifies a platform appropriate port name for this adapter.  Note that
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
   public abstract boolean selectPort (String portName)
      throws OneWireIOException, OneWireException;

   /**
    * Frees ownership of the selected port, if it is currently owned, back
    * to the system.  This should only be called if the recently
    * selected port does not have an adapter, or at the end of
    * your application's use of the port.
    *
    * @throws OneWireException If port does not exist
    */
   public abstract void freePort ()
      throws OneWireException;

   /**
    * Retrieves the name of the selected port as a <code>String</code>.
    *
    * @return  <code>String</code> of selected port
    *
    * @throws OneWireException if valid port not yet selected
    */
   public abstract String getPortName ()
      throws OneWireException;

   //--------
   //-------- Adapter detection
   //--------

   /**
    * Detects adapter presence on the selected port.
    *
    * @return  <code>true</code> if the adapter is confirmed to be connected to
    * the selected port, <code>false</code> if the adapter is not connected.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public abstract boolean adapterDetected ()
      throws OneWireIOException, OneWireException;

   /**
    * Retrieves the version of the adapter.
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
   public String getAdapterVersion ()
      throws OneWireIOException, OneWireException
   {
      return "<na>";
   }

   /**
    * Retrieves the address of the adapter, if it has one.
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
    * @see   com.dalsemi.onewire.utils.Address
    */
   public String getAdapterAddress ()
      throws OneWireIOException, OneWireException
   {
      return "<na>";
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
   public boolean canOverdrive ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

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
   public boolean canHyperdrive ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

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
   public boolean canFlex ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

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
   public boolean canProgram ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

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
   public boolean canDeliverPower ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

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
   public boolean canDeliverSmartPower ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

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
   public boolean canBreak ()
      throws OneWireIOException, OneWireException
   {
      return false;
   }

   //--------
   //-------- Finding iButtons and 1-Wire devices
   //--------

   /**
    * Returns an enumeration of <code>OneWireContainer</code> objects corresponding
    * to all of the iButtons or 1-Wire devices found on the 1-Wire Network.
    * If no devices are found, then an empty enumeration will be returned.
    * In most cases, all further communication with the device is done
    * through the OneWireContainer.
    *
    * @return  <code>Enumeration</code> of <code>OneWireContainer</code> objects
    * found on the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public Enumeration getAllDeviceContainers ()
      throws OneWireIOException, OneWireException
   {
      Vector           ibutton_vector = new Vector();
      OneWireContainer temp_ibutton;

      temp_ibutton = getFirstDeviceContainer();

      if (temp_ibutton != null)
      {
         ibutton_vector.addElement(temp_ibutton);

         // loop to get all of the ibuttons
         do
         {
            temp_ibutton = getNextDeviceContainer();

            if (temp_ibutton != null)
               ibutton_vector.addElement(temp_ibutton);
         }
         while (temp_ibutton != null);
      }

      return ibutton_vector.elements();
   }

   /**
    * Returns a <code>OneWireContainer</code> object corresponding to the first iButton
    * or 1-Wire device found on the 1-Wire Network. If no devices are found,
    * then a <code>null</code> reference will be returned. In most cases, all further
    * communication with the device is done through the <code>OneWireContainer</code>.
    *
    * @return  The first <code>OneWireContainer</code> object found on the
    * 1-Wire Network, or <code>null</code> if no devices found.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public OneWireContainer getFirstDeviceContainer ()
      throws OneWireIOException, OneWireException
   {
      if (findFirstDevice() == true)
      {
         return getDeviceContainer();
      }
      else
         return null;
   }

   /**
    * Returns a <code>OneWireContainer</code> object corresponding to the next iButton
    * or 1-Wire device found. The previous 1-Wire device found is used
    * as a starting point in the search.  If no devices are found,
    * then a <code>null</code> reference will be returned. In most cases, all further
    * communication with the device is done through the <code>OneWireContainer</code>.
    *
    * @return  The next <code>OneWireContainer</code> object found on the
    * 1-Wire Network, or <code>null</code> if no iButtons found.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public OneWireContainer getNextDeviceContainer ()
      throws OneWireIOException, OneWireException
   {
      if (findNextDevice() == true)
      {
         return getDeviceContainer();
      }
      else
         return null;
   }

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
   public abstract boolean findFirstDevice ()
      throws OneWireIOException, OneWireException;

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
   public abstract boolean findNextDevice ()
      throws OneWireIOException, OneWireException;

   /**
    * Copies the 'current' 1-Wire device address being used by the adapter into
    * the array.  This address is the last iButton or 1-Wire device found
    * in a search (findNextDevice()...).
    * This method copies into a user generated array to allow the
    * reuse of the buffer.  When searching many iButtons on the one
    * wire network, this will reduce the memory burn rate.
    *
    * @param  address An array to be filled with the current iButton address.
    * @see   com.dalsemi.onewire.utils.Address
    */
   public abstract void getAddress (byte[] address);

   /**
    * Gets the 'current' 1-Wire device address being used by the adapter as a long.
    * This address is the last iButton or 1-Wire device found
    * in a search (findNextDevice()...).
    *
    * @return <code>long</code> representation of the iButton address
    * @see   com.dalsemi.onewire.utils.Address
    */
   public long getAddressAsLong ()
   {
      byte[] address = new byte [8];

      getAddress(address);

      return Address.toLong(address);
   }

   /**
    * Gets the 'current' 1-Wire device address being used by the adapter as a String.
    * This address is the last iButton or 1-Wire device found
    * in a search (findNextDevice()...).
    *
    * @return <code>String</code> representation of the iButton address
    * @see   com.dalsemi.onewire.utils.Address
    */
   public String getAddressAsString ()
   {
      byte[] address = new byte [8];

      getAddress(address);

      return Address.toString(address);
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present on
    * the 1-Wire Network. This does not affect the 'current' device
    * state information used in searches (findNextDevice...).
    *
    * @param  address  device address to verify is present
    *
    * @return  <code>true</code> if device is present, else
    *         <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean isPresent (byte[] address)
      throws OneWireIOException, OneWireException
   {
      reset();
      putByte(0xF0);   // Search ROM command

      return strongAccess(address);
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present on
    * the 1-Wire Network. This does not affect the 'current' device
    * state information used in searches (findNextDevice...).
    *
    * @param  address  device address to verify is present
    *
    * @return  <code>true</code> if device is present, else
    *         <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean isPresent (long address)
      throws OneWireIOException, OneWireException
   {
      return isPresent(Address.toByteArray(address));
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present on
    * the 1-Wire Network. This does not affect the 'current' device
    * state information used in searches (findNextDevice...).
    *
    * @param  address  device address to verify is present
    *
    * @return  <code>true</code> if device is present, else
    *         <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean isPresent (String address)
      throws OneWireIOException, OneWireException
   {
      return isPresent(Address.toByteArray(address));
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present
    * on the 1-Wire Network and in an alarm state. This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * @param  address  device address to verify is present and alarming
    *
    * @return  <code>true</code> if device is present and alarming, else
    * <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean isAlarming (byte[] address)
      throws OneWireIOException, OneWireException
   {
      reset();
      putByte(0xEC);   // Conditional search commands

      return strongAccess(address);
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present
    * on the 1-Wire Network and in an alarm state. This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * @param  address  device address to verify is present and alarming
    *
    * @return  <code>true</code> if device is present and alarming, else
    * <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean isAlarming (long address)
      throws OneWireIOException, OneWireException
   {
      return isAlarming(Address.toByteArray(address));
   }

   /**
    * Verifies that the iButton or 1-Wire device specified is present
    * on the 1-Wire Network and in an alarm state. This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * @param  address  device address to verify is present and alarming
    *
    * @return  <code>true</code> if device is present and alarming, else
    * <code>false</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean isAlarming (String address)
      throws OneWireIOException, OneWireException
   {
      return isAlarming(Address.toByteArray(address));
   }

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
    * @param  address    address of iButton or 1-Wire device to select
    *
    * @return  <code>true</code> if device address was sent, <code>false</code>
    * otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean select (byte[] address)
      throws OneWireIOException, OneWireException
   {
      // send 1-Wire Reset
      int rslt = reset();

      // broadcast the MATCH ROM command and address
      byte[] send_packet = new byte [9];

      send_packet [0] = 0x55;   // MATCH ROM command

      System.arraycopy(address, 0, send_packet, 1, 8);
      dataBlock(send_packet, 0, 9);

      // success if any device present on 1-Wire Network
      return ((rslt == RESET_PRESENCE) || (rslt == RESET_ALARM));
   }

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
    * @param  address    address of iButton or 1-Wire device to select
    *
    * @return  <code>true</code> if device address was sent,<code>false</code>
    * otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean select (long address)
      throws OneWireIOException, OneWireException
   {
      return select(Address.toByteArray(address));
   }

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
    * @param  address    address of iButton or 1-Wire device to select
    *
    * @return  <code>true</code> if device address was sent,<code>false</code>
    * otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
    * @see   com.dalsemi.onewire.utils.Address
    */
   public boolean select (String address)
      throws OneWireIOException, OneWireException
   {
      return select(Address.toByteArray(address));
   }

   /**
    * Selects the specified iButton or 1-Wire device by broadcasting its
    * address.  This operation is refered to a 'MATCH ROM' operation
    * in the iButton and 1-Wire device data sheets.  This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * In addition, this method asserts that the select did find some
    * devices on the 1-Wire net.  If no devices were found, a OneWireException
    * is thrown.
    *
    * Warning, this does not verify that the device is currently present
    * on the 1-Wire Network (See isPresent).
    *
    * @param  address    address of iButton or 1-Wire device to select
    *
    * @throws OneWireIOException on a 1-Wire communication error, or if their
    *         are no devices on the 1-Wire net.
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
    * @see   com.dalsemi.onewire.utils.Address
    */
   public void assertSelect(byte[] address)
      throws OneWireIOException, OneWireException
   {
      if(!select(address))
         throw new OneWireIOException("Device " + Address.toString(address)
             + " not present.");
   }

   /**
    * Selects the specified iButton or 1-Wire device by broadcasting its
    * address.  This operation is refered to a 'MATCH ROM' operation
    * in the iButton and 1-Wire device data sheets.  This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * In addition, this method asserts that the select did find some
    * devices on the 1-Wire net.  If no devices were found, a OneWireException
    * is thrown.
    *
    * Warning, this does not verify that the device is currently present
    * on the 1-Wire Network (See isPresent).
    *
    * @param  address    address of iButton or 1-Wire device to select
    *
    * @return  <code>true</code> if device address was sent,<code>false</code>
    * otherwise.
    *
    * @throws OneWireIOException on a 1-Wire communication error, or if their
    *         are no devices on the 1-Wire net.
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
    * @see   com.dalsemi.onewire.utils.Address
    */
   public void assertSelect(long address)
      throws OneWireIOException, OneWireException
   {
      if(!select(Address.toByteArray(address)))
         throw new OneWireIOException("Device " + Address.toString(address)
             + " not present.");
   }

   /**
    * Selects the specified iButton or 1-Wire device by broadcasting its
    * address.  This operation is refered to a 'MATCH ROM' operation
    * in the iButton and 1-Wire device data sheets.  This does not
    * affect the 'current' device state information used in searches
    * (findNextDevice...).
    *
    * In addition, this method asserts that the select did find some
    * devices on the 1-Wire net.  If no devices were found, a OneWireException
    * is thrown.
    *
    * Warning, this does not verify that the device is currently present
    * on the 1-Wire Network (See isPresent).
    *
    * @param  address    address of iButton or 1-Wire device to select
    *
    * @throws OneWireIOException on a 1-Wire communication error, or if their
    *         are no devices on the 1-Wire net.
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#isPresent(byte[])
    * @see   com.dalsemi.onewire.utils.Address
    */
   public void assertSelect(String address)
      throws OneWireIOException, OneWireException
   {
      if(!select(Address.toByteArray(address)))
         throw new OneWireIOException("Device " + address
             + " not present.");
   }

   //--------
   //-------- Finding iButton/1-Wire device options
   //--------

   /**
    * Sets the 1-Wire Network search to find only iButtons and 1-Wire
    * devices that are in an 'Alarm' state that signals a need for
    * attention.  Not all iButton types
    * have this feature.  Some that do: DS1994, DS1920, DS2407.
    * This selective searching can be canceled with the
    * 'setSearchAllDevices()' method.
    *
    * @see #setNoResetSearch
    */
   public abstract void setSearchOnlyAlarmingDevices ();

   /**
    * Sets the 1-Wire Network search to not perform a 1-Wire
    * reset before a search.  This feature is chiefly used with
    * the DS2409 1-Wire coupler.
    * The normal reset before each search can be restored with the
    * 'setSearchAllDevices()' method.
    */
   public abstract void setNoResetSearch ();

   /**
    * Sets the 1-Wire Network search to find all iButtons and 1-Wire
    * devices whether they are in an 'Alarm' state or not and
    * restores the default setting of providing a 1-Wire reset
    * command before each search. (see setNoResetSearch() method).
    *
    * @see #setNoResetSearch
    */
   public abstract void setSearchAllDevices ();

   /**
    * Removes any selectivity during a search for iButtons or 1-Wire devices
    * by family type.  The unique address for each iButton and 1-Wire device
    * contains a family descriptor that indicates the capabilities of the
    * device.
    * @see    #targetFamily
    * @see    #targetFamily(byte[])
    * @see    #excludeFamily
    * @see    #excludeFamily(byte[])
    */
   public void targetAllFamilies ()
   {
      include = null;
      exclude = null;
   }

   /**
    * Takes an integer to selectively search for this desired family type.
    * If this method is used, then no devices of other families will be
    * found by any of the search methods.
    *
    * @param  family   the code of the family type to target for searches
    * @see   com.dalsemi.onewire.utils.Address
    * @see    #targetAllFamilies
    */
   public void targetFamily (int family)
   {
      if ((include == null) || (include.length != 1))
         include = new byte [1];

      include [0] = ( byte ) family;
   }

   /**
    * Takes an array of bytes to use for selectively searching for acceptable
    * family codes.  If used, only devices with family codes in this array
    * will be found by any of the search methods.
    *
    * @param  family  array of the family types to target for searches
    * @see   com.dalsemi.onewire.utils.Address
    * @see    #targetAllFamilies
    */
   public void targetFamily (byte family [])
   {
      if ((include == null) || (include.length != family.length))
         include = new byte [family.length];

      System.arraycopy(family, 0, include, 0, family.length);
   }

   /**
    * Takes an integer family code to avoid when searching for iButtons.
    * or 1-Wire devices.
    * If this method is used, then no devices of this family will be
    * found by any of the search methods.
    *
    * @param  family   the code of the family type NOT to target in searches
    * @see   com.dalsemi.onewire.utils.Address
    * @see    #targetAllFamilies
    */
   public void excludeFamily (int family)
   {
      if ((exclude == null) || (exclude.length != 1))
         exclude = new byte [1];

      exclude [0] = ( byte ) family;
   }

   /**
    * Takes an array of bytes containing family codes to avoid when finding
    * iButtons or 1-Wire devices.  If used, then no devices with family
    * codes in this array will be found by any of the search methods.
    *
    * @param  family  array of family cods NOT to target for searches
    * @see   com.dalsemi.onewire.utils.Address
    * @see    #targetAllFamilies
    */
   public void excludeFamily (byte family [])
   {
      if ((exclude == null) || (exclude.length != family.length))
         exclude = new byte [family.length];

      System.arraycopy(family, 0, exclude, 0, family.length);
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
   public abstract boolean beginExclusive (boolean blocking)
      throws OneWireException;

   /**
    * Relinquishes exclusive control of the 1-Wire Network.
    * This command dynamically marks the end of a critical section and
    * should be used when exclusive control is no longer needed.
    */
   public abstract void endExclusive ();

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
   public abstract void putBit (boolean bitValue)
      throws OneWireIOException, OneWireException;

   /**
    * Gets a bit from the 1-Wire Network.
    *
    * @return  the bit value recieved from the the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract boolean getBit ()
      throws OneWireIOException, OneWireException;

   /**
    * Sends a byte to the 1-Wire Network.
    *
    * @param  byteValue  the byte value to send to the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract void putByte (int byteValue)
      throws OneWireIOException, OneWireException;

   /**
    * Gets a byte from the 1-Wire Network.
    *
    * @return  the byte value received from the the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract int getByte ()
      throws OneWireIOException, OneWireException;

   /**
    * Gets a block of data from the 1-Wire Network.
    *
    * @param  len  length of data bytes to receive
    *
    * @return  the data received from the 1-Wire Network.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract byte[] getBlock (int len)
      throws OneWireIOException, OneWireException;

   /**
    * Gets a block of data from the 1-Wire Network and write it into
    * the provided array.
    *
    * @param  arr     array in which to write the received bytes
    * @param  len     length of data bytes to receive
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract void getBlock (byte[] arr, int len)
      throws OneWireIOException, OneWireException;

   /**
    * Gets a block of data from the 1-Wire Network and write it into
    * the provided array.
    *
    * @param  arr     array in which to write the received bytes
    * @param  off     offset into the array to start
    * @param  len     length of data bytes to receive
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract void getBlock (byte[] arr, int off, int len)
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
   public abstract void dataBlock (byte dataBlock [], int off, int len)
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
    * <li> 3 (RESET_SHORT) indicates 1-Wire appears shorted.  This can be
    *        transient conditions in a 1-Wire Network.  Not all adapter types
    *        can detect this condition.
    * </ul>
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    */
   public abstract int reset ()
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
      throw new OneWireException(
         "Power delivery not supported by this adapter type");
   }

   /**
    * Sets the 1-Wire Network voltage to supply power to a 1-Wire device.
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
   public boolean startPowerDelivery (int changeCondition)
      throws OneWireIOException, OneWireException
   {
      throw new OneWireException(
         "Power delivery not supported by this adapter type");
   }

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
    * <li>   7 (DELIVERY_EPROM) provide program pulse for 480 microseconds
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
      throw new OneWireException(
         "Program pulse delivery not supported by this adapter type");
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
   public boolean startProgramPulse (int changeCondition)
      throws OneWireIOException, OneWireException
   {
      throw new OneWireException(
         "Program pulse delivery not supported by this adapter type");
   }

   /**
    * Sets the 1-Wire Network voltage to 0 volts.  This method is used
    * rob all 1-Wire Network devices of parasite power delivery to force
    * them into a hard reset.
    *
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *         or the adapter does not support this operation
    */
   public void startBreak ()
      throws OneWireIOException, OneWireException
   {
      throw new OneWireException(
         "Break delivery not supported by this adapter type");
   }

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
   public void setPowerNormal ()
      throws OneWireIOException, OneWireException
   {
      return;
   }

   //--------
   //-------- 1-Wire Network speed methods
   //--------

   /**
    * Sets the new speed of data
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
    * @throws OneWireIOException on a 1-Wire communication error
    * @throws OneWireException on a setup error with the 1-Wire adapter
    *         or the adapter does not support this operation
    */
   public void setSpeed (int speed)
      throws OneWireIOException, OneWireException
   {
      if (speed != SPEED_REGULAR)
         throw new OneWireException(
            "Non-regular 1-Wire speed not supported by this adapter type");
   }

   /**
    * Returns the current data transfer speed on the 1-Wire Network. <p>
    *
    * @return <code>int</code> representing the current 1-Wire speed
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
   public int getSpeed ()
   {
      return SPEED_REGULAR;
   }

   //--------
   //-------- Misc
   //--------

   /**
    * Constructs a <code>OneWireContainer</code> object with a user supplied 1-Wire network address.
    *
    * @param  address  device address with which to create a new container
    *
    * @return  The <code>OneWireContainer</code> object
    * @see   com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer getDeviceContainer (byte[] address)
   {
      int              family_code   = address [0] & 0x7F;
      String           family_string =
         ((family_code) < 16)
         ? ("0" + Integer.toHexString(family_code)).toUpperCase()
         : (Integer.toHexString(family_code)).toUpperCase();
      Class            ibutton_class = null;
      OneWireContainer new_ibutton;

      // If any user registered button exist, check the hashtable.
      if (!registeredOneWireContainerClasses.isEmpty())
      {
         Integer familyInt = new Integer(family_code);

         // Try and get a user provided container class first.
         ibutton_class =
            ( Class ) registeredOneWireContainerClasses.get(familyInt);
      }

      // If we don't get one, do the normal lookup method.
      if (ibutton_class == null)
      {

         // try to load the ibutton container class
         try
         {
            ibutton_class =
               Class.forName("com.dalsemi.onewire.container.OneWireContainer"
                             + family_string);
         }
         catch (Exception e)
         {
            ibutton_class = null;
         }

         // if did not get specific container try the general one
         if (ibutton_class == null)
         {

            // try to load the ibutton container class
            try
            {
               ibutton_class = Class.forName(
                  "com.dalsemi.onewire.container.OneWireContainer");
            }
            catch (Exception e)
            {
               System.out.println("EXCEPTION: Unable to load OneWireContainer"
                                  + e);
               return null;
            }
         }
      }

      // try to load the ibutton container class
      try
      {

         // create the iButton container with a reference to this adapter
         new_ibutton = ( OneWireContainer ) ibutton_class.newInstance();

         new_ibutton.setupContainer(this, address);
      }
      catch (Exception e)
      {
         System.out.println(
            "EXCEPTION: Unable to instantiate OneWireContainer "
            + ibutton_class + ": " + e);
         e.printStackTrace();

         return null;
      }

      // return this new container
      return new_ibutton;
   }

   /**
    * Constructs a <code>OneWireContainer</code> object with a user supplied 1-Wire network address.
    *
    * @param  address  device address with which to create a new container
    *
    * @return  The <code>OneWireContainer</code> object
    * @see   com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer getDeviceContainer (long address)
   {
      return getDeviceContainer(Address.toByteArray(address));
   }

   /**
    * Constructs a <code>OneWireContainer</code> object with a user supplied 1-Wire network address.
    *
    * @param  address  device address with which to create a new container
    *
    * @return  The <code>OneWireContainer</code> object
    * @see   com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer getDeviceContainer (String address)
   {
      return getDeviceContainer(Address.toByteArray(address));
   }

   /**
    * Constructs a <code>OneWireContainer</code> object using the current 1-Wire network address.
    * The internal state of the port adapter keeps track of the last
    * address found and is able to create container objects from this
    * state.
    *
    * @return  the <code>OneWireContainer</code> object
    */
   public OneWireContainer getDeviceContainer ()
   {

      // Mask off the upper bit.
      byte[] address = new byte [8];

      getAddress(address);

      return getDeviceContainer(address);
   }

   /**
    * Checks to see if the family found is in the desired
    * include group.
    *
    * @return  <code>true</code> if in include group
    */
   protected boolean isValidFamily (byte[] address)
   {
      byte familyCode = address [0];

      if (exclude != null)
      {
         for (int i = 0; i < exclude.length; i++)
         {
            if (familyCode == exclude [i])
            {
               return false;
            }
         }
      }

      if (include != null)
      {
         for (int i = 0; i < include.length; i++)
         {
            if (familyCode == include [i])
            {
               return true;
            }
         }

         return false;
      }

      return true;
   }

   /**
    * Performs a 'strongAccess' with the provided 1-Wire address.
    * 1-Wire Network has already been reset and the 'search'
    * command sent before this is called.
    *
    * @param  address  device address to do strongAccess on
    *
    * @return  true if device participated and was present
    *         in the strongAccess search
    */
   private boolean strongAccess (byte[] address)
      throws OneWireIOException, OneWireException
   {
      byte[] send_packet = new byte [24];
      int    i;

      // set all bits at first
      for (i = 0; i < 24; i++)
         send_packet [i] = ( byte ) 0xFF;

      // now set or clear apropriate bits for search
      for (i = 0; i < 64; i++)
         arrayWriteBit(arrayReadBit(i, address), (i + 1) * 3 - 1,
                       send_packet);

      // send to 1-Wire Net
      dataBlock(send_packet, 0, 24);

      // check the results of last 8 triplets (should be no conflicts)
      int cnt = 56, goodbits = 0, tst, s;

      for (i = 168; i < 192; i += 3)
      {
         tst = (arrayReadBit(i, send_packet) << 1)
               | arrayReadBit(i + 1, send_packet);
         s   = arrayReadBit(cnt++, address);

         if (tst == 0x03)   // no device on line
         {
            goodbits = 0;   // number of good bits set to zero

            break;          // quit
         }

         if (((s == 0x01) && (tst == 0x02)) || ((s == 0x00) && (tst == 0x01)))   // correct bit
            goodbits++;   // count as a good bit
      }

      // check too see if there were enough good bits to be successful
      return (goodbits >= 8);
   }

   /**
    * Writes the bit state in a byte array.
    *
    * @param state new state of the bit 1, 0
    * @param index bit index into byte array
    * @param buf byte array to manipulate
    */
   private void arrayWriteBit (int state, int index, byte[] buf)
   {
      int nbyt = (index >>> 3);
      int nbit = index - (nbyt << 3);

      if (state == 1)
         buf [nbyt] |= (0x01 << nbit);
      else
         buf [nbyt] &= ~(0x01 << nbit);
   }

   /**
    * Reads a bit state in a byte array.
    *
    * @param index bit index into byte array
    * @param buf byte array to read from
    *
    * @return bit state 1 or 0
    */
   private int arrayReadBit (int index, byte[] buf)
   {
      int nbyt = (index >>> 3);
      int nbit = index - (nbyt << 3);

      return ((buf [nbyt] >>> nbit) & 0x01);
   }

   //--------
   //-------- java.lang.Object methods
   //--------

   /**
    * Returns a hashcode for this object
    * @return a hascode for this object
    */
   /*public int hashCode()
   {
      return this.toString().hashCode();
   }*/

   /**
    * Returns true if the given object is the same or equivalent
    * to this DSPortAdapter.
    *
    * @param o the Object to compare this DSPortAdapter to
    * @return true if the given object is the same or equivalent
    * to this DSPortAdapter.
    */
   public boolean equals(Object o)
   {
      if(o!=null && o instanceof DSPortAdapter)
      {
         if(o==this || o.toString().equals(this.toString()))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * Returns a string representation of this DSPortAdapter, in the format
    * of "<adapter name> <port name>".
    *
    * @return a string representation of this DSPortAdapter
    */
   public String toString()
   {
      try
      {
         return this.getAdapterName() + " " + this.getPortName();
      }
      catch(OneWireException owe)
      {
         return this.getAdapterName() + " Unknown Port";
      }
   }
}
