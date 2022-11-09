/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal;

/**
 *
 * Exception for errors during calls of the iCloud API.
 *
 * @author Simon Spielmann
 */
public class ICloudAPIResponseException extends RuntimeException {

  private int statusCode;

  /**
   * The constructor.
   *
   * @param url URL for which the exception occured
   * @param statusCode HTTP status code which was reported
   */
  public ICloudAPIResponseException(String url, int statusCode) {

    super(String.format("Request {} failed with {}.", url, statusCode));
    this.statusCode = statusCode;
  }
}
