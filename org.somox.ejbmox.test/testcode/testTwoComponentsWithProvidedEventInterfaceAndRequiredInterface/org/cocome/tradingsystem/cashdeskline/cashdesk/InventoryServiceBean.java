/***************************************************************************
 * Copyright 2013 DFG SPP 1593 (http://dfg-spp1593.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package org.cocome.tradingsystem.cashdeskline.cashdesk;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.cocome.tradingsystem.cashdeskline.events.CashAmountEnteredEvent;
import org.cocome.tradingsystem.cashdeskline.events.CashBoxClosedEvent;
import org.cocome.tradingsystem.cashdeskline.events.CreditCardPinEnteredEvent;
import org.cocome.tradingsystem.cashdeskline.events.CreditCardScannedEvent;
import org.cocome.tradingsystem.cashdeskline.events.ExpressModeDisabledEvent;
import org.cocome.tradingsystem.cashdeskline.events.ExpressModeEnabledEvent;
import org.cocome.tradingsystem.cashdeskline.events.PaymentModeSelectedEvent;
import org.cocome.tradingsystem.cashdeskline.events.ProductBarcodeScannedEvent;
import org.cocome.tradingsystem.cashdeskline.events.SaleFinishedEvent;
import org.cocome.tradingsystem.cashdeskline.events.SaleStartedEvent;
import org.cocome.tradingsystem.cashdeskline.events.ChangeAmountCalculatedEvent;
import org.cocome.tradingsystem.inventory.application.store.ProductOutOfStockException;


/**
 * Implements the business logic of the cash desk. The controller handles events
 * received by the cash desk and translates them to invocations on the cash desk
 * model. The cash desk model is stateful, so most methods can only be called in
 * certain state. If an event is received that results in illegal method
 * invocation on the model, the event is logged and ignored.
 * <p>
 * The state is kept in the model to enforce sequencing, because the model will emit events in response to method invocations. This allows to control the model from
 * outside.
 * 
 * @author Yannick Welsch
 * @author Lubomir Bulej
 * @author Tobias Pöppke
 * @author Robert Heinrich
 */

//@Dependent
@Stateless
class InventoryServiceBean implements InventoryService, Serializable {

	InputStream inStream = new FileInputStream("test");
	
	@Inject
	private Event<ChangeAmountCalculatedEvent> changeAmountCalculatedEvent; 
	
	public void reserveItem(@Observes SaleStartedEvent event) throws IllegalCashDeskStateException {
		// dummy loop is necessray to create a SEFF --> JaMoPPStatementVisitor thinks we do some actual work here
        int j = 0;
        for(int i = 0; i < 10; i++){
            j += i;
        }
        // internal call
        this.internalCall();
        changeAmountCalculatedEvent.fire(new ChangeAmountCalculatedEvent());
        // libary call
        this.inStream.available();
        for(int i = 0; i < 10; i++){
            j += i;
        }
        // internal call containing External call
        this.internalCallContainingExternal();
        // end of method calls
	}
	
	private void internalCallContainingExternal(){
		changeAmountCalculatedEvent.fire(new ChangeAmountCalculatedEvent());
	}
	
	private void internalCall(){

	}
	
	public void onEvent(@Observes SaleFinishedEvent event) throws IllegalCashDeskStateException {}
	
	public void onEvent(@Observes PaymentModeSelectedEvent event) throws IllegalCashDeskStateException {}

	public void onEvent(@Observes ExpressModeDisabledEvent event) {}

	public void onEvent(@Observes ProductBarcodeScannedEvent event) throws IllegalCashDeskStateException, ProductOutOfStockException { 	}
	
	public void onEvent(@Observes CashAmountEnteredEvent event) throws IllegalCashDeskStateException { }
	
	public void onEvent(@Observes CashBoxClosedEvent event) throws IllegalCashDeskStateException {	}

	public void onEvent(@Observes CreditCardScannedEvent event) throws IllegalCashDeskStateException {	}

	public void onEvent(@Observes CreditCardPinEnteredEvent event) throws IllegalCashDeskStateException {}

	public void onEvent(@Observes ExpressModeEnabledEvent event) {}

}
