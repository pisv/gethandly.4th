/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.examples.basic.ui.model;

import org.eclipse.handly.model.ISourceConstruct;
import org.eclipse.handly.model.ISourceElementExtension;

/**
 * Represents a variable declared in a Foo file.
 */
public interface IFooVar
    extends IFooElement, ISourceConstruct, ISourceElementExtension
{
    @Override
    default IFooFile getParent()
    {
        return (IFooFile)IFooElement.super.getParent();
    }
}
