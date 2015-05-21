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
package org.eclipse.handly.internal.examples.basic.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.handly.model.ISourceElement;
import org.eclipse.handly.model.ISourceElementInfo;
import org.eclipse.handly.model.ISourceFile;
import org.eclipse.handly.util.TextRange;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Utilities for <code>ISourceElement</code>s.
 * <p>
 * Note how this code is free from specifics of the Foo Model, 
 * thanks to the uniform API provided by Handly.
 * </p>
 */
public class SourceElementUtil
{
    /**
     * Returns the smallest element within the given element that includes
     * the given source position, or <code>null</code> if the given position
     * is not within the source range of the given element, or if the
     * given element does not exist or an exception occurs while accessing
     * its corresponding resource. If no finer grained element is found
     * at the position, the given element is returned.
     * <p>
     * As a side effect, if the given element is contained in a source file,
     * the source file will be reconciled.
     * </p>
     *
     * @param element a source element (not <code>null</code>)
     * @param position a source position (0-based)
     * @return the innermost element enclosing the given source position,
     *  or <code>null</code> if none (including the given element)
     */
    public static ISourceElement getElementAt(ISourceElement element,
        int position)
    {
        ISourceFile sourceFile;
        if (element instanceof ISourceFile)
            sourceFile = (ISourceFile)element;
        else
            sourceFile = element.getAncestor(ISourceFile.class);
        if (sourceFile != null)
        {
            try
            {
                sourceFile.reconcile(false, null);
            }
            catch (CoreException e)
            {
                Activator.log(e.getStatus());
                return null;
            }
        }
        return element.getElementAt(position, null);
    }

    /**
     * Selects and reveals the identifying range of the given source element 
     * in the given text editor. Returns <code>false</code> if the identifying 
     * range is not set or cannot be obtained (e.g., the element does not exist). 
     *
     * @param textEditor not <code>null</code>
     * @param element not <code>null</code>
     * @return <code>true</code> if the element was successfully revealed 
     *  in the editor; <code>false</code> otherwise
     */
    public static boolean revealInTextEditor(ITextEditor textEditor,
        ISourceElement element)
    {
        ISourceElementInfo info;
        try
        {
            info = element.getSourceElementInfo();
        }
        catch (CoreException e)
        {
            if (!element.exists())
                ; // this is considered normal
            else
                Activator.log(e.getStatus());
            return false;
        }
        TextRange identifyingRange = info.getIdentifyingRange();
        if (identifyingRange == null)
            return false;
        textEditor.selectAndReveal(identifyingRange.getOffset(),
            identifyingRange.getLength());
        return true;
    }

    private SourceElementUtil()
    {
    }
}
