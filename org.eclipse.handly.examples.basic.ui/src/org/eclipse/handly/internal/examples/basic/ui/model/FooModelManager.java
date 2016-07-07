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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.internal.examples.basic.ui.Activator;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.impl.ElementChangeEvent;
import org.eclipse.handly.model.impl.ElementManager;

/**
 * The manager for the Foo Model. 
 *
 * @threadsafe This class is intended to be thread-safe
 */
public class FooModelManager
    implements IResourceChangeListener
{
    /**
     * The sole instance of the manager. 
     */
    public static final FooModelManager INSTANCE = new FooModelManager();

    private IFooModel fooModel;
    private ElementManager elementManager;
    private ListenerList<IElementChangeListener> listeners;

    public void startup() throws Exception
    {
        fooModel = new FooModel();
        elementManager = new ElementManager(new FooModelCache());
        listeners = new ListenerList<>();
        fooModel.getWorkspace().addResourceChangeListener(this,
            IResourceChangeEvent.POST_CHANGE);
    }

    public void shutdown() throws Exception
    {
        fooModel.getWorkspace().removeResourceChangeListener(this);
        listeners = null;
        elementManager = null;
        fooModel = null;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event)
    {
        if (event.getType() != IResourceChangeEvent.POST_CHANGE)
            return;
        FooDeltaProcessor deltaProcessor = new FooDeltaProcessor();
        try
        {
            event.getDelta().accept(deltaProcessor);
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
        }
        if (!deltaProcessor.isEmptyDelta())
        {
            fireElementChangeEvent(new ElementChangeEvent(
                ElementChangeEvent.POST_CHANGE, deltaProcessor.getDelta()));
        }
    }

    public IFooModel getFooModel()
    {
        if (fooModel == null)
            throw new IllegalStateException();
        return fooModel;
    }

    public ElementManager getElementManager()
    {
        if (elementManager == null)
            throw new IllegalStateException();
        return elementManager;
    }

    public void addElementChangeListener(IElementChangeListener listener)
    {
        if (listeners == null)
            throw new IllegalStateException();
        listeners.add(listener);
    }

    public void removeElementChangeListener(IElementChangeListener listener)
    {
        if (listeners == null)
            throw new IllegalStateException();
        listeners.remove(listener);
    }

    public void fireElementChangeEvent(final IElementChangeEvent event)
    {
        if (listeners == null)
            throw new IllegalStateException();
        for (IElementChangeListener listener : listeners)
        {
            SafeRunner.run(new ISafeRunnable()
            {
                public void handleException(Throwable exception)
                {
                    // already logged by Platform
                }

                public void run() throws Exception
                {
                    listener.elementChanged(event);
                }
            });
        }
    }

    private FooModelManager()
    {
    }
}
