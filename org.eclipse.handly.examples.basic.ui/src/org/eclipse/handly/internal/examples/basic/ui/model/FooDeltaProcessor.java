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
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.Handle;

/**
 * This class is used by the <code>FooModelManager</code> to process 
 * resource deltas and update the Foo Model accordingly.
 */
class FooDeltaProcessor
    implements IResourceDeltaVisitor
{
    private Set<String> oldFooProjectNames = new HashSet<String>();

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException
    {
        switch (delta.getResource().getType())
        {
        case IResource.ROOT:
            return processRoot(delta);

        case IResource.PROJECT:
            return processProject(delta);

        case IResource.FILE:
            return processFile(delta);

        default:
            return true;
        }
    }

    private boolean processRoot(IResourceDelta delta) throws CoreException
    {
        initOldFooProjectNames();
        return true;
    }

    private boolean processProject(IResourceDelta delta) throws CoreException
    {
        switch (delta.getKind())
        {
        case IResourceDelta.ADDED:
            return processAddedProject(delta);

        case IResourceDelta.REMOVED:
            return processRemovedProject(delta);

        case IResourceDelta.CHANGED:
            return processChangedProject(delta);

        default:
            return true;
        }
    }

    private boolean processAddedProject(IResourceDelta delta)
        throws CoreException
    {
        IProject project = (IProject)delta.getResource();
        if (project.hasNature(IFooProject.NATURE_ID))
        {
            IFooProject fooProject = FooModelCore.create(project);
            addToModel(fooProject);
        }
        return false;
    }

    private boolean processRemovedProject(IResourceDelta delta)
        throws CoreException
    {
        IProject project = (IProject)delta.getResource();
        if (wasFooProject(project))
        {
            IFooProject fooProject = FooModelCore.create(project);
            removeFromModel(fooProject);
        }
        return false;
    }

    private boolean processChangedProject(IResourceDelta delta)
        throws CoreException
    {
        IProject project = (IProject)delta.getResource();
        IFooProject fooProject = FooModelCore.create(project);

        if ((delta.getFlags() & IResourceDelta.OPEN) != 0)
        {
            if (project.isOpen())
            {
                if (project.hasNature(IFooProject.NATURE_ID))
                {
                    addToModel(fooProject);
                }
            }
            else
            {
                if (wasFooProject(project))
                {
                    removeFromModel(fooProject);
                }
            }
            return false;
        }

        boolean isFooProject = project.hasNature(IFooProject.NATURE_ID);
        if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0)
        {
            boolean wasFooProject = wasFooProject(project);
            if (wasFooProject != isFooProject)
            {
                // Foo nature has been added or removed
                if (isFooProject)
                {
                    addToModel(fooProject);
                }
                else
                {
                    removeFromModel(fooProject);
                }
                return false; // when Foo nature is added/removed don't process children
            }
        }

        if (isFooProject)
        {
            Body parentBody = findBody(fooProject.getParent());
            IHandle[] children = parentBody.getChildren();
            if (!Arrays.asList(children).contains(fooProject))
                addToModel(fooProject); // in case the project was removed then added then changed

            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean processFile(IResourceDelta delta)
    {
        switch (delta.getKind())
        {
        case IResourceDelta.ADDED:
            return processAddedFile(delta);

        case IResourceDelta.REMOVED:
            return processRemovedFile(delta);

        case IResourceDelta.CHANGED:
            return processChangedFile(delta);

        default:
            return false;
        }
    }

    private boolean processAddedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IFooFile fooFile = FooModelCore.create(file);
        if (fooFile != null)
            addToModel(fooFile);
        return false;
    }

    private boolean processRemovedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IFooFile fooFile = FooModelCore.create(file);
        if (fooFile != null)
            removeFromModel(fooFile);
        return false;
    }

    private boolean processChangedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IFooFile fooFile = FooModelCore.create(file);
        if (fooFile != null)
        {
            if ((delta.getFlags() & ~(IResourceDelta.MARKERS | IResourceDelta.SYNC)) != 0)
                contentChanged(fooFile);
        }
        return false;
    }

    private void initOldFooProjectNames() throws CoreException
    {
        IFooProject[] fooProjects = FooModelCore.getFooModel().getFooProjects();
        for (IFooProject fooProject : fooProjects)
        {
            oldFooProjectNames.add(fooProject.getName());
        }
    }

    private boolean wasFooProject(IProject project)
    {
        return oldFooProjectNames.contains(project.getName());
    }

    private void addToModel(IHandle element)
    {
        Body parentBody = findBody(element.getParent());
        if (parentBody != null)
            parentBody.addChild(element);
        close(element);
    }

    private void removeFromModel(IHandle element)
    {
        Body parentBody = findBody(element.getParent());
        if (parentBody != null)
            parentBody.removeChild(element);
        close(element);
    }

    private void contentChanged(IFooFile fooFile)
    {
        close(fooFile);
    }

    private static Body findBody(IHandle element)
    {
        return ((Handle)element).findBody();
    }

    private static void close(IHandle element)
    {
        ((Handle)element).close();
    }
}
