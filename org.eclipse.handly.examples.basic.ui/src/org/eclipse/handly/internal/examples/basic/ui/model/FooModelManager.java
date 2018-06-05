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

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.context.Context;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.internal.examples.basic.ui.Activator;
import org.eclipse.handly.model.ElementDeltas;
import org.eclipse.handly.model.IElementDelta;
import org.eclipse.handly.model.impl.support.ElementChangeEvent;
import org.eclipse.handly.model.impl.support.ElementManager;
import org.eclipse.handly.model.impl.support.IModelManager;
import org.eclipse.handly.model.impl.support.INotificationManager;
import org.eclipse.handly.model.impl.support.NotificationManager;

/**
 * The manager for the Foo Model. 
 *
 * @threadsafe This class is intended to be thread-safe
 */
public class FooModelManager
    implements IModelManager, IResourceChangeListener
{
    /**
     * The sole instance of the manager. 
     */
    public static final FooModelManager INSTANCE = new FooModelManager();

    private FooModel fooModel;
    private ElementManager elementManager;
    private NotificationManager notificationManager;
    private Context modelContext;

    public void startup() throws Exception
    {
        fooModel = new FooModel();
        elementManager = new ElementManager(new FooModelCache());
        notificationManager = new NotificationManager();
        modelContext = new Context();
        modelContext.bind(INotificationManager.class).to(notificationManager);
        fooModel.getWorkspace().addResourceChangeListener(this,
            IResourceChangeEvent.POST_CHANGE);
    }

    public void shutdown() throws Exception
    {
        fooModel.getWorkspace().removeResourceChangeListener(this);
        modelContext = null;
        notificationManager = null;
        elementManager = null;
        fooModel = null;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event)
    {
        FooDeltaProcessor deltaProcessor = new FooDeltaProcessor();
        try
        {
            event.getDelta().accept(deltaProcessor);
        }
        catch (CoreException e)
        {
            Activator.log(e.getStatus());
        }
        IElementDelta delta = deltaProcessor.getDelta();
        if (!ElementDeltas.isEmpty(delta))
        {
            getNotificationManager().fireElementChangeEvent(
                new ElementChangeEvent(ElementChangeEvent.POST_CHANGE,
                    delta));
        }
    }

    @Override
    public FooModel getModel()
    {
        if (fooModel == null)
            throw new IllegalStateException();
        return fooModel;
    }

    @Override
    public ElementManager getElementManager()
    {
        if (elementManager == null)
            throw new IllegalStateException();
        return elementManager;
    }

    public NotificationManager getNotificationManager()
    {
        if (notificationManager == null)
            throw new IllegalStateException();
        return notificationManager;
    }

    public IContext getModelContext()
    {
        if (modelContext == null)
            throw new IllegalStateException();
        return modelContext;
    }

    private FooModelManager()
    {
    }
}
