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

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.Path;
import org.eclipse.handly.examples.basic.ui.model.FooModelCore;
import org.eclipse.handly.examples.basic.ui.model.IFooFile;
import org.eclipse.handly.examples.basic.ui.model.IFooModel;
import org.eclipse.handly.examples.basic.ui.model.IFooProject;
import org.eclipse.handly.junit.WorkspaceTestCase;
import org.eclipse.handly.model.IElementChangeEvent;
import org.eclipse.handly.model.IElementChangeListener;
import org.eclipse.handly.model.IElementDeltaConstants;
import org.eclipse.handly.model.impl.support.ElementDelta;

/**
 * Foo element change notification tests.
 */
public class FooModelNotificationTest
    extends WorkspaceTestCase
{
    private IFooModel fooModel = FooModelCore.getFooModel();
    private FooModelListener listener = new FooModelListener();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        setUpProject("Test001");
        fooModel.addElementChangeListener(listener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        fooModel.removeElementChangeListener(listener);
        super.tearDown();
    }

    public void testFooModelNotification() throws Exception
    {
        IFooProject fooProject1 = fooModel.getFooProject("Test001");
        IFooProject fooProject2 = fooModel.getFooProject("Test002");

        setUpProject("Test002");
        assertDelta(newDeltaBuilder().added(fooProject2).getDelta(),
            listener.delta);

        IFooFile fooFile1 = fooProject1.getFooFile("test.foo");
        fooFile1.getFile().touch(null);
        assertDelta(newDeltaBuilder().changed(fooFile1,
            IElementDeltaConstants.F_CONTENT).getDelta(), listener.delta);

        fooFile1.getFile().copy(new Path("/Test002/test1.foo"), true, null);
        assertDelta(newDeltaBuilder().added(fooProject2.getFooFile(
            "test1.foo")).getDelta(), listener.delta);

        fooFile1.getFile().delete(true, null);
        assertDelta(newDeltaBuilder().removed(fooFile1).getDelta(),
            listener.delta);

        IFooFile fooFile2 = fooProject2.getFooFile("test.foo");
        IFooFile movedFooFile2 = fooProject1.getFooFile("test1.foo");
        fooFile2.getFile().move(new Path("/Test001/test1.foo"), true, null);
        assertDelta(newDeltaBuilder().movedTo(movedFooFile2,
            fooFile2).movedFrom(fooFile2, movedFooFile2).getDelta(),
            listener.delta);

        fooProject2.getProject().close(null);
        assertDelta(newDeltaBuilder().removed(fooProject2,
            IElementDeltaConstants.F_OPEN).getDelta(), listener.delta);

        fooProject2.getProject().open(null);
        assertDelta(newDeltaBuilder().added(fooProject2,
            IElementDeltaConstants.F_OPEN).getDelta(), listener.delta);

        fooProject2.getProject().delete(true, null);
        assertDelta(newDeltaBuilder().removed(fooProject2).getDelta(),
            listener.delta);

        IProjectDescription description =
            fooProject1.getProject().getDescription();
        String[] oldNatures = description.getNatureIds();
        description.setNatureIds(new String[0]);
        fooProject1.getProject().setDescription(description, null);
        assertDelta(newDeltaBuilder().removed(fooProject1,
            IElementDeltaConstants.F_DESCRIPTION).getDelta(), listener.delta);

        description.setNatureIds(oldNatures);
        fooProject1.getProject().setDescription(description, null);
        assertDelta(newDeltaBuilder().added(fooProject1,
            IElementDeltaConstants.F_DESCRIPTION).getDelta(), listener.delta);

        IFooProject movedFooProject1 = fooModel.getFooProject("Test");
        fooProject1.getProject().move(new Path("Test"), true, null);
        assertDelta(newDeltaBuilder().movedTo(movedFooProject1,
            fooProject1).movedFrom(fooProject1, movedFooProject1).getDelta(),
            listener.delta);
    }

    private ElementDelta.Builder newDeltaBuilder()
    {
        return new ElementDelta.Builder(new ElementDelta(fooModel));
    }

    private static void assertDelta(ElementDelta expected, ElementDelta actual)
    {
        if (expected == null)
        {
            assertNull(actual);
            return;
        }
        assertNotNull(actual);
        assertEquals(expected.getElement_(), actual.getElement_());
        assertEquals(expected.getKind_(), actual.getKind_());
        assertEquals(expected.getFlags_(), actual.getFlags_());
        assertEquals(expected.getMovedToElement_(),
            actual.getMovedToElement_());
        assertEquals(expected.getMovedFromElement_(),
            actual.getMovedFromElement_());
        ElementDelta[] expectedChildren = expected.getAffectedChildren_();
        ElementDelta[] actualChildren = actual.getAffectedChildren_();
        assertEquals(expectedChildren.length, actualChildren.length);
        for (int i = 0; i < expectedChildren.length; i++)
            assertDelta(expectedChildren[i], actualChildren[i]);
    }

    private static class FooModelListener
        implements IElementChangeListener
    {
        public ElementDelta delta;

        @Override
        public void elementChanged(IElementChangeEvent event)
        {
            delta = (ElementDelta)event.getDeltas()[0];
        }
    }
}
