/**
 * star-challenge
 *
 * (C) Copyright 2016 IBM Corporation
 * All rights reserved
 *
 * Creation date: 03.08.2016
 */
package com.ibm.intro;

import com.ibm.intro.dao.ProductionDao;
import com.ibm.intro.exception.DataStoreException;
import com.ibm.intro.model.EntityState;
import com.ibm.intro.model.Production;

import java.util.List;

/**
 * @author Richard Holzeis
 */
public class ProductionService implements CRUDService<Production> {

	/*
	 * #task 2: implement me!
	 */
	@Override
	public List<Production> findAll() {

		return ProductionDao.getInstance().list();
	}

	@Override
	public Production findById(String id) {
		return ProductionDao.getInstance().load(id);
	}

	public Production save(Production object) throws DataStoreException {
		try {
			object = ProductionDao.getInstance().persist(object);
		} catch (DataStoreException dataStoreException) {
			dataStoreException.printStackTrace();
		}
		return object;
	}

	@Override
	public List<Production> create(List<Production> objects) {
		try {
			objects = ProductionDao.getInstance().persist(objects);
		} catch (DataStoreException dataStoreException) {
			dataStoreException.printStackTrace();
		}
		return objects;
	}

	@Override
	public Production create(Production object) throws DataStoreException {
		object.setEntityState(EntityState.CREATED);
		return save(object);
	}

	@Override
	public Production update(Production object) throws DataStoreException {
		ProductionDao.getInstance().lock(object.getId(), 1L);
		object.setEntityState(EntityState.UPDATED);
		ProductionDao.getInstance().release(object.getId());
		return save(object);
	}

	@Override
	public void delete(Production object) throws DataStoreException {
		object.setEntityState(EntityState.DELETED);
		save(object);
	}

}
