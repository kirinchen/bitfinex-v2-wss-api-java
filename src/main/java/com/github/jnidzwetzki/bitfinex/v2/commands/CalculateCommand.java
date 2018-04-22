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
package com.github.jnidzwetzki.bitfinex.v2.commands;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexApiBroker;

public class CalculateCommand extends AbstractAPICommand {
	
	/**
	 * The symbol
	 */
	private String symbol;

	public CalculateCommand(final String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String getCommand(final BitfinexApiBroker bitfinexApiBroker) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[0,\"calc\",null,[[\"");
		sb.append(symbol);
		sb.append("\"]]]");
		
		return sb.toString();
	}
}
