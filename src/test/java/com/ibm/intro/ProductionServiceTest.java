/**
 * star-challenge
 * <p>
 * (C) Copyright 2016 IBM Corporation
 * All rights reserved
 * <p>
 * Creation date: 03.08.2016
 */
package com.ibm.intro;

import com.ibm.intro.exception.DataStoreException;
import com.ibm.intro.model.Production;
import com.ibm.intro.model.ProductionType;
import com.ibm.intro.model.Status;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Richard Holzeis
 */
public class ProductionServiceTest {

	/*
	 * #task 2: implement me!
	 */


	@Test
	public void testSaveAndRead() throws DataStoreException {
		ProductionService service = new ProductionService();
		final Production production = new Production(ProductionType.COLLECTION, 1L);

		Production pro = service.create(production);

		Assert.assertEquals(production, service.findById(pro.getId()));
	}

	@Test
	public void testReadList() {
		ProductionService service = new ProductionService();
		List<Production> productionList = new ArrayList<Production>(Arrays.asList(
				new Production(ProductionType.COLLECTION, 1L),
				new Production(ProductionType.DELIVERY, 1L)));

		service.create(productionList);

		Assert.assertTrue("List is Empty", !service.findAll().isEmpty());
	}

	@Test
	public void testUpdate() throws DataStoreException {
		ProductionService service = new ProductionService();
		final Production production = new Production(ProductionType.COLLECTION, 1L);

		Production pro = service.create(production);

		Assert.assertEquals(ProductionType.COLLECTION, pro.getProductionType());


		pro.setProductionType(ProductionType.DELIVERY);
		service.update(pro);

		Assert.assertEquals(ProductionType.DELIVERY, service.findById(pro.getId()).getProductionType());
	}

	@Test
	public void testDelete() throws DataStoreException {
		ProductionService service = new ProductionService();
		final Production production = new Production(ProductionType.COLLECTION, 1L);

		Production pro = service.create(production);

		Assert.assertEquals(service.findById(pro.getId()), production);

		service.delete(pro);

		Assert.assertTrue("is still there", service.findById(pro.getId()) == null);
	}

	@Test
	public void testLocking() throws InterruptedException, DataStoreException {
		ProductionService service = new ProductionService();
		Production production = new Production(ProductionType.COLLECTION, 1L);
		final Production protest = service.create(production);
		List<Status> testList = Arrays.asList(Status.CREATED, Status.COMPLETED);

		// when
		final ExecutorService executor = Executors.newFixedThreadPool(testList.size());

		for (Status testStatus : testList) {
			executor.execute(() -> {
				try {
					protest.setStatus(testStatus);
					service.update(protest);
				} catch (DataStoreException dataStoreException) {
					dataStoreException.printStackTrace();
				}
			});
		}

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);

		Assert.assertEquals(Status.COMPLETED, service.findById(protest.getId()).getStatus());
	}


}
