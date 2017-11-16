/*******************************************************************************
 * Copyright (c) 2014, 2017 1C-Soft LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.internal.examples.basic.ui.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.handly.context.IContext;
import org.eclipse.handly.examples.basic.foo.Module;
import org.eclipse.handly.examples.basic.ui.model.IFooDef;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooVar;
import org.eclipse.handly.internal.examples.basic.ui.Activator;
import org.eclipse.handly.model.IElement;
import org.eclipse.handly.model.impl.support.SourceElementBody;
import org.eclipse.handly.model.impl.support.WorkspaceSourceFile;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;

/**
 * Represents a Foo source file.
 */
public class FooFile
    extends WorkspaceSourceFile
    implements IFooFile, IFooElementInternal
{
    /**
     * Constructs a handle for a Foo file with the given parent element 
     * and the given underlying workspace file.
     * 
     * @param parent the parent of the element (not <code>null</code>)
     * @param file the workspace file underlying the element (not <code>null</code>)
     * @throws IllegalArgumentException if the handle cannot be constructed 
     *  on the given workspace file
     */
    public FooFile(FooProject parent, IFile file)
    {
        super(parent, file);
        if (!file.getParent().equals(parent.getProject()))
            throw new IllegalArgumentException();
        if (!EXT.equals(file.getFileExtension()))
            throw new IllegalArgumentException();
    }

    @Override
    public IFooVar getVar(String name)
    {
        return new FooVar(this, name);
    }

    @Override
    public IFooVar[] getVars() throws CoreException
    {
        return getChildren(IFooVar.class);
    }

    @Override
    public IFooDef getDef(String name, int arity)
    {
        return new FooDef(this, name, arity);
    }

    @Override
    public IFooDef[] getDefs() throws CoreException
    {
        return getChildren(IFooDef.class);
    }

    @Override
	public void buildSourceStructure_(IContext context,
	    IProgressMonitor monitor) throws CoreException
    {
        Map<IElement, Object> newElements = context.get(NEW_ELEMENTS);
        SourceElementBody body = new SourceElementBody();

        XtextResource resource = (XtextResource)context.get(SOURCE_AST);
        if (resource == null)
        {
            try
            {
                resource = parse(context.get(SOURCE_CONTENTS),
                    getFile().getCharset());
            }
            catch (IOException e)
            {
                throw new CoreException(Activator.createErrorStatus(
                    e.getMessage(), e));
            }
        }

        IParseResult parseResult = resource.getParseResult();
        if (parseResult != null)
        {
            EObject root = parseResult.getRootASTElement();
            if (root instanceof Module)
            {
                FooFileStructureBuilder builder = new FooFileStructureBuilder(
                    newElements, resource.getResourceServiceProvider());
                builder.buildStructure(this, body, (Module)root);
            }
        }

        newElements.put(this, body);
    }

    /**
     * Returns a new <code>XtextResource</code> loaded from the given
     * contents. The resource is created in a new <code>ResourceSet</code>
     * obtained from the <code>IResourceSetProvider</code> corresponding
     * to this file. The resource's encoding is set to the given value.
     * This is a handle-only method.
     *
     * @param contents the contents to parse (not <code>null</code>)
     * @param encoding the encoding to be set for the created
     *  <code>XtextResource</code> (not <code>null</code>)
     * @return the new <code>XtextResource</code> loaded from
     *  the given contents (never <code>null</code>)
     * @throws IOException if resource loading failed 
     */
    protected XtextResource parse(String contents, String encoding)
        throws IOException
    {
        IResourceSetProvider resourceSetProvider =
            getResourceServiceProvider().get(IResourceSetProvider.class);
        ResourceSet resourceSet = resourceSetProvider.get(
            getFile().getProject());
        XtextResource resource = (XtextResource)resourceSet.createResource(
            getResourceUri());
        resource.load(new ByteArrayInputStream(contents.getBytes(encoding)),
            Collections.singletonMap(XtextResource.OPTION_ENCODING, encoding));
        return resource;
    }

    /**
     * Returns the <code>IResourceSetProvider</code> corresponding to
     * this file. This is a handle-only method.
     *
     * @return the <code>IResourceSetProvider</code> for this file 
     *  (never <code>null</code>)
     */
    protected IResourceServiceProvider getResourceServiceProvider()
    {
        IResourceServiceProvider provider =
            IResourceServiceProvider.Registry.INSTANCE.getResourceServiceProvider(
                getResourceUri());
        if (provider == null)
            throw new AssertionError();
        return provider;
    }

    /**
     * Returns the EMF resource URI for this file. 
     * This is a handle-only method.
     *
     * @return the resource URI for this file (never <code>null</code>)
     */
    protected URI getResourceUri()
    {
        return URI.createPlatformResourceURI(getFile_().getFullPath().toString(),
            true);
    }
}
