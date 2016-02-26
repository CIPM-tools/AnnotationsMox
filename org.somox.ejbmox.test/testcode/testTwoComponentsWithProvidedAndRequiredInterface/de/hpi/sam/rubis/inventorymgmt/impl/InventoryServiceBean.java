package de.hpi.sam.rubis.inventorymgmt.impl;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import de.hpi.sam.rubis.entity.InventoryItem;
import de.hpi.sam.rubis.entity.Item;
import de.hpi.sam.rubis.inventorymgmt.InventoryService;
import de.hpi.sam.rubis.inventorymgmt.InventoryServiceException;
import de.hpi.sam.rubis.persistenceservice.BusinessObjectsPersistenceService;
import de.hpi.sam.rubis.persistenceservice.BusinessObjectsPersistenceServiceException;
import de.hpi.sam.rubis.queryservice.QueryService;
import de.hpi.sam.rubis.queryservice.QueryServiceException;

/**
 * Implementation of the {@link InventoryService}.
 * 
 * @author thomas
 * 
 */
@Stateless(mappedName = InventoryService.MAPPED_NAME)
public class InventoryServiceBean implements InventoryService {

	@EJB(mappedName = QueryService.MAPPED_NAME)
	private QueryService queryService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int checkAvailabilityOfItem(Item item)
			throws InventoryServiceException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean reserveItem(Item item, int numberOfItems)
			throws InventoryServiceException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void cancelReservedItem(Item item, int numberOfItems)
			throws InventoryServiceException {
		
	}

}
