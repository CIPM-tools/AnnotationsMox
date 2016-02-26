package de.hpi.sam.rubis.inventorymgmt.impl;

import de.hpi.sam.rubis.entity.Item;
import de.hpi.sam.rubis.inventorymgmt.InventoryService;
import de.hpi.sam.rubis.inventorymgmt.InventoryServiceException;

/**
 * Implementation of the {@link InventoryService}.
 *
 * @author thomas
 *
 */
@Stateless(mappedName = InventoryService.MAPPED_NAME)
public class InventoryServiceBean implements InventoryService {

    /**
     * {@inheritDoc}
     */
    @Override
    public int checkAvailabilityOfItem(final Item item) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reserveItem(final Item item, final int numberOfItems) throws InventoryServiceException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelReservedItem(final Item item, final int numberOfItems) throws InventoryServiceException {

    }

}
