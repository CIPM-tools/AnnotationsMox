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
public class InventoryServiceBean implements InventoryService {

    @EJB(mappedName = QueryService.MAPPED_NAME)
    private QueryService queryService;

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
        // external call
        final Bid bid = this.queryService.findItemMaxBid(0);
        // libary call
        this.inStream.available();
        // libary call
        bid.getBidPrice();
        // external call
        this.queryService.findPastItemsInCategoryAndRegion(0, 0, 0);
        // internal call containing external call
        this.internalCallContainingExternalCall();
        // end of method calls
    }

    private void internalCallContainingExternalCall() {
        //intenal stuff
        int i =0;
        i++;
        // external call
        this.queryService.findCategoriesInRegion("test");

    }

    private void internalCall() {
        //library call, which only has influence in building of SEFFs not by testing the FunctionCallClassification Visitor
        this.inStream.available();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelReservedItem(final Item item, final int numberOfItems) throws InventoryServiceException {

    }

}
