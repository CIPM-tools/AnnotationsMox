package de.hpi.sam.rubis.inventorymgmt.impl;

import java.io.FileInputStream;
import java.io.InputStream;

import de.hpi.sam.rubis.entity.Bid;
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

    InputStream inStream = new FileInputStream("test");

    private final Bid bid;
    
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
        // dummy loop is necessray to create a SEFF --> JaMoPPStatementVisitor thinks we do some actual work here
        int j = 0;
        for(int i = 0; i < 10; i++){
            j += i;
        }
        // internal call
        this.internalCall();
        // libary call
        this.inStream.available();
        // libary call
        bid.getBidPrice();
        // internal call containing external call
        this.internalCall();
        // end of method calls
    }

    private void internalCall() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelReservedItem(final Item item, final int numberOfItems) throws InventoryServiceException {

    }

}
