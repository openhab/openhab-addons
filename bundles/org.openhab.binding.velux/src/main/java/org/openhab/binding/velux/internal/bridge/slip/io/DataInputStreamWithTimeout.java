/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.velux.internal.bridge.slip.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is an extension of {@link java.io.DataInputStream}, which adds timeouts to receive operation.
 * <P>
 * A data input stream lets an application read primitive Java data
 * types from an underlying input stream in a machine-independent
 * way. An application uses a data output stream to write data that
 * can later be read by a data input stream.
 * <p>
 * For an in-depth discussion, see:
 * https://stackoverflow.com/questions/804951/is-it-possible-to-read-from-a-inputstream-with-a-timeout
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class DataInputStreamWithTimeout extends DataInputStream {

    /*
     * ***************************
     * ***** Private Objects *****
     */

    /**
     * Executor for asynchronous read command
     */
    ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * Creates a DataInputStreamWithTimeout that uses the specified
     * underlying DataInputStream.
     *
     * @param in the specified input stream
     */
    public DataInputStreamWithTimeout(InputStream in) {
        super(in);
    }

    /**
     * Reads up to <code>len</code> bytes of data from the contained
     * input stream into an array of bytes. An attempt is made to read
     * as many as <code>len</code> bytes, but a smaller number may be read,
     * possibly zero. The number of bytes actually read is returned as an
     * integer.
     *
     * <p>
     * This method blocks until input data is available, end of file is
     * detected, or an exception is thrown <B>until</B> the given timeout.
     *
     * <p>
     * If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p>
     * The first byte read is stored into element <code>b[off]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p>
     * In every case, elements <code>b[0]</code> through
     * <code>b[off]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read.
     * @param timeoutMSecs the maximum duration of this read before throwing a TimeoutException.
     * @return the total number of bytes read into the buffer, or
     *         <code>-1</code> if there is no more data because the end
     *         of the stream has been reached.
     * @exception NullPointerException If <code>b</code> is <code>null</code>.
     * @exception IndexOutOfBoundsException If <code>off</code> is negative,
     *                <code>len</code> is negative, or <code>len</code> is greater than
     *                <code>b.length - off</code>
     * @exception IOException if the first byte cannot be read for any reason
     *                other than end of file, the stream has been closed and the underlying
     *                input stream does not support reading after close, or another I/O
     *                error occurs. Additionally it will occur when the timeout happens.
     * @see java.io.DataInputStream#read
     */
    public synchronized int read(byte b[], int off, int len, int timeoutMSecs) throws IOException {
        // Definition of Method which encapsulates the Read of data
        Callable<Integer> readTask = new Callable<Integer>() {
            @Override
            public Integer call() throws IOException {
                return in.read(b, off, len);
            }
        };
        try {
            Future<Integer> future = executor.submit(readTask);
            return future.get(timeoutMSecs, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            throw new IOException("executor failed", e);
        } catch (ExecutionException e) {
            throw new IOException("execution failed", e);
        } catch (InterruptedException e) {
            throw new IOException("read interrupted", e);
        } catch (TimeoutException e) {
            throw new IOException("read timeout", e);
        }
    }
}
