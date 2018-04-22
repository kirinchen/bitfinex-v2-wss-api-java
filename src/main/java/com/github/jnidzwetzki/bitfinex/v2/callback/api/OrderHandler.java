/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *  
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License. 
 *    
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.callback.api;

import java.util.concurrent.CountDownLatch;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;
import com.github.jnidzwetzki.bitfinex.v2.entity.APIException;
import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexOrderType;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrder;
import com.github.jnidzwetzki.bitfinex.v2.entity.ExchangeOrderState;

public class OrderHandler implements APICallbackHandler {
	
	/**
	 * The Logger
	 */
	final static Logger logger = LoggerFactory.getLogger(OrderHandler.class);

	@Override
	public void handleChannelData(final BitfinexApiBroker bitfinexApiBroker, final JSONArray jsonArray) 
			throws APIException {

		logger.info("Got order callback {}", jsonArray.toString());
		
		final JSONArray orders = jsonArray.optJSONArray(2);
		
		// No orders active
		if(orders.length() == 0) {
			notifyOrderLatch(bitfinexApiBroker);
			return;
		}
		
		// Snapshot or update
		if(! (orders.opt(0) instanceof JSONArray)) {
			handleOrderCallback(bitfinexApiBroker, orders);
		} else {
			for(int orderPos = 0; orderPos < orders.length(); orderPos++) {
				final JSONArray orderArray = orders.optJSONArray(orderPos);
				handleOrderCallback(bitfinexApiBroker, orderArray);
			}
			
			notifyOrderLatch(bitfinexApiBroker);
		}
	}

	/**
	 * Notify the order latch
	 * @param bitfinexApiBroker
	 */
	private void notifyOrderLatch(final BitfinexApiBroker bitfinexApiBroker) {
		
		// All snapshots are completed
		final CountDownLatch connectionReadyLatch = bitfinexApiBroker.getConnectionReadyLatch();
		
		if(connectionReadyLatch != null) {
			connectionReadyLatch.countDown();
		}
	}

	/**
	 * Handle a single order callback
	 * @param bitfinexApiBroker 
	 * @param orderArray
	 * @throws APIException 
	 */
	private void handleOrderCallback(BitfinexApiBroker bitfinexApiBroker, final JSONArray order) throws APIException {		
		final ExchangeOrder exchangeOrder = new ExchangeOrder();
		exchangeOrder.setApikey(bitfinexApiBroker.getApiKey());
		exchangeOrder.setOrderId(order.optLong(0));
		exchangeOrder.setGroupId(order.optInt(1, -1));
		exchangeOrder.setCid(order.optLong(2, -1));
		exchangeOrder.setSymbol(order.optString(3));
		exchangeOrder.setCreated(order.optLong(4));
		exchangeOrder.setUpdated(order.optLong(5));
		exchangeOrder.setAmount(order.optDouble(6));
		exchangeOrder.setAmountAtCreation(order.optDouble(7));
		exchangeOrder.setOrderType(BitfinexOrderType.fromString(order.optString(8)));
		
		final ExchangeOrderState orderState = ExchangeOrderState.fromString(order.optString(13));
		exchangeOrder.setState(orderState);
		
		exchangeOrder.setPrice(order.optDouble(16));
		exchangeOrder.setPriceAvg(order.optDouble(17, -1));
		exchangeOrder.setPriceTrailing(order.optDouble(18, -1));
		exchangeOrder.setPriceAuxLimit(order.optDouble(19, -1));
		exchangeOrder.setNotify(order.optInt(23) == 1 ? true : false);
		exchangeOrder.setHidden(order.optInt(24) == 1 ? true : false);

		bitfinexApiBroker.getOrderManager().updateOrder(exchangeOrder);
	}
}
