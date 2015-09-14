/*---------------------------------------------------------------------------
 * Copyright (C) 2003 Dallas Semiconductor Corporation, All Rights Reserved.
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
import java.util.*;
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;


/**
 * <P> 1-Wire&#174 container for a 32K bytes of read-only and read/write password
 * protected memory, DS1977.  This container encapsulates the functionality
 * of the 1-Wire family type <B>37</B> (hex).
 * </P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> 32K bytes EEPROM organized as pages of 64 bytes.
 *   <LI> 512-bit scratchpad ensures integrity of data transfer
 *   <LI> On-chip 16-bit CRC generator
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
 *   <LI> <B> Scratchpad with CRC and Password support </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 64 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write not-general-purpose volatile
 *         <LI> <I> Pages</I> 1 page of length 64 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *         <li> <i> Extra information for each page</i>  Target address, offset, length 3
 *         <LI> <i> Supports Copy Scratchpad With Password command </I>
 *      </UL>
 *   <LI> <B> Main Memory </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32704 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 511 pages of length 64 bytes giving 61 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC
 *         <LI> <I> Read-Only and Read/Write password </I> if enabled, passwords are required for
 *                  reading from and writing to the device.
 *      </UL>
 *   <LI> <B> Register control </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 64 starting at physical address 32704
 *         <LI> <I> Features</I> Read/Write not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 1 pages of length 64 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *         <LI> <I> Read-Only and Read/Write password </I> if enabled, passwords are required for
 *                  reading from and writing to the device.
 *      </UL>
 * </UL>
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
 * @see com.dalsemi.onewire.container.PasswordContainer
 *
 * @version    1.00, 18 Aug 2003
 * @author     jevans
 *
 */
