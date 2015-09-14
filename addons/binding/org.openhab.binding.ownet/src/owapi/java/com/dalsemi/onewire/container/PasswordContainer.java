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

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;

/**
 * Public interface for all devices which implement some form of
 * password protection.  The operation protected could be reading from
 * the device, writing to the device, or both.  These interface methods
 * will allow you to set the passwords on the device, enable/disable the
 * passwords on the device, and set the passwords for the API to use
 * when interacting with the device.
 * 
 * @version    1.00, 8 Aug 2003
 * @author     shughes, JPE
 */
public interface PasswordContainer
{
   // -----------------------------------------------------------------

   /**
    * Returns the length in bytes of the Read-Only password.
    * 
    * @return the length in bytes of the Read-Only password.
    */
   public int getReadOnlyPasswordLength()
      throws OneWireException;

   /**
    * Returns the length in bytes of the Read/Write password.
    * 
    * @return the length in bytes of the Read/Write password.
    */
   public int getReadWritePasswordLength()
      throws OneWireException;

   /**
    * Returns the length in bytes of the Write-Only password.
    * 
    * @return the length in bytes of the Write-Only password.
    */
   public int getWriteOnlyPasswordLength()
      throws OneWireException;

   // -----------------------------------------------------------------

   /**
    * Returns the absolute address of the memory location where
    * the Read-Only password is written.
    * 
    * @return the absolute address of the memory location where
    *         the Read-Only password is written.
    */
   public int getReadOnlyPasswordAddress()
      throws OneWireException;

   /**
    * Returns the absolute address of the memory location where
    * the Read/Write password is written.
    * 
    * @return the absolute address of the memory location where
    *         the Read/Write password is written.
    */
   public int getReadWritePasswordAddress()
      throws OneWireException;

   /**
    * Returns the absolute address of the memory location where
    * the Write-Only password is written.
    * 
    * @return the absolute address of the memory location where
    *         the Write-Only password is written.
    */
   public int getWriteOnlyPasswordAddress()
      throws OneWireException;
 
   // -----------------------------------------------------------------
     
   /**
    * Returns true if this device has a Read-Only password.
    * If false, all other functions dealing with the Read-Only
    * password will throw an exception if called.
    * 
    * @return <code>true</code> if this device has a Read-Only password.
    */
   public boolean hasReadOnlyPassword();
      
   /**
    * Returns true if this device has a Read/Write password.
    * If false, all other functions dealing with the Read/Write
    * password will throw an exception if called.
    * 
    * @return <code>true</code> if this device has a Read/Write password.
    */
   public boolean hasReadWritePassword();
      
   /**
    * Returns true if this device has a Write-Only password.
    * If false, all other functions dealing with the Write-Only
    * password will throw an exception if called.
    * 
    * @return <code>true</code> if this device has a Write-Only password.
    */
   public boolean hasWriteOnlyPassword();

   // -----------------------------------------------------------------

   /**
    * Returns true if the device's Read-Only password has been enabled.
    * 
    * @return <code>true</code> if the device's Read-Only password has been enabled.
    */
   public boolean getDeviceReadOnlyPasswordEnable()
      throws OneWireException;

   /**
    * Returns true if the device's Read/Write password has been enabled.
    * 
    * @return <code>true</code> if the device's Read/Write password has been enabled.
    */
   public boolean getDeviceReadWritePasswordEnable()
      throws OneWireException;

   /**
    * Returns true if the device's Write-Only password has been enabled.
    * 
    * @return <code>true</code> if the device's Write-Only password has been enabled.
    */
   public boolean getDeviceWriteOnlyPasswordEnable()
      throws OneWireException;

   // -----------------------------------------------------------------

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
   public boolean hasSinglePasswordEnable();

