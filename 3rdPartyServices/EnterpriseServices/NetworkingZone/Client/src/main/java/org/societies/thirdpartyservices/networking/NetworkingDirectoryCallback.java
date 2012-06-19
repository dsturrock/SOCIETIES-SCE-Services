/**
 * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
 * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
 * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
 * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
 * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
 * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 *    disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.societies.thirdpartyservices.networking;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.comm.xmpp.interfaces.ICommCallback;
import org.societies.api.comm.xmpp.datatypes.Stanza;
import org.societies.api.comm.xmpp.exceptions.XMPPError;
import org.societies.api.comm.xmpp.datatypes.XMPPInfo;

import org.societies.thirdpartyservices.api.internal.networking.INetworkingDirectoryCallback;
import org.societies.thirdpartyservices.schema.networking.NetworkingBeanResult;



/**
 * Describe your class here...
 *
 * @author aleckey
 *
 */
public class NetworkingDirectoryCallback implements ICommCallback {

	private static final List<String> NAMESPACES = Collections.unmodifiableList(
			  Arrays.asList("http://societies.org/thirdpartyservices/schema/networking"));
	private static final List<String> PACKAGES = Collections.unmodifiableList(
			  Arrays.asList("org.societies.thirdpartyservices.schema.networking"));
	
	
	private static Logger logger = LoggerFactory.getLogger(NetworkingDirectoryCallback.class);
	
	//MAP TO STORE THE ALL THE CLIENT CONNECTIONS
	private static final Map<String, INetworkingDirectoryCallback> networkingDirectoryClients = new HashMap<String, INetworkingDirectoryCallback>();
	
	/** Constructor for callback
	 * @param clientID unique ID of send request to comms framework
	 * @param serviceDiscoveryClient callback from originating client
	 */
	public NetworkingDirectoryCallback(String clientID, INetworkingDirectoryCallback networkingDirectoryClient) {
		//STORE THIS CALLBACK WITH THIS REQUEST ID
		networkingDirectoryClients.put(clientID, networkingDirectoryClient);
	}
	
	
	/**Returns the correct calculator client callback for this request 
	 * @param requestID the id of the initiating request
	 * @return
	 * @throws UnavailableException
	 */
	private INetworkingDirectoryCallback getRequestingClient(String requestID) {
		INetworkingDirectoryCallback requestingClient = (INetworkingDirectoryCallback) networkingDirectoryClients.get(requestID);
		networkingDirectoryClients.remove(requestID);
		return requestingClient;
	}

	/**Returns the correct calculator client callback for this request 
	 * @param requestID the id of the initiating request
	 * @return
	 * @throws UnavailableException
	 */
	private INetworkingDirectoryCallback getRequestingControlClient(String requestID) {
		INetworkingDirectoryCallback requestingClient = (INetworkingDirectoryCallback) networkingDirectoryClients.get(requestID);
		networkingDirectoryClients.remove(requestID);
		return requestingClient;
	}
	
	/* (non-Javadoc)
	 * @see org.societies.comm.xmpp.interfaces.CommCallback#receiveResult(org.societies.comm.xmpp.datatypes.Stanza, java.lang.Object) */
	@Override
	public void receiveResult(Stanza returnStanza, Object msgBean) {
		
		if(logger.isDebugEnabled()) logger.debug("Networking Callback called!");
		
		//CHECK WHICH END SERVICE IS SENDING US A MESSAGE
		
		if (msgBean.getClass().equals(NetworkingBeanResult.class)) {
			
			if(logger.isDebugEnabled()) logger.debug("NetworkingBeanResult!");
			
			
			NetworkingBeanResult networkingDirectoryResult = (NetworkingBeanResult) msgBean;
			
			INetworkingDirectoryCallback networkingDirectoryClient = getRequestingClient(returnStanza.getId());
			
			//TODO : Check where this goes
			networkingDirectoryClient.getResult(networkingDirectoryResult.getResultUserRec());	
			
		}
		
		
	
	}

	/* (non-Javadoc)
	 * @see org.societies.comm.xmpp.interfaces.CommCallback#getJavaPackages() */
	@Override
	public List<String> getJavaPackages() {
		return PACKAGES;
	}

	/* (non-Javadoc)
	 * @see org.societies.comm.xmpp.interfaces.CommCallback#getXMLNamespaces() */
	@Override
	public List<String> getXMLNamespaces() {
		return NAMESPACES;
	}

	/* (non-Javadoc)
	 * @see org.societies.comm.xmpp.interfaces.CommCallback#receiveError(org.societies.comm.xmpp.datatypes.Stanza, org.societies.comm.xmpp.datatypes.XMPPError)
	 */
	@Override
	public void receiveError(Stanza returnStanza, XMPPError info) {
		if(logger.isDebugEnabled()) logger.debug("received an Error!");
		logger.error(info.getMessage());
		
	}

	/* (non-Javadoc)
	 * @see org.societies.comm.xmpp.interfaces.CommCallback#receiveInfo(org.societies.comm.xmpp.datatypes.Stanza, java.lang.String, org.societies.comm.xmpp.datatypes.XMPPInfo)
	 */
	@Override
	public void receiveInfo(Stanza returnStanza, String node, XMPPInfo info) {
		System.out.println(info.getIdentityName());
		
	}

	/* (non-Javadoc)
	 * @see org.societies.comm.xmpp.interfaces.CommCallback#receiveMessage(org.societies.comm.xmpp.datatypes.Stanza, java.lang.Object)
	 */
	@Override
	public void receiveMessage(Stanza returnStanza, Object messageBean) {
		System.out.println(messageBean.getClass().toString());		
	}

	/* (non-Javadoc)
	 * @see org.societies.api.comm.xmpp.interfaces.ICommCallback#receiveItems(org.societies.api.comm.xmpp.datatypes.Stanza, java.lang.String, java.util.List)
	 */
	@Override
	public void receiveItems(Stanza arg0, String arg1, List<String> arg2) {
		// TODO Auto-generated method stub	
	}


}