public class OneWireContainer37 extends OneWireContainer
   implements PasswordContainer
{
   // enables/disables debugging
   private static final boolean DEBUG = false;

   // when reading a page, the memory bank may throw a crc exception if the device
   // is sampling or starts sampling during the read.  This value sets how many
   // times the device retries before passing the exception on to the application.
   private static final int MAX_READ_RETRY_CNT = 15;

   // the length of the Read-Only and Read/Write password registers
   private static final int PASSWORD_LENGTH = 8;

   // memory bank for scratchpad
   private MemoryBankScratchCRCPW scratch = null;
   // memory bank for general-purpose user data
   private MemoryBankNVCRCPW userDataMemory = null;
   // memory bank for control register
   private MemoryBankNVCRCPW register = null;

   // Maxim/Dallas Semiconductor Part number
   private String partNumber = "DS1977";

   // Letter appended at end of partNumber (S/H/L/T)
    private char partLetter = '0';


   // should we check the speed
   private boolean doSpeedEnable = true;

   /**
    * The current password for readingfrom this device.
    */
   protected final byte[] readPassword = new byte[8];
   protected boolean readPasswordSet = false;

   /**
    * The current password for reading/writing from/to this device.
    */
   protected final byte[] readWritePassword = new byte[8];
   protected boolean readWritePasswordSet = false;

   // used to tell if the passwords have been enabled
   private boolean readOnlyPasswordEnabled = false;
   private boolean readWritePasswordEnabled = false;

   // used to 'enable' passwords
   private static final byte ENABLE_BYTE = (byte)0xAA;
   // used to 'disable' passwords
   private static final byte DISABLE_BYTE = 0x00;

   private String descriptionString =
       "Rugged, self-sufficient 1-Wire device that, once setup can "
       + "store 32KB of password protected memory with a read only "
       + "and a read/write password.";

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// 1-Wire Commands
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

   /** 1-Wire command for Write Scratchpad */
   public static final byte WRITE_SCRATCHPAD_COMMAND   = (byte)0x0F;
   /** 1-Wire command for Read Scratchpad */
   public static final byte READ_SCRATCHPAD_COMMAND    = (byte)0xAA;
   /** 1-Wire command for Copy Scratchpad With Password */
   public static final byte COPY_SCRATCHPAD_PW_COMMAND = (byte)0x99;
   /** 1-Wire command for Read Memory With Password */
   public static final byte READ_MEMORY_PW_COMMAND     = (byte)0x69;
   /** 1-Wire command for Verifing the Password */
   public static final byte VERIFY_PSW_COMMAND         = (byte)0xC3;
   /** 1-Wire command for getting Read Version */
   public static final byte READ_VERSION               = (byte)0xCC;

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Register addresses and control bits
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

   // 1 byte, alternating ones and zeroes indicates passwords are enabled
   /** Address of the Password Control Register. */
   public static final int PASSWORD_CONTROL_REGISTER = 0x7FD0;

   // 8 bytes, write only, for setting the Read Access Password
   /** Address of Read Access Password. */
   public static final int READ_ACCESS_PASSWORD = 0x7FC0;

   // 8 bytes, write only, for setting the Read Access Password
   /** Address of the Read Write Access Password. */
   public static final int READ_WRITE_ACCESS_PASSWORD = 0x7FC8;

   public static final int READ_WRITE_PWD = 0;
   public static final int READ_ONLY_PWD  = 1;

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Constructors and Initializers
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a
    * DS1977.
    * Note that the method <code>setupContainer(DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer37(DSPortAdapter,byte[])
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer37(DSPortAdapter,long)
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer37(DSPortAdapter,String)
    */
   public OneWireContainer37()
   {
      super();

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a
    * DS1977.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1977
    *
    * @see #OneWireContainer37()
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer37(DSPortAdapter,long)
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer37(DSPortAdapter,String)
    */
   public OneWireContainer37(DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a
    * DS1977.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1977
    *
    * @see #OneWireContainer37()
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer37(DSPortAdapter,byte[])
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer37(DSPortAdapter,String)
    */
   public OneWireContainer37(DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a
    * DS1977.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1977
    *
    * @see #OneWireContainer37()
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer37(DSPortAdapter,long)
    * @see #OneWireContainer37(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer37(DSPortAdapter,String)
    */
   public OneWireContainer37(DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
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
   public void setupContainer(DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super.setupContainer(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
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
   public void setupContainer(DSPortAdapter sourceAdapter, long newAddress)
   {
      super.setupContainer(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
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
   public void setupContainer(DSPortAdapter sourceAdapter, String newAddress)
   {
      super.setupContainer(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Container Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

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
      Vector v = new Vector(3);

      v.addElement(scratch);
      v.addElement(userDataMemory);
      v.addElement(register);

      return v.elements();
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
    * Gets the Dallas Semiconductor part number of the iButton
    * or 1-Wire Device as a <code>java.lang.String</code>.
    * For example "DS1977".
    *
    * @return iButton or 1-Wire device name
    */
   public String getName ()
   {
      return partNumber;
   }

   /**
    * Retrieves the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of MicroLAN devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * "Crypto iButton".
    *
    * @return  the alternate names for this iButton or 1-Wire device
    */
   public String getAlternateNames ()
   {
      return "";
   }

   /**
    * Gets a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return descriptionString;
   }

   /**
    * Directs the container to avoid the calls to doSpeed() in methods that communicate
    * with the Thermocron. To ensure that all parts can talk to the 1-Wire bus
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

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Read/Write Password Functions
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

   /**
    * Retrieves the password length for the read-only password.
    *
    * @return  the read-only password length
    *
    * @throws OneWireException
    */
   public int getReadOnlyPasswordLength()
      throws OneWireException
   {
      return PASSWORD_LENGTH;
   }

   /**
    * Retrieves the password length for the read/write password.
    *
    * @return  the read/write password length
    *
    * @throws OneWireException
    */
   public int getReadWritePasswordLength()
      throws OneWireException
   {
      return PASSWORD_LENGTH;
   }

   /**
    * Retrieves the password length for the write-only password.
    *
    * @return  the write-only password length
    *
    * @throws OneWireException
    */
   public int getWriteOnlyPasswordLength()
      throws OneWireException
   {
      throw new OneWireException("The DS1977 does not have a write password.");
   }

   /**
    * Retrieves the address the read only password starts
    *
    * @return the address of the read only password
    */
   public int getReadOnlyPasswordAddress()
      throws OneWireException
   {
      return READ_ACCESS_PASSWORD;
   }

   /**
    * Retrieves the address the read/write password starts
    *
    * @return the address of the read/write password
    */
   public int getReadWritePasswordAddress()
      throws OneWireException
   {
      return READ_WRITE_ACCESS_PASSWORD;
   }

   /**
    * Retrieves the address the write only password starts
    *
    * @return the address of the write only password
    */
   public int getWriteOnlyPasswordAddress()
      throws OneWireException
   {
      throw new OneWireException("The DS1977 does not have a write password.");
   }

   /**
    * Tells whether the device has a read only password.
    *
    * @return  if the device has a read only password
    */
   public boolean hasReadOnlyPassword()
   {
      return true;
   }

   /**
    * Tells whether the device has a read/write password.
    *
    * @return  if the device has a read/write password
    */
   public boolean hasReadWritePassword()
   {
      return true;
   }

   /**
    * Tells whether the device has a write only password.
    *
    * @return  if the device has a write only password
    */
   public boolean hasWriteOnlyPassword()
   {
      return false;
   }

   /**
    * Tells whether the read only password has been enabled.
    *
    * @return  the enabled status of the read only password
    *
    * @throws OneWireException
    */
   public boolean getDeviceReadOnlyPasswordEnable()
      throws OneWireException
   {
      return readOnlyPasswordEnabled;
   }

   /**
    * Tells whether the read/write password has been enabled.
    *
    * @return  the enabled status of the read/write password
    *
    * @throws OneWireException
    */
   public boolean getDeviceReadWritePasswordEnable()
      throws OneWireException
   {
      return readWritePasswordEnabled;
   }

   /**
    * Tells whether the write only password has been enabled.
    *
    * @return  the enabled status of the write only password
    *
    * @throws OneWireException
    */
   public boolean getDeviceWriteOnlyPasswordEnable()
      throws OneWireException
   {
      throw new OneWireException("The DS1977 does not have a Write Only Password.");
   }

   /**
    * Returns true if this device has the capability to enable one type of password
    * while leaving another type disabled.  i.e. if the device has Read-Only password
    * protection and Write-Only password protection, this method indicates whether or
    * not you can enable Read-Only protection while leaving the Write-Only protection
    * disabled.
    *
    * @return <code>true</code> if the device has the capability to enable one type
    *         of password while leaving another type disabled.
    */
   public boolean hasSinglePasswordEnable()
   {
      return false;
   }

   /**
    * <p>Enables/Disables passwords for this device.  If the part has more than one
    * type of password (Read-Only, Write-Only, or Read/Write), all passwords
    * will be enabled.  This function is equivalent to the following:
    *    <code> owc37.setDevicePasswordEnable(
    *                    owc37.hasReadOnlyPassword(),
    *                    owc37.hasReadWritePassword(),
    *                    owc37.hasWriteOnlyPassword() ); </code></p>
    *
    * <p>For this to be successful, either write-protect passwords must be disabled,
    * or the write-protect password(s) for this container must be set and must match
    * the value of the write-protect password(s) in the device's register.</P>
    *
    * <P><B>
    * WARNING: Enabling passwords requires that both the read password and the
    * read/write password be re-written to the part.  Before calling this method,
    * you should set the container read password and read/write password values.
    * This will ensure that the correct value is written into the part.
    * </B></P>
    *
    * @param enableAll if <code>true</code>, all passwords are enabled.  Otherwise,
    *        all passwords are disabled.
    */
   public void setDevicePasswordEnableAll(boolean enableAll)
      throws OneWireException, OneWireIOException
   {
      setDevicePasswordEnable(enableAll, enableAll, false);
   }

   /**
    * Attempts to change the value of the read password in the device's
    * register. For this to be successful, either passwords must be disabled,
    * or the read/write password for this container must be set and must match
    * the value of the read/write password in the device's register.
    *
    * <P><B>
    * WARNING: Setting the read password requires that both the read password
    * and the read/write password be written to the part.  Before calling this
    * method, you should set the container read/write password value.
    * This will ensure that the correct value is written into the part.
    * </B></P>
    *
    * @param password the new value of 8-byte device read password, to be copied
    *        into the devices register.
    * @param offset the offset to start copying the 8 bytes from the array
    */
   public void setDeviceReadOnlyPassword(byte[] password, int offset)
      throws OneWireException, OneWireIOException
   {
      register.write(READ_ACCESS_PASSWORD&0x3F, password, offset, 8);

      if(verifyPassword(password, offset, READ_ONLY_PWD))
         setContainerReadOnlyPassword(password, offset);
   }

   /**
    * Attempts to change the value of the read/write password in the device's
    * register.  For this to be successful, either passwords must be disabled,
    * or the read/write password for this container must be set and must match
    * the current value of the read/write password in the device's register.
    *
    * @param password the new value of 8-byte device read/write password, to be
    *        copied into the devices register.
    * @param offset the offset to start copying the 8 bytes from the array
    */
   public void setDeviceReadWritePassword(byte[] password, int offset)
      throws OneWireException, OneWireIOException
   {
      register.write(READ_WRITE_ACCESS_PASSWORD&0x3F, password, offset, 8);

      if(verifyPassword(password, offset, READ_WRITE_PWD))
         setContainerReadWritePassword(password, offset);
   }

   /**
    * Attempts to change the value of the write only password in the device's
    * register.  For this to be successful, either passwords must be disabled,
    * or the read/write password for this container must be set and must match
    * the current value of the read/write password in the device's register.
    *
    * @param password the new value of 8-byte device read/write password, to be
    *        copied into the devices register.
    * @param offset the offset to start copying the 8 bytes from the array
    */
   public void setDeviceWriteOnlyPassword(byte[] password, int offset)
      throws OneWireException, OneWireIOException
   {
      throw new OneWireException("The DS1977 does not have a write only password.");
   }

   /**
    * <P>Enables/disables passwords by writing to the devices password control
    * register.  For this to be successful, either passwords must be disabled,
    * or the read/write password for this container must be set and must match
    * the current value of the read/write password in the device's register.</P>
    *
    * <P><B>
    * WARNING: Enabling passwords requires that both the read password and the
    * read/write password be re-written to the part.  Before calling this method,
    * you should set the container read password and read/write password values.
    * This will ensure that the correct value is written into the part.
    * </B></P>
    *
    * @param enable if <code>true</code>, device passwords will be enabled.
    *        All subsequent read and write operations will require that the
    *        passwords for the container are set.
    */
   public void setDevicePasswordEnable(boolean enableReadOnly,
                  boolean enableReadWrite, boolean enableWriteOnly)
      throws OneWireException, OneWireIOException
   {
      if(enableWriteOnly)
         throw new OneWireException(
            "The DS1922 does not have a write only password.");

      if(!isContainerReadOnlyPasswordSet() && enableReadOnly)
         throw new OneWireException("Container Read Password is not set");
      if(!isContainerReadWritePasswordSet())
         throw new OneWireException("Container Read/Write Password is not set");
      if(enableReadOnly != enableReadWrite)
         throw new OneWireException("Both read only and read/write passwords " +
                                    "will both be disable or enabled");

      // must write both passwords for this to work
      byte[] enableCommand = new byte[1];
      enableCommand[0] = (enableReadWrite?ENABLE_BYTE:DISABLE_BYTE);

      register.write(PASSWORD_CONTROL_REGISTER&0x3F, enableCommand, 0, 1);

      if(enableReadOnly)
      {
         readOnlyPasswordEnabled = true;
         readWritePasswordEnabled = true;
      }
      else
      {
         readOnlyPasswordEnabled = false;
         readWritePasswordEnabled = false;
      }
   }

   /**
    * Sets the value of the read password for the container.  This is the value
    * used by this container to read the memory of the device.  If this password
    * does not match the value of the read password in the device's password
    * register, all subsequent read operations will fail.
    *
    * @param password New 8-byte value of container's read password.
    * @param offset Index to start copying the password from the array.
    */
   public void setContainerReadOnlyPassword(byte[] password, int offset)
      throws OneWireException
   {
      System.arraycopy(password, offset, readPassword, 0, PASSWORD_LENGTH);
      readPasswordSet = true;
   }

   /**
    * Returns the read password used by this container to read the memory
    * of the device.
    *
    * @param password Holds the 8-byte value of container's read password.
    * @param offset Index to start copying the password into the array.
    */
   public void getContainerReadOnlyPassword(byte[] password, int offset)
      throws OneWireException
   {
      System.arraycopy(readPassword, 0, password, offset, PASSWORD_LENGTH);
   }

   /**
    * Returns true if the container's read password has been set.  The return
    * value is not affected by whether or not the read password of the container
    * actually matches the value in the device's password register.
    *
    * @return <code>true</code> if the container's read password has been set
    */
   public boolean isContainerReadOnlyPasswordSet()
      throws OneWireException
   {
      return readPasswordSet;
   }

   /**
    * Sets the value of the read/write password for the container.  This is the
    * value used by this container to read and write to the memory of the
    * device.  If this password does not match the value of the read/write
    * password in the device's password register, all subsequent read and write
    * operations will fail.
    *
    * @param password New 8-byte value of container's read/write password.
    * @param offset Index to start copying the password from the array.
    */
   public void setContainerReadWritePassword(byte[] password, int offset)
      throws OneWireException
   {
      System.arraycopy(password, offset, readWritePassword, 0, 8);
      readWritePasswordSet = true;
   }

   /**
    * Returns the read/write password used by this container to read from and
    * write to the memory of the device.
    *
    * @param password Holds the 8-byte value of container's read/write password.
    * @param offset Index to start copying the password into the array.
    */
   public void getContainerReadWritePassword(byte[] password, int offset)
      throws OneWireException
   {
      System.arraycopy(readWritePassword, 0, password, offset, PASSWORD_LENGTH);
   }

   /**
    * Returns true if the container's read/write password has been set.  The
    * return value is not affected by whether or not the read/write password of
    * the container actually matches the value in the device's password
    * register.
    *
    * @return <code>true</code> if the container's read/write password has been
    *         set.
    */
   public boolean isContainerReadWritePasswordSet()
      throws OneWireException
   {
      return readWritePasswordSet;
   }

   /**
    * Sets the value of the read/write password for the container.  This is the
    * value used by this container to read and write to the memory of the
    * device.  If this password does not match the value of the read/write
    * password in the device's password register, all subsequent read and write
    * operations will fail.
    *
    * @param password New 8-byte value of container's read/write password.
    * @param offset Index to start copying the password from the array.
    */
   public void setContainerWriteOnlyPassword(byte[] password, int offset)
      throws OneWireException
   {
      throw new OneWireException("The DS1977 does not have a write only password.");
   }

   /**
    * Returns the read/write password used by this container to read from and
    * write to the memory of the device.
    *
    * @param password Holds the 8-byte value of container's read/write password.
    * @param offset Index to start copying the password into the array.
    */
   public void getContainerWriteOnlyPassword(byte[] password, int offset)
      throws OneWireException
   {
      throw new OneWireException("The DS1977 does not have a write only password.");
   }

   /**
    * Returns true if the container's read/write password has been set.  The
    * return value is not affected by whether or not the read/write password of
    * the container actually matches the value in the device's password
    * register.
    *
    * @return <code>true</code> if the container's read/write password has been
    *         set.
    */
   public boolean isContainerWriteOnlyPasswordSet()
      throws OneWireException
   {
      throw new OneWireException("The DS1977 does not have a write only password.");
   }

   public boolean verifyPassword(byte[] password, int offset, int type)
      throws OneWireException, OneWireIOException
   {
      byte[]  raw_buf = new byte [15];
      int addr = ((type == READ_ONLY_PWD) ? READ_ACCESS_PASSWORD : READ_WRITE_ACCESS_PASSWORD);

      // command, address, offset, password (except last byte)
      raw_buf [0] = VERIFY_PSW_COMMAND;
      raw_buf [1] = ( byte ) (addr & 0xFF);
      raw_buf [2] = ( byte ) (((addr & 0xFFFF) >>> 8) & 0xFF);

      System.arraycopy(password, offset, raw_buf, 3, 8);

      // send block (check copy indication complete)
      register.ib.adapter.dataBlock(raw_buf, 0, 10);

      if(register.ib.adapter.startPowerDelivery(
         DSPortAdapter.CONDITION_AFTER_BYTE))
      {

         // send last byte of password and enable strong pullup
         register.ib.adapter.putByte(raw_buf[11]);

         // delay for read to complete
         msWait(5);

         // turn off strong pullup
         register.ib.adapter.setPowerNormal();

         // read the confirmation byte
         if (register.ib.adapter.getByte() != 0xAA)
         {
            return false;
         }

         return true;
      }

      return false;
   }

// *****************************************************************************
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Private initilizers
//  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// *****************************************************************************

   /**
    * Construct the memory banks used for I/O.
    */
   private void initMem ()
   {
      // scratchpad
      scratch = new MemoryBankScratchCRCPW(this);
      scratch.pageLength          = 64;
      scratch.size                = 64;
      scratch.numberPages         = 1;
      scratch.maxPacketDataLength = 61;
      scratch.enablePower         = true;

      // User Data Memory
      userDataMemory                      = new MemoryBankNVCRCPW(this, scratch);
      userDataMemory.numberPages          = 511;
      userDataMemory.size                 = 32704;
      userDataMemory.pageLength           = 64;
      userDataMemory.maxPacketDataLength  = 61;
      userDataMemory.bankDescription      = "Data Memory";
      userDataMemory.startPhysicalAddress = 0x0000;
      userDataMemory.generalPurposeMemory = true;
      userDataMemory.readOnly             = false;
      userDataMemory.readWrite            = true;
      userDataMemory.enablePower          = true;

      // Register
      register                      = new MemoryBankNVCRCPW(this, scratch);
      register.numberPages          = 1;
      register.size                 = 64;
      register.pageLength           = 64;
      register.maxPacketDataLength  = 61;
      register.bankDescription      = "Register control";
      register.startPhysicalAddress = 0x7FC0;
      register.generalPurposeMemory = false;
      register.enablePower          = true;
   }

   /**
    * helper method to pause for specified milliseconds
    */
   private static final void msWait(final long ms)
   {
      try
      {
         Thread.sleep(ms);
      }
      catch(InterruptedException ie)
      {;}
   }

}