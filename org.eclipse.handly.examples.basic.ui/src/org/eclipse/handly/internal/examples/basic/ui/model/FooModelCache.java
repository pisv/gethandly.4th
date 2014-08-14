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

import java.util.HashMap;

import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.model.IHandle;
import org.eclipse.handly.model.impl.Body;
import org.eclipse.handly.model.impl.ElementCache;
import org.eclipse.handly.model.impl.IBodyCache;

/**
 * The Foo Model cache.
 */
class FooModelCache
    implements IBodyCache
{
    private static final int DEFAULT_PROJECT_SIZE = 5;
    private static final int DEFAULT_FILE_SIZE = 100;
    private static final int DEFAULT_CHILDREN_SIZE = DEFAULT_FILE_SIZE * 20; // average 20 children per file

    private Body modelBody; // Foo model element's body
    private HashMap<IHandle, Body> projectCache; // cache of open Foo projects
    private ElementCache fileCache; // cache of open Foo files
    private HashMap<IHandle, Body> childrenCache; // cache of children of open Foo files

    public FooModelCache()
    {
        projectCache = new HashMap<IHandle, Body>(DEFAULT_PROJECT_SIZE);
        fileCache = new ElementCache(DEFAULT_FILE_SIZE);
        childrenCache = new HashMap<IHandle, Body>(DEFAULT_CHILDREN_SIZE);
    }

    @Override
    public Body get(IHandle handle)
    {
        if (handle instanceof IFooModel)
            return modelBody;
        else if (handle instanceof IFooProject)
            return projectCache.get(handle);
        else if (handle instanceof IFooFile)
            return fileCache.get(handle);
        else
            return childrenCache.get(handle);
    }

    @Override
    public Body peek(IHandle handle)
    {
        if (handle instanceof IFooModel)
            return modelBody;
        else if (handle instanceof IFooProject)
            return projectCache.get(handle);
        else if (handle instanceof IFooFile)
            return fileCache.peek(handle);
        else
            return childrenCache.get(handle);
    }

    @Override
    public void put(IHandle handle, Body body)
    {
        if (handle instanceof IFooModel)
            modelBody = body;
        else if (handle instanceof IFooProject)
        {
            projectCache.put(handle, body);
            fileCache.ensureSpaceLimit(body, handle);
        }
        else if (handle instanceof IFooFile)
            fileCache.put(handle, body);
        else
            childrenCache.put(handle, body);
    }

    @Override
    public void remove(IHandle handle)
    {
        if (handle instanceof IFooModel)
            modelBody = null;
        else if (handle instanceof IFooProject)
        {
            projectCache.remove(handle);
            fileCache.resetSpaceLimit(DEFAULT_FILE_SIZE, handle);
        }
        else if (handle instanceof IFooFile)
            fileCache.remove(handle);
        else
            childrenCache.remove(handle);
    }
}
