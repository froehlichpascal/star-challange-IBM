/**
 * star-challenge
 *
 * (C) Copyright 2016 IBM Corporation
 * All rights reserved
 *
 * Creation date: 03.08.2016
 */
package com.ibm.intro.dao;

import com.ibm.intro.exception.DataStoreException;
import com.ibm.intro.model.AbstractObject;
import com.ibm.intro.model.Entity;
import com.ibm.intro.model.EntityState;

import java.util.*;

/**
 * The in memory storage for any kind of AbstractObject e.g. Production.
 * 
 * @author Richard Holzeis
 */
public abstract class InMemoryDao<O extends AbstractObject> {

	private HashSet<Entity<O>> store;

	/**
	 * Initializes an InMemoryDao object.
	 */
	public InMemoryDao() {
		this.store = new HashSet<Entity<O>>();
	}

	/**
	 * Tries to acquire the lock for a requested id. A lock may only be granted if it isn't already locked. If the
	 * business object is already locked by the requesting thread, the lock remains and the method does not block.
	 * 
	 * @param id: the id of the requested business object.
	 * @param wait: the time to wait for a lock in milliseconds.
	 * @return true if the lock has been acquired.
	 * @throws DataStoreException is thrown if the the lock could not be acquired.
	 */
	public boolean lock(String id, Long wait) throws DataStoreException {
		// #task 1: implement me!
		if (findEntityById(id).lock(wait)) {
			return true;
		} else {
			throw new DataStoreException("Lock could not be acquired");
		}
	}

	/**
	 * Tries to release a lock for a given id. A lock can only be release from the thread which holds the lock.
	 * 
	 * @param id: the id of the to be released business object.
	 * @return true or false whether the release was successful or not
	 */
	public boolean release(String id) {
		if (findEntityById(id).release()) {
			return true;
		}
		return false;
	}

	/**
	 * Finds the stored entity by the business object id.
	 * 
	 * @param id: the id of the business object.
	 * @return the stored entity.
	 */
	private Entity<O> findEntityById(String id) {
		synchronized (store) {
			for (Entity<O> entity : store) {
				if (!entity.retrieveId().equals(id)) {
					continue;
				}
				return entity;
			}
			return null;
		}
	}

	/**
	 * Persists a business object according to the entity state. Plausibility rules are applied to check if the action
	 * is valid. It implicitly sets a change date and resets the entity state to read after successful update. A persist
	 * on a read object will do nothing.
	 * 
	 * @param object: the business object to be created, updated or deleted.
	 * @return: the updated business object.
	 * @throws A DataStoreException if the persist failed.
	 */
	@SuppressWarnings("unchecked")
	public O persist(O object) throws DataStoreException {
		if (object == null) {
			return object;
		}

		switch (object.getEntityState()) {
		case CREATED: {
			if (object.getId() != null) {
				throw new DataStoreException("Cannot create already persited entity!");
			}
			object.setEntityState(EntityState.READ);
			// assign an id to created objects.
			object.setId(UUID.randomUUID().toString());
			object.setChangeDate(System.currentTimeMillis());

			// ensure mutual exclusion when accessing the internal storage.
			synchronized (store) {
				// objects are always kept as clone in the storage, in order to prevent
				// unintended updates.
				store.add(new Entity<O>((O) object.clone()));
			}
		}
			break;
		case UPDATED: {
			if (object.getId() == null) {
				throw new DataStoreException("Cannot update a created entity!");
			}

			Entity<O> entity = this.findEntityById(object.getId());
			object.setEntityState(EntityState.READ);
			object.setChangeDate(System.currentTimeMillis());
			// ensure mutual exclusion when accessing the internal storage.
			synchronized (store) {
				entity.update((O) object.clone());
			}
		}
			break;
		case DELETED: {
			// ensure mutual exclusion when accessing the internal storage.
			Entity<O> entity = this.findEntityById(object.getId());
			synchronized (store) {
				if (!store.remove(entity)) {
					throw new DataStoreException("Cannot delete entity!");
				}
			}
		}
			break;
		default:
			break;
		}

		return object;
	}

	/**
	 * Persists an array of objects.
	 * 
	 * @param objs the array of objects.
	 * @return the persisted list of objects.
	 * @throws A DataStoreException if the persist failed.
	 */
	public List<O> persist(@SuppressWarnings("unchecked") O... objs) throws DataStoreException {
		if (objs == null) {
			return new ArrayList<O>();
		}
		for (O obj : objs) {
			this.persist(obj);
		}
		return Arrays.asList(objs);
	}

	/**
	 * Persists a list of objects.
	 * 
	 * @param objs the array of objects.
	 * @return the persisted list of objects.
	 * @throws A DataStoreException if the persist failed.
	 */
	public List<O> persist(List<O> objs) throws DataStoreException {
		if (objs == null) {
			return objs;
		}
		for (O obj : objs) {
			this.persist(obj);
		}
		return objs;
	}

	/**
	 * Loads an object by the id. Note, that only a copy is returned!
	 * 
	 * @param id: the id of the requested object.
	 * @return the object or null if not found.
	 */
	public O load(String id) {
		Entity<O> entity = this.findEntityById(id);
		if (entity == null) {
			return null;
		}
		return entity.retrieveObject();
	}

	/**
	 * Lists all stored objects.
	 * 
	 * @return
	 */
	public List<O> list() {
		List<O> list = new ArrayList<O>();
		// ensure mutual exclusion when accessing the internal storage.
		synchronized (store) {
			for (Entity<O> entity : store) {
				list.add(entity.retrieveObject());
			}
		}
		return list;
	}

	/**
	 * Returns the last change date of the object or null if object does not exist.
	 * 
	 * @param id: the id of the requested object.
	 * @return the last change date.
	 */
	public Long loadLastChangeDate(String id) {
		O object = this.load(id);
		if (object == null) {
			return null;
		}
		return object.getChangeDate();
	}

	/**
	 * Clears the internal storage.
	 */
	public void evict() {
		this.store.clear();
	}
}
