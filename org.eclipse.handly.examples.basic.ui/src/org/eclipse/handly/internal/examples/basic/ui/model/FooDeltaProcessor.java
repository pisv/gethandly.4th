/*******************************************************************************
 * Copyright (c) 2014, 2016 1C-Soft LLC and others.
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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooElement;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.IElementDeltaConstants;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.Element;
import org.eclipse.handly.model.impl.ElementDelta;

/**
 * This class is used by the <code>FooModelManager</code> to process 
 * resource deltas, convert them into Foo element deltas,
 * and update the Foo Model accordingly.
 */
class FooDeltaProcessor
    implements IResourceDeltaVisitor
{
    private ElementDelta.Builder builder = new ElementDelta.Builder(
        new ElementDelta(FooModelCore.getFooModel()));
    private Set<String> oldFooProjectNames = new HashSet<String>();

    /**
     * Returns the Foo element delta built from the resource delta. 
     * Returns an empty delta if no Foo elements were affected 
     * by the resource change.
     * 
     * @return Foo element delta (never <code>null</code>)
     */
    public IElementDelta getDelta()
    {
        return builder.getDelta();
    }

    /**
     * Returns whether Foo elements were affected by the resource change.
     *
     * @return <code>true</code> if no Foo elements were affected,
     *  and <code>false</code> otherwise
     */
    public boolean isEmptyDelta()
    {
        return builder.isEmptyDelta();
    }

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
            translateAddedDelta(delta, fooProject);
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
            translateRemovedDelta(delta, fooProject);
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
                    builder.added(fooProject, IElementDeltaConstants.F_OPEN);
                }
            }
            else
            {
                if (wasFooProject(project))
                {
                    removeFromModel(fooProject);
                    builder.removed(fooProject, IElementDeltaConstants.F_OPEN);
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
                    builder.added(fooProject,
                        IElementDeltaConstants.F_DESCRIPTION);
                }
                else
                {
                    removeFromModel(fooProject);
                    builder.removed(fooProject,
                        IElementDeltaConstants.F_DESCRIPTION);
                }
                return false; // when Foo nature is added/removed don't process children
            }
            else
            {
                if (isFooProject)
                {
                    builder.changed(fooProject,
                        IElementDeltaConstants.F_DESCRIPTION);
                }
            }
        }

        if (isFooProject)
        {
            Body parentBody = findBody(fooProject.getParent());
            IElement[] children = parentBody.getChildren();
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
        {
            addToModel(fooFile);
            translateAddedDelta(delta, fooFile);
        }
        return false;
    }

    private boolean processRemovedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IFooFile fooFile = FooModelCore.create(file);
        if (fooFile != null)
        {
            removeFromModel(fooFile);
            translateRemovedDelta(delta, fooFile);
        }
        return false;
    }

    private boolean processChangedFile(IResourceDelta delta)
    {
        IFile file = (IFile)delta.getResource();
        IFooFile fooFile = FooModelCore.create(file);
        if (fooFile != null)
        {
            if ((delta.getFlags() & ~(IResourceDelta.MARKERS
                | IResourceDelta.SYNC)) != 0)
            {
                contentChanged(fooFile);
            }
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

    private void addToModel(IElement element)
    {
        Body parentBody = findBody(Elements.getParent(element));
        if (parentBody != null)
            parentBody.addChild(element);
        close(element);
    }

    private void removeFromModel(IElement element)
    {
        Body parentBody = findBody(Elements.getParent(element));
        if (parentBody != null)
            parentBody.removeChild(element);
        close(element);
    }

    private void translateAddedDelta(IResourceDelta delta, IFooElement element)
    {
        if ((delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) // regular addition
        {
            builder.added(element);
        }
        else
        {
            IFooElement movedFromElement = FooModelCore.create(getResource(
                delta.getMovedFromPath(), delta.getResource().getType()));
            if (movedFromElement == null)
                builder.added(element);
            else
                builder.movedTo(element, movedFromElement);
        }
    }

    private void translateRemovedDelta(IResourceDelta delta,
        IFooElement element)
    {
        if ((delta.getFlags() & IResourceDelta.MOVED_TO) == 0) // regular removal
        {
            builder.removed(element);
        }
        else
        {
            IFooElement movedToElement = FooModelCore.create(getResource(
                delta.getMovedToPath(), delta.getResource().getType()));
            if (movedToElement == null)
                builder.removed(element);
            else
                builder.movedFrom(element, movedToElement);
        }
    }

    private void contentChanged(IFooFile fooFile)
    {
        if (fooFile.isWorkingCopy())
        {
            builder.changed(fooFile, IElementDeltaConstants.F_CONTENT
                | IElementDeltaConstants.F_UNDERLYING_RESOURCE);
            return;
        }

        close(fooFile);
        builder.changed(fooFile, IElementDeltaConstants.F_CONTENT);
    }

    private static Body findBody(IElement element)
    {
        return (Body)((Element)element).hFindBody();
    }

    private static void close(IElement element)
    {
        ((Element)element).hClose();
    }

    private static IResource getResource(IPath fullPath, int resourceType)
    {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        switch (resourceType)
        {
        case IResource.ROOT:
            return root;

        case IResource.PROJECT:
            return root.getProject(fullPath.lastSegment());

        case IResource.FOLDER:
            return root.getFolder(fullPath);

        case IResource.FILE:
            return root.getFile(fullPath);

        default:
            return null;
        }
    }
}
