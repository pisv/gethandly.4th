/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.basic.ui;

import org.eclipse.handly.examples.basic.ui.internal.FooActivator;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Use this class to register components to be used within the IDE.
 * <p>
 * Note: Xtext-generated {@link FooActivator} implementation assumes 
 * that this class lives in this package. Don't rename/move this class.
 */
public class FooUiModule
    extends AbstractFooUiModule
{
    public FooUiModule(AbstractUIPlugin plugin)
    {
        super(plugin);
    }
}
