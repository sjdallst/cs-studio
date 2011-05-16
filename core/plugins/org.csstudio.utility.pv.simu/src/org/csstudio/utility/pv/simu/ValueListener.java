/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.utility.pv.simu;

/** Listener to a DynamicValue
 *  @author Kay Kasemir
 */
public interface ValueListener
{
    /** Notification of change in value
     *  @param value The value that changed
     */
    public void changed(Value value);
}