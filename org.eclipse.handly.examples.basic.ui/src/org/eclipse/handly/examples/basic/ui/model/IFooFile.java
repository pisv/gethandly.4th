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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.ISourceElementExtension;
import org.eclipse.handly.model.ISourceFileExtension;

/**
 * Represents a Foo source file.
 */
public interface IFooFile
    extends IFooElement, ISourceFileExtension, ISourceElementExtension
{
    /**
     * Foo file extension.
     */
    String EXT = "foo";

    @Override
    default IFooProject getParent()
    {
        return (IFooProject)IFooElement.super.getParent();
    }

    /**
     * Returns the variable with the given name declared in this Foo file.
     * This is a handle-only method. The variable may or may not exist.
     *
     * @param name the name of the requested variable in the Foo file
     * @return a handle onto the corresponding variable (never <code>null</code>). 
     *  The variable may or may not exist.
     */
    IFooVar getVar(String name);

    /**
     * Returns the variables declared in this Foo file in the order in which 
     * they appear in the source.
     *
     * @return the variables declared in this Foo file (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception 
     *  occurs while accessing its corresponding resource
     */
    IFooVar[] getVars() throws CoreException;

    /**
     * Returns the function with the given name and the given arity defined 
     * in this Foo file. This is a handle-only method. The function may or 
     * may not exist.
     *
     * @param name the name of the requested function in the Foo file
     * @param arity the arity of the requested function in the Foo file
     * @return a handle onto the corresponding function (never <code>null</code>). 
     *  The function may or may not exist.
     */
    IFooDef getDef(String name, int arity);

    /**
     * Returns the functions defined in this Foo file in the order in which 
     * they appear in the source.
     *
     * @return the functions defined in this Foo file (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception 
     *  occurs while accessing its corresponding resource
     */
    IFooDef[] getDefs() throws CoreException;
}
