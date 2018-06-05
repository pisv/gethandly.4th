/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
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
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.handly.ApiLevel;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.Elements;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.impl.IModelImpl;
import org.eclipse.handly.model.impl.support.Body;
import org.eclipse.handly.model.impl.support.Element;

/**
 * Represents the root Foo element corresponding to the workspace. 
 */
public class FooModel
    extends Element
    implements IFooModel, IFooElementInternal, IModelImpl
{
    private final IWorkspace workspace;

    /**
     * Constructs a handle for the root Foo element.
     */
    public FooModel()
    {
        super(null, null);
        this.workspace = ResourcesPlugin.getWorkspace();
    }

    @Override
    public void addElementChangeListener(IElementChangeListener listener)
    {
        FooModelManager.INSTANCE.getNotificationManager().addElementChangeListener(
            listener);
    }

    @Override
    public void removeElementChangeListener(IElementChangeListener listener)
    {
        FooModelManager.INSTANCE.getNotificationManager().removeElementChangeListener(
            listener);
    }

    @Override
    public IFooProject getFooProject(String name)
    {
        return new FooProject(this, workspace.getRoot().getProject(name));
    }

    @Override
    public IFooProject[] getFooProjects() throws CoreException
    {
        IElement[] children = getChildren();
        int length = children.length;
        IFooProject[] result = new IFooProject[length];
        System.arraycopy(children, 0, result, 0, length);
        return result;
    }

    @Override
    public IWorkspace getWorkspace()
    {
        return workspace;
    }

    @Override
    public int getModelApiLevel_()
    {
        return ApiLevel.CURRENT;
    }

    @Override
    public IContext getModelContext_()
    {
        return FooModelManager.INSTANCE.getModelContext();
    }

    @Override
    public IResource getResource_()
    {
        return workspace.getRoot();
    }

    @Override
	public void validateExistence_(IContext context)
    {
        // always exists
    }

    @Override
	public void buildStructure_(IContext context, IProgressMonitor monitor)
        throws CoreException
    {
        IProject[] projects = workspace.getRoot().getProjects();
        List<IFooProject> fooProjects = new ArrayList<>(projects.length);
        for (IProject project : projects)
        {
            if (project.isOpen() && project.hasNature(IFooProject.NATURE_ID))
            {
                fooProjects.add(new FooProject(this, project));
            }
        }
        Body body = new Body();
        body.setChildren(fooProjects.toArray(Elements.EMPTY_ARRAY));
        context.get(NEW_ELEMENTS).put(this, body);
    }
}
