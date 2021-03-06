/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.test.jsf.impl.config.view.controller.uc001;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.spi.config.view.ViewConfigNode;
import org.apache.deltaspike.core.api.config.view.controller.ViewControllerRef;
import org.apache.deltaspike.core.api.config.view.controller.PreRenderView;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigExtension;
import org.apache.deltaspike.jsf.impl.config.view.ViewConfigResolverProducer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class ViewConfigTest
{
    private ViewConfigExtension viewConfigExtension;

    private ViewConfigResolverProducer viewConfigResolverProducer;

    @Before
    public void before()
    {
        this.viewConfigExtension = new ViewConfigExtension();
        this.viewConfigResolverProducer = new ViewConfigResolverProducer(this.viewConfigExtension);
    }

    @After
    public void after()
    {
        this.viewConfigExtension.freeViewConfigCache(null);
    }

    @Test
    public void testSimpleMetaDataTreeWithViewControllerCallback()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig.class);

        ViewConfigNode node = this.viewConfigExtension.findNode(SimplePageConfig.class);

        Assert.assertNotNull(node);
        Assert.assertNotNull(node.getParent());
        Assert.assertNull(node.getParent().getParent());

        Assert.assertNotNull(node.getChildren());
        Assert.assertEquals(0, node.getChildren().size());

        Assert.assertNotNull(node.getMetaData());
        Assert.assertEquals(1, node.getMetaData().size());

        Assert.assertNotNull(node.getInheritedMetaData());
        Assert.assertEquals(0, node.getInheritedMetaData().size());

        Assert.assertNotNull(node.getCallbackDescriptors());
        //TODO related to the discussion about #getInheritedMetaData (see TODOs in other use-cases)
        Assert.assertEquals(0, node.getCallbackDescriptors().size());
    }

    @Test
    public void testSimpleViewConfigWithViewControllerCallback()
    {
        this.viewConfigExtension.addPageDefinition(SimplePageConfig.class);

        ViewConfigResolver viewConfigResolver = this.viewConfigResolverProducer.createViewConfigResolver();
        ViewConfigDescriptor viewConfigDescriptor = viewConfigResolver.getViewConfigDescriptor(SimplePageConfig.class);

        Assert.assertNotNull(viewConfigDescriptor);
        Assert.assertNotNull(viewConfigDescriptor.getCallbackDescriptor(ViewControllerRef.class, PreRenderView.class));
        Assert.assertEquals(PageBean001.class, viewConfigDescriptor.getCallbackDescriptor(ViewControllerRef.class, PreRenderView.class).getCallbackMethods().keySet().iterator().next());
        Assert.assertEquals("preRenderViewCallbackMethod", ((List<Method>) viewConfigDescriptor.getCallbackDescriptor(ViewControllerRef.class, PreRenderView.class).getCallbackMethods().values().iterator().next()).iterator().next().getName());
    }
}
