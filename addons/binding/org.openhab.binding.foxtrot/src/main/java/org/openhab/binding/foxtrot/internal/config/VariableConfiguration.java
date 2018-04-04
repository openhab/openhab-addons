/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.foxtrot.internal.config;

import java.math.BigDecimal;

/**
 * FoxtrotNumberConfiguration.
 *
 * @author Radovan Sninsky
 * @since 2018-02-11 13:04
 */
public class VariableConfiguration {

  /**
   * PLC's variable name published in public.pub file.
   */
  public String var;

  /**
   * Value change delta to receive new state from Plc.
   */
  public BigDecimal delta;
}
