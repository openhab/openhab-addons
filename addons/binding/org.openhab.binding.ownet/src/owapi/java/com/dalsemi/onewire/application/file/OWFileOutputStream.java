
/*---------------------------------------------------------------------------
 * Copyright (C) 2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.application.file;

import java.io.IOException;
import java.io.OutputStream;
import com.dalsemi.onewire.container.OneWireContainer;


/**
 * A 1-Wire file output stream is an output stream for writing data to a
 * <code>OWFile</code> or to a <code>OWFileDescriptor</code>. Whether or not
 * a file is available or may be created depends upon the underlying
 * platform.  This platform allows a file to be opened
 * for writing by only one <tt>OWFileOutputStream</tt> (or other
 * file-writing object) at a time.  In such situations the constructors in
 * this class will fail if the file involved is already open.  The 1-Wire
 * File system must be formatted before use.  Use OWFile:format to prepare
 * a device or group of devices.
 *
 * <p> The 1-Wire device will only be written in the following situations
 * <ul>
 * <li> use <code>getFD()</code> and call the <code>sync()</code> method of the
 *      <code>OWFileDescriptor</code> until a <code>SyncFailedException</code> is
 *      NOT thrown
 * <li> if the device runs out of memory during a write, before
 *      <code>IOException</code> is thrown
 * <li> by calling <code>close()</code>
 * <li> in <code>finalize()</code> <B>WARNING</B> could deadlock if device not
 *      synced and inside beginExclusive/endExclusive block.
 * </ul>
 *
 * <p> Note that the 1-Wire File system can reside across multiple 1-Wire
 *  devices.  In this case only one of the devices need be supplied to the
 *  constructor.  Each device in a multi-device file system contains
 *  information to reacquire the entire list.
 *
 * <p>  File and directory <b> name </b> limitations
 * <ul>
 * <li> File/directory names limited to 4 characters not including extension
 * <li> File/directory names are not case sensitive and will be automatically
 *      changed to all-CAPS
 * <li> Only files can have extensions
 * <li> Extensions are numberical in the range 0 to 125
 * <li> Extensions 100 to 125 are special purpose and not yet implemented or allowed
 * <li> Files can have the read-only attribute
 * <li> Directories can have the hidden attribute
 * <li> It is recommended to limit directory depth to 10 levels to accomodate
 *      legacy implementations
 * </ul>
 *
 * <H3> Usage </H3>
 * <DL>
 * <DD> <H4> Example </H4>
 * Write to a 1-Wire file on device 'owd':
 * <PRE> <CODE>
 *   // create a 1-Wire file at root
 *   OWFileOutputStream outstream = new OWFileOutputStream(owd, "DEMO.0");
 *
 *   // write the data (in a byte array data[])
 *   outstream.write(data);
 *
 *   // get 1-Wire File descriptor to flush to device
 *   OWFileDescriptor owfd = owfile.getFD();
 *
 *   // loop until sync is successful
 *   do
 *   {
 *      try
 *      {
 *         owfd.sync();
 *         done = true;
 *      }
 *      catch (SyncFailedException e)
 *      {
 *         // do something
 *         ...
 *         done = false;
 *      }
 *   }
 *   while (!done)
 *
 *   // close the stream to release system resources
 *   outstream.close();
 * </CODE> </PRE>
 *
 * @author  DS
 * @version 0.01, 1 June 2001
 * @see     com.dalsemi.onewire.application.file.OWFile
 * @see     com.dalsemi.onewire.application.file.OWFileDescriptor
 * @see     com.dalsemi.onewire.application.file.OWFileInputStream
 */
