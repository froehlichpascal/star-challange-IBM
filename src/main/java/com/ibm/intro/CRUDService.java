/**
 * star-challenge
 *
 * (C) Copyright 2016 IBM Corporation
 * All rights reserved
 *
 * Creation date: 03.08.2016
 */
package com.ibm.intro;

import com.ibm.intro.exception.DataStoreException;
import com.ibm.intro.model.AbstractObject;

import java.util.List;

/**
 * @author Richard Holzeis
 * @param <O> the business object
 */
public interface CRUDService<O extends AbstractObject> {

	/*
	 * #task 2: define me
	 *
	 * create, read, update, delete
	 */

	List<O> findAll();

	O findById(String id);

	List<O> create(List<O> object);

	O create(O object) throws DataStoreException;

	O update(O object) throws DataStoreException;

	void delete(O object) throws DataStoreException;
}