   /**
    * <p>Enables/Disables passwords for this Device.  This method allows you to 
    * individually enable the different types of passwords for a particular
    * device.  If <code>hasSinglePasswordEnable()</code> returns true,
    * you can selectively enable particular types of passwords.  Otherwise,
    * this method will throw an exception if all supported types are not
    * enabled.</p>
    * 
    * <p>For this to be successful, either write-protect passwords must be disabled,
    * or the write-protect password(s) for this container must be set and must match
    * the value of the write-protect password(s) in the device's register.</p>
    * 
    * @param enableReadOnly if <code>true</code> Read-Only passwords will be enabled.
    * @param enableReadWrite if <code>true</code> Read/Write passwords will be enabled.
    * @param enableWriteOnly if <code>true</code> Write-Only passwords will be enabled.
    */
   public void setDevicePasswordEnable(boolean enableReadOnly, 
                  boolean enableReadWrite, boolean enableWriteOnly)
      throws OneWireException, OneWireIOException;

   /**
    * <p>Enables/Disables passwords for this device.  If the part has more than one
    * type of password (Read-Only, Write-Only, or Read/Write), all passwords
    * will be enabled.  This function is equivalent to the following:
    *    <code> owc41.setDevicePasswordEnable(
    *                    owc41.hasReadOnlyPassword(), 
    *                    owc41.hasReadWritePassword(),
    *                    owc41.hasWriteOnlyPassword() ); </code></p>
    * 
    * <p>For this to be successful, either write-protect passwords must be disabled,
    * or the write-protect password(s) for this container must be set and must match
    * the value of the write-protect password(s) in the device's register.</p>
    * 
    * @param enableAll if <code>true</code>, all passwords are enabled.  Otherwise,
    *        all passwords are disabled.
    */
   public void setDevicePasswordEnableAll(boolean enableAll)
      throws OneWireException, OneWireIOException;

   // -----------------------------------------------------------------

   /**
    * <p>Writes the given password to the device's Read-Only password register.  Note
    * that this function does not enable the password, just writes the value to
    * the appropriate memory location.</p>
    * 
    * <p>For this to be successful, either write-protect passwords must be disabled,
    * or the write-protect password(s) for this container must be set and must match
    * the value of the write-protect password(s) in the device's register.</p>
    * 
    * @param password the new password to be written to the device's Read-Only
    *        password register.  Length must be 
    *        <code>(offset + getReadOnlyPasswordLength)</code>
    * @param offset the starting point for copying from the given password array
    */
   public void setDeviceReadOnlyPassword(byte[] password, int offset)
      throws OneWireException, OneWireIOException;

   /**
    * <p>Writes the given password to the device's Read/Write password register.  Note
    * that this function does not enable the password, just writes the value to
    * the appropriate memory location.</p>
    * 
    * <p>For this to be successful, either write-protect passwords must be disabled,
    * or the write-protect password(s) for this container must be set and must match
    * the value of the write-protect password(s) in the device's register.</p>
    * 
    * @param password the new password to be written to the device's Read-Write
    *        password register.  Length must be 
    *        <code>(offset + getReadWritePasswordLength)</code>
    * @param offset the starting point for copying from the given password array
    */
   public void setDeviceReadWritePassword(byte[] password, int offset)
      throws OneWireException, OneWireIOException;

   /**
    * <p>Writes the given password to the device's Write-Only password register.  Note
    * that this function does not enable the password, just writes the value to
    * the appropriate memory location.</p>
    * 
    * <p>For this to be successful, either write-protect passwords must be disabled,
    * or the write-protect password(s) for this container must be set and must match
    * the value of the write-protect password(s) in the device's register.</p>
    * 
    * @param password the new password to be written to the device's Write-Only
    *        password register.  Length must be 
    *        <code>(offset + getWriteOnlyPasswordLength)</code>
    * @param offset the starting point for copying from the given password array
    */
   public void setDeviceWriteOnlyPassword(byte[] password, int offset)
      throws OneWireException, OneWireIOException;

   // -----------------------------------------------------------------

   /**
    * Sets the Read-Only password used by the API when reading from the
    * device's memory.  This password is not written to the device's
    * Read-Only password register.  It is the password used by the
    * software for interacting with the device only.
    * 
    * @param password the new password to be used by the API when 
    *        reading from the device's memory.  Length must be 
    *        <code>(offset + getReadOnlyPasswordLength)</code>
    * @param offset the starting point for copying from the given password array
    */
   public void setContainerReadOnlyPassword(byte[] password, int offset)
      throws OneWireException;

