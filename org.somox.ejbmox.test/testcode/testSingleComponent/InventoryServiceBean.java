package de.hpi.sam.rubis.inventorymgmt.impl;

import java.io.FileInputStream;
import java.io.InputStream;

import de.hpi.sam.rubis.entity.Bid;
import de.hpi.sam.rubis.entity.Item;
import de.hpi.sam.rubis.inventorymgmt.InventoryService;
import de.hpi.sam.rubis.inventorymgmt.InventoryServiceException;
import de.hpi.sam.rubis.queryservice.QueryService;

/**
 * Implementation of the {@link InventoryService}.
 *
 * @author thomas
 *
 */
@Stateless(mappedName = InventoryService.MAPPED_NAME)
public class InventoryServiceBean {

    InputStream inStream = new FileInputStream("test");

    /**
     * {@inheritDoc}
     */
    @Override
    public int checkAvailabilityOfItem(final Item item) throws InventoryServiceException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reserveItem(final Item item, final int numberOfItems) throws InventoryServiceException {
        // internal call
        this.internalCall();
        // libary call
        this.inStream.available();
        // libary call
        this.inStream.close();
        // internal call containing
        internalCall();
        // end of method calls
    }


    private void internalCall() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelReservedItem(final Item item, final int numberOfItems) throws InventoryServiceException {

    }

}
