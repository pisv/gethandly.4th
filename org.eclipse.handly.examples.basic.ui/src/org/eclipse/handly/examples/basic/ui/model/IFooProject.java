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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.internal.examples.basic.ui.model.FooProjectNature;

/**
 * Represents a Foo project.
 */
public interface IFooProject
    extends IFooElement
{
    /**
     * Foo project nature id.
     */
    String NATURE_ID = FooProjectNature.ID;

    @Override
    default IFooModel getParent()
    {
        return (IFooModel)IFooElement.super.getParent();
    }

    /**
     * Returns the Foo file with the given name in this project, or 
     * <code>null</code> if unable to associate the given name 
     * with a Foo file. The name has to be a valid file name. 
     * This is a handle-only method. The Foo file may or may not exist.
     * 
     * @param name the name of the Foo file (not <code>null</code>)
     * @return the Foo file with the given name in this project, 
     *  or <code>null</code> if unable to associate the given name 
     *  with a Foo file 
     */
    IFooFile getFooFile(String name);

    /**
     * Returns the Foo files contained in this project.
     *
     * @return the Foo files contained in this project (never <code>null</code>)
     * @throws CoreException if this element does not exist or if an exception 
     *  occurs while accessing its corresponding resource
     */
    IFooFile[] getFooFiles() throws CoreException;

    /**
     * Returns the <code>IProject</code> on which this <code>IFooProject</code>
     * was created. This is handle-only method.
     *
     * @return the <code>IProject</code> on which this <code>IFooProject</code>
     *  was created (never <code>null</code>)
     */
    IProject getProject();
}
