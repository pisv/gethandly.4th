/*******************************************************************************
 * Copyright (c) 2014, 2015 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.internal.examples.basic.ui.Activator;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.Handle;
import org.eclipse.handly.model.impl.HandleManager;

/**
 * Represents a Foo project.
 */
public class FooProject
    extends Handle
    implements IFooProject
{
    private final IProject project;

    /**
     * Constructs a handle for a Foo project with the given parent element 
     * and the given underlying workspace project.
     * 
     * @param parent the parent of the element (not <code>null</code>)
     * @param project the workspace project underlying the element 
     *  (not <code>null</code>)
     */
    public FooProject(FooModel parent, IProject project)
    {
        super(parent, project.getName());
        if (parent == null)
            throw new IllegalArgumentException();
        this.project = project;
    }

    @Override
    public IFooFile getFooFile(String name)
    {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0)
            return null;
        String fileExtension = name.substring(lastDot + 1);
        if (!IFooFile.EXT.equals(fileExtension))
            return null;
        return new FooFile(this, project.getFile(name));
    }

    @Override
    public IFooFile[] getFooFiles() throws CoreException
    {
        IHandle[] children = getChildren();
        int length = children.length;
        IFooFile[] result = new IFooFile[length];
        System.arraycopy(children, 0, result, 0, length);
        return result;
    }

    @Override
    public IProject getProject()
    {
        return project;
    }

    @Override
    public IResource getResource()
    {
        return project;
    }

    @Override
    protected HandleManager getHandleManager()
    {
        return FooModelManager.INSTANCE.getHandleManager();
    }

    @Override
    protected void validateExistence() throws CoreException
    {
        if (!project.exists())
            throw new CoreException(Activator.createErrorStatus(
                MessageFormat.format(
                    "Project ''{0}'' does not exist in workspace", name),
                null));

        if (!project.isOpen())
            throw new CoreException(Activator.createErrorStatus(
                MessageFormat.format("Project ''{0}'' is not open", name),
                null));

        if (!project.hasNature(NATURE_ID))
            throw new CoreException(Activator.createErrorStatus(
                MessageFormat.format(
                    "Project ''{0}'' does not have the Foo nature", name),
                null));
    }

    @Override
    protected void buildStructure(Body body, Map<IHandle, Body> newElements,
        IProgressMonitor monitor) throws CoreException
    {
        IResource[] members = project.members();
        List<IFooFile> fooFiles = new ArrayList<IFooFile>(members.length);
        for (IResource member : members)
        {
            if (member instanceof IFile)
            {
                IFile file = (IFile)member;
                if (IFooFile.EXT.equals(file.getFileExtension()))
                {
                    IFooFile fooFile = new FooFile(this, file);
                    if (fooFile != null)
                        fooFiles.add(fooFile);
                }
            }
        }
        body.setChildren(fooFiles.toArray(new IHandle[fooFiles.size()]));
    }
}