   /**
    * Sets the Read/Write password used by the API when reading from  or
    * writing to the device's memory.  This password is not written to 
    * the device's Read/Write password register.  It is the password used 
    * by the software for interacting with the device only.
    * 
    * @param password the new password to be used by the API when 
    *        reading from or writing to the device's memory.  Length must be 
    *        <code>(offset + getReadWritePasswordLength)</code>
    * @param offset the starting point for copying from the given password array
    */
   public void setContainerReadWritePassword(byte[] password, int offset)
      throws OneWireException;

   /**
    * Sets the Write-Only password used by the API when writing to the
    * device's memory.  This password is not written to the device's
    * Write-Only password register.  It is the password used by the
    * software for interacting with the device only.
    * 
    * @param password the new password to be used by the API when 
    *        writing to the device's memory.  Length must be 
    *        <code>(offset + getWriteOnlyPasswordLength)</code>
    * @param offset the starting point for copying from the given password array
    */
   public void setContainerWriteOnlyPassword(byte[] password, int offset)
      throws OneWireException;

   // -----------------------------------------------------------------

   /**
    * Returns true if the password used by the API for reading from the
    * device's memory has been set.  The return value is not affected by 
    * whether or not the read password of the container actually matches 
    * the value in the device's password register.
    * 
    * @return <code>true</code> if the password used by the API for 
    * reading from the device's memory has been set.
    */
   public boolean isContainerReadOnlyPasswordSet()
      throws OneWireException;

   /**
    * Returns true if the password used by the API for reading from or
    * writing to the device's memory has been set.  The return value is 
    * not affected by whether or not the read/write password of the 
    * container actually matches the value in the device's password 
    * register.
    * 
    * @return <code>true</code> if the password used by the API for 
    * reading from or writing to the device's memory has been set.
    */
   public boolean isContainerReadWritePasswordSet()
      throws OneWireException;

   /**
    * Returns true if the password used by the API for writing to the
    * device's memory has been set.  The return value is not affected by 
    * whether or not the write password of the container actually matches 
    * the value in the device's password register.
    * 
    * @return <code>true</code> if the password used by the API for 
    * writing to the device's memory has been set.
    */
   public boolean isContainerWriteOnlyPasswordSet()
      throws OneWireException;

   // -----------------------------------------------------------------

   /**
    * Gets the Read-Only password used by the API when reading from the
    * device's memory.  This password is not read from the device's
    * Read-Only password register.  It is the password used by the
    * software for interacting with the device only and must have been
    * set using the <code>setContainerReadOnlyPassword</code> method.
    * 
    * @param password array for holding the password that is used by the 
    *        API when reading from the device's memory.  Length must be 
    *        <code>(offset + getWriteOnlyPasswordLength)</code>
    * @param offset the starting point for copying into the given password array
    */
   public void getContainerReadOnlyPassword(byte[] password, int offset)
      throws OneWireException;

   /**
    * Gets the Read/Write password used by the API when reading from or 
    * writing to the device's memory.  This password is not read from 
    * the device's Read/Write password register.  It is the password used 
    * by the software for interacting with the device only and must have 
    * been set using the <code>setContainerReadWritePassword</code> method.
    * 
    * @param password array for holding the password that is used by the 
    *        API when reading from or writing to the device's memory.  Length must be 
    *        <code>(offset + getReadWritePasswordLength)</code>
    * @param offset the starting point for copying into the given password array
    */
   public void getContainerReadWritePassword(byte[] password, int offset)
      throws OneWireException;

   /**
    * Gets the Write-Only password used by the API when writing to the
    * device's memory.  This password is not read from the device's
    * Write-Only password register.  It is the password used by the
    * software for interacting with the device only and must have been
    * set using the <code>setContainerWriteOnlyPassword</code> method.
    * 
    * @param password array for holding the password that is used by the 
    *        API when writing to the device's memory.  Length must be 
    *        <code>(offset + getWriteOnlyPasswordLength)</code>
    * @param offset the starting point for copying into the given password array
    */
   public void getContainerWriteOnlyPassword(byte[] password, int offset)
      throws OneWireException;

   // -----------------------------------------------------------------
}