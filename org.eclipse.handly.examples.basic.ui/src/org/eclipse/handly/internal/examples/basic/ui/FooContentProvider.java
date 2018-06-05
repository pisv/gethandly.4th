/*******************************************************************************
 * Copyright (c) 2014 1C LLC.
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
package org.eclipse.handly.internal.examples.basic.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.examples.basic.ui.model.IFooElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Foo content provider.
 */
public class FooContentProvider
    implements ITreeContentProvider
{
    protected static final Object[] NO_CHILDREN = new Object[0];

    @Override
    public Object[] getElements(Object inputElement)
    {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parentElement)
    {
        if (parentElement instanceof IFooElement)
        {
            try
            {
                return ((IFooElement)parentElement).getChildren();
            }
            catch (CoreException e)
            {
            }
        }
        return NO_CHILDREN;
    }

    @Override
    public Object getParent(Object element)
    {
        if (element instanceof IFooElement)
            return ((IFooElement)element).getParent();
        return null;
    }

    @Override
    public boolean hasChildren(Object element)
    {
        return getChildren(element).length > 0;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    @Override
    public void dispose()
    {
    }
}