public class OWFileOutputStream
   extends OutputStream
{

   //--------
   //-------- Variables
   //--------

   /**
    * File descriptor.
    */
   private OWFileDescriptor fd;

   //--------
   //-------- Constructors
   //--------

   /**
    * Creates an output file stream to write to the file with the
    * specified name. A new <code>OWFileDescriptor</code> object is
    * created to represent this file connection.
    * <p>
    * First, if there is a security manager, its <code>checkWrite</code>
    * method is called with <code>name</code> as its argument.
    * <p>
    * If the file exists but is a directory rather than a regular file, does
    * not exist but cannot be created, or cannot be opened for any other
    * reason then a <code>FileNotFoundException</code> is thrown.
    *
    * @param      owd    OneWireContainer that this Filesystem resides on
    * @param      name   the system-dependent filename
    * @exception  FileNotFoundException  if the file exists but is a directory
    *                   rather than a regular file, does not exist but cannot
    *                   be created, or cannot be opened for any other reason
    * @exception  SecurityException  if a security manager exists and its
    *               <code>checkWrite</code> method denies write access
    *               to the file.
    */
   public OWFileOutputStream(OneWireContainer owd, String name)
      throws OWFileNotFoundException
   {
      OneWireContainer[] devices = new OneWireContainer[1];
      devices[0] = owd;
      fd = new OWFileDescriptor(devices, name);

      try
      {
         fd.create(false, false, false, -1 , -1);
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }
   }

   /**
    * Creates an output file stream to write to the file with the
    * specified name. A new <code>OWFileDescriptor</code> object is
    * created to represent this file connection.
    * <p>
    * First, if there is a security manager, its <code>checkWrite</code>
    * method is called with <code>name</code> as its argument.
    * <p>
    * If the file exists but is a directory rather than a regular file, does
    * not exist but cannot be created, or cannot be opened for any other
    * reason then a <code>FileNotFoundException</code> is thrown.
    *
    * @param      owd    array of OneWireContainers that this Filesystem resides on
    * @param      name   the system-dependent filename
    * @exception  FileNotFoundException  if the file exists but is a directory
    *                   rather than a regular file, does not exist but cannot
    *                   be created, or cannot be opened for any other reason
    * @exception  SecurityException  if a security manager exists and its
    *               <code>checkWrite</code> method denies write access
    *               to the file.
    */
   public OWFileOutputStream(OneWireContainer[] owd, String name)
      throws OWFileNotFoundException
   {
      fd = new OWFileDescriptor(owd, name);

      try
      {
         fd.create(false, false, false, -1 , -1);
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }
   }

   /**
    * Creates an output file stream to write to the file with the specified
    * <code>name</code>.  If the second argument is <code>true</code>, then
    * bytes will be written to the end of the file rather than the beginning.
    * A new <code>OWFileDescriptor</code> object is created to represent this
    * file connection.
    * <p>
    * First, if there is a security manager, its <code>checkWrite</code>
    * method is called with <code>name</code> as its argument.
    * <p>
    * If the file exists but is a directory rather than a regular file, does
    * not exist but cannot be created, or cannot be opened for any other
    * reason then a <code>FileNotFoundException</code> is thrown.
    *
    * @param     owd    OneWireContainer that this Filesystem resides on
    * @param     name   the system-dependent file name
    * @param     append if <code>true</code>, then bytes will be written
    *                   to the end of the file rather than the beginning
    * @exception  FileNotFoundException  if the file exists but is a directory
    *                   rather than a regular file, does not exist but cannot
    *                   be created, or cannot be opened for any other reason.
    * @exception  SecurityException  if a security manager exists and its
    *               <code>checkWrite</code> method denies write access
    *               to the file.
    */
   public OWFileOutputStream(OneWireContainer owd, String name,
                             boolean append)
      throws OWFileNotFoundException
   {
      fd = new OWFileDescriptor(owd, name);

      try
      {
         fd.create(append, false, false, -1, -1);
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }
   }

   /**
    * Creates an output file stream to write to the file with the specified
    * <code>name</code>.  If the second argument is <code>true</code>, then
    * bytes will be written to the end of the file rather than the beginning.
    * A new <code>OWFileDescriptor</code> object is created to represent this
    * file connection.
    * <p>
    * First, if there is a security manager, its <code>checkWrite</code>
    * method is called with <code>name</code> as its argument.
    * <p>
    * If the file exists but is a directory rather than a regular file, does
    * not exist but cannot be created, or cannot be opened for any other
    * reason then a <code>FileNotFoundException</code> is thrown.
    *
    * @param      owd    array of OneWireContainers that this Filesystem resides on
    * @param     name    the system-dependent file name
    * @param     append  if <code>true</code>, then bytes will be written
    *                   to the end of the file rather than the beginning
    * @exception  FileNotFoundException  if the file exists but is a directory
    *                   rather than a regular file, does not exist but cannot
    *                   be created, or cannot be opened for any other reason.
    * @exception  SecurityException  if a security manager exists and its
    *               <code>checkWrite</code> method denies write access
    *               to the file.
    */
   public OWFileOutputStream(OneWireContainer[] owd, String name,
                             boolean append)
      throws OWFileNotFoundException
   {
      fd = new OWFileDescriptor(owd, name);

      try
      {
         fd.create(append, false, false, -1, -1);
      }
      catch (OWFileNotFoundException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }
   }

   /**
    * Creates a file output stream to write to the file represented by
    * the specified <code>File</code> object. A new
    * <code>OWFileDescriptor</code> object is created to represent this
    * file connection.
    * <p>
    * First, if there is a security manager, its <code>checkWrite</code>
    * method is called with the path represented by the <code>file</code>
    * argument as its argument.
    * <p>
    * If the file exists but is a directory rather than a regular file, does
    * not exist but cannot be created, or cannot be opened for any other
    * reason then a <code>FileNotFoundException</code> is thrown.
    *
    * @param      file               the file to be opened for writing.
    * @exception  FileNotFoundException  if the file exists but is a directory
    *                   rather than a regular file, does not exist but cannot
    *                   be created, or cannot be opened for any other reason
    * @exception  SecurityException  if a security manager exists and its
    *               <code>checkWrite</code> method denies write access
    *               to the file.
    * @see        java.io.File#getPath()
    */
   public OWFileOutputStream(OWFile file)
      throws OWFileNotFoundException
   {
      try
      {
         fd = file.getFD();
      }
      catch (IOException e)
      {
         fd.free();
         fd = null;
         throw new OWFileNotFoundException(e.toString());
      }

      fd.open();
   }

   /**
    * Creates an output file stream to write to the specified file
    * descriptor, which represents an existing connection to an actual
    * file in the Filesystem.
    * <p>
    * First, if there is a security manager, its <code>checkWrite</code>
    * method is called with the file descriptor <code>fdObj</code>
    * argument as its argument.
    *
    * @param      fdObj   the file descriptor to be opened for writing.
    * @exception  SecurityException  if a security manager exists and its
    *               <code>checkWrite</code> method denies
    *               write access to the file descriptor.
    */
   public OWFileOutputStream(OWFileDescriptor fdObj)
   {
      if (fdObj == null)
         throw new NullPointerException(
            "1-Wire FileDescriptor provided is null");

      fd = fdObj;
   }

   //--------
   //-------- Write Methods
   //--------

   /**
    * Writes the specified byte to this file output stream. Implements
    * the <code>write</code> method of <code>OutputStream</code>.
    *
    * @param      b   the byte to be written.
    * @exception  IOException  if an I/O error occurs.
    */
   public void write(int b)
      throws IOException
   {
      if (fd != null)
         fd.write(b);
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Writes <code>b.length</code> bytes from the specified byte array
    * to this file output stream.
    *
    * @param      b   the data.
    * @exception  IOException  if an I/O error occurs.
    */
   public void write(byte b [])
      throws IOException
   {
      if (fd != null)
         fd.write(b, 0, b.length);
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Writes <code>len</code> bytes from the specified byte array
    * starting at offset <code>off</code> to this file output stream.
    *
    * @param      b     the data.
    * @param      off   the start offset in the data.
    * @param      len   the number of bytes to write.
    * @exception  IOException  if an I/O error occurs.
    */
   public void write(byte b [], int off, int len)
      throws IOException
   {
      if (fd != null)
         fd.write(b, off, len);
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Closes this file output stream and releases any system resources
    * associated with this stream. This file output stream may no longer
    * be used for writing bytes.
    *
    * @exception  IOException  if an I/O error occurs.
    */
   public void close()
      throws IOException
   {
      if (fd != null)
         fd.close();
      else
         throw new IOException("1-Wire FileDescriptor is null");

      fd = null;
   }

   /**
    * Returns the file descriptor associated with this stream.
    *
    * @return  the <code>OWFileDescriptor</code> object that represents
    *          the connection to the file in the Filesystem being used
    *          by this <code>FileOutputStream</code> object.
    *
    * @exception  IOException  if an I/O error occurs.
    * @see        com.dalsemi.onewire.application.file.OWFileDescriptor
    */
   public OWFileDescriptor getFD()
      throws IOException
   {
      if (fd != null)
         return fd;
      else
         throw new IOException("1-Wire FileDescriptor is null");
   }

   /**
    * Cleans up the connection to the file, and ensures that the
    * <code>close</code> method of this file output stream is
    * called when there are no more references to this stream.
    *
    * @exception  IOException  if an I/O error occurs.
    * @see        com.dalsemi.onewire.application.file.OWFileInputStream#close()
    */
   public void finalize()
      throws IOException
   {
      if (fd != null)
         fd.close();
   }
}
