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
package org.apache.deltaspike.data.test.ee7.graph;

import static org.apache.deltaspike.data.test.ee7.util.TestDeployments.initDeployment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.UserTransaction;

import org.apache.deltaspike.data.api.QueryInvocationException;
import org.apache.deltaspike.data.impl.graph.EntityGraphException;
import org.apache.deltaspike.data.test.ee7.domain.Flat;
import org.apache.deltaspike.data.test.ee7.domain.Garage;
import org.apache.deltaspike.data.test.ee7.domain.House;
import org.apache.deltaspike.data.test.ee7.domain.Tenant;
import org.apache.deltaspike.data.test.ee7.service.HouseRepository;
import org.apache.deltaspike.test.category.WebEE7ProfileCategory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@Category(WebEE7ProfileCategory.class)
@RunWith(Arquillian.class)
public class HouseRepositoryTest
{

    public static String DS_PROPERTIES_WITH_ENV_AWARE_TX_STRATEGY = 
        "globalAlternatives.org.apache.deltaspike.jpa.spi.transaction.TransactionStrategy="
            + "org.apache.deltaspike.jpa.impl.transaction.EnvironmentAwareTransactionStrategy";

    @Deployment
    public static Archive<?> deployment()
    {
        return initDeployment()
            .addClass(HouseRepository.class)
            .addClasses(House.class, Flat.class, Garage.class, Tenant.class)
            .addAsWebInfResource(new StringAsset(DS_PROPERTIES_WITH_ENV_AWARE_TX_STRATEGY),
                "classes/META-INF/apache-deltaspike.properties");
    }

    @Inject
    private HouseRepository repository;

    @Inject
    private UserTransaction tx;

    @Produces
    @PersistenceContext
    private EntityManager entityManager;

    private PersistenceUnitUtil puu;

    @Test
    @InSequence(1)
    public void should_run_modifying_in_transaction() throws Exception
    {
        House house = repository.findByName("Bellevue");
        assertNotNull(house);
        assertNotNull(house.getId());
        assertEquals("Bellevue", house.getName());

        assertTrue(puu.isLoaded(house, "flats"));
        assertFalse(puu.isLoaded(house, "garages"));
    }

    @Test
    @InSequence(2)
    public void shouldNotLoadLazyAssociationsWithoutGraph() throws Exception
    {
        House house = repository.findOptionalByName("Bellevue");
        assertNotNull(house);
        assertNotNull(house.getId());
        assertEquals("Bellevue", house.getName());

        PersistenceUnitUtil puu = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

        assertFalse(puu.isLoaded(house, "flats"));
        assertFalse(puu.isLoaded(house, "garages"));
    }

    @Test
    @InSequence(3)
    public void should_combine_entity_graph_with_explicit_query() throws Exception
    {
        House house = repository.fetchByName("Bellevue");
        assertNotNull(house);
        assertNotNull(house.getId());
        assertEquals("Bellevue", house.getName());

        assertTrue(puu.isLoaded(house, "flats"));
        assertFalse(puu.isLoaded(house, "garages"));
    }

    @Test
    @InSequence(4)
    public void should_throw_on_invalid_graph() throws Exception
    {
        try
        {
            repository.fetchByNameWithInvalidGraph("Bellevue");
            fail("expected QueryInvocationException");
        }
        catch (QueryInvocationException e)
        {
            assertTrue(e.getCause() instanceof EntityGraphException);
        }
    }

    @Before
    public void init() throws Exception
    {
        puu = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        tx.begin();
        if (repository.count() == 0)
        {
            House house = new House();
            Flat flat1 = new Flat();
            flat1.setName("Flat 1");
            flat1.setHouse(house);

            Flat flat2 = new Flat();
            flat2.setName("Flat 2");
            flat2.setHouse(house);

            Garage garageA = new Garage();
            garageA.setName("Garage A");
            garageA.setHouse(house);

            Garage garageB = new Garage();
            garageB.setName("Garage B");
            garageB.setHouse(house);

            house.setName("Bellevue");
            house.setFlats(Arrays.asList(flat1, flat2));
            house.setGarages(Arrays.asList(garageA, garageB));

            entityManager.persist(house);
        }
        tx.commit();
    }
}
