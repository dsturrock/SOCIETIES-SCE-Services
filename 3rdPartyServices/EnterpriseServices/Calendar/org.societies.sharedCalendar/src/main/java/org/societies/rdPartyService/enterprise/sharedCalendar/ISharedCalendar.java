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
package org.societies.rdPartyService.enterprise.sharedCalendar;

import java.util.List;

import org.societies.api.ext3p.schema.sharedcalendar.Calendar;
import org.societies.api.ext3p.schema.sharedcalendar.Event;



/**
 * This is the exposed interface that client developers will use.
 *
 * @author solutanet
 *
 */
public interface ISharedCalendar {
	
	//The following are the verbs for the Activity Stream Entries
	
	public static final String VERB_CIS_CALENDAR_CREATED = "CIS Calendar Created";
	public static final String VERB_CIS_CALENDAR_DELETED = "CIS Calendar Deleted";
	public static final String VERB_CIS_CALENDAR_EVENT_CREATED = "CIS Calendar Event Created";
	public static final String VERB_CIS_CALENDAR_EVENT_DELETED = "CIS Calendar Deleted";
	public static final String VERB_CSS_CALENDAR_CREATED = "CSS Calendar Created";
	public static final String VERB_CSS_CALENDAR_DELETED = "CSS Calendar Deleted";
	public static final String VERB_CSS_CALENDAR_EVENT_CREATED = "CSS Calendar Event Created";
	public static final String VERB_CSS_CALENDAR_EVENT_DELETED = "CSS Calendar Event Deleted";
	public static final String VERB_CALENDAR_EVENT_SUBSCRIPTION = "Calendar Event Subscription";
	public static final String VERB_CALENDAR_EVENT_UNSUBSCRIPTION = "Calendar Event Unsubscription";
	
	/**
	 * This method create a private calendar for a CIS
	 * @param CISId
	 * @param calendarSummary
	 * @return
	 */
	public boolean createCISCalendar(String calendarSummary, String CISId);
	
	/**
	 * This method deletes the CIS calendar with the provided CalendarId
	 * @param CalendarId
	 * @return true if successful
	 */
	public boolean deleteCISCalendar(String calendarId);
	
	/**
	 * This method is used to retrieve all available calendars provided by the 3rd party service
	 * @Param CISId
	 * @return the list of Calendars
	 */

	public List<Calendar> retrieveCISCalendarList(String CISId);
	
	/**
	 * 
	 * @param calendarId
	 * @return the list of events inside a CIS Calendar
	 */
	public List<Event> retrieveCISCalendarEvents(String calendarId);
	
	/**
	 * This method create an Event on CIS Calendar
	 * @param newEvent
	 * @param calendarId
	 * @return the id of the event
	 */
	public String createEventOnCISCalendar(Event newEvent, String calendarId);
	
	/**
	 * 
	 * @param eventId
	 * @param calendarId
	 * @return true if the event is deleted successfully
	 */
	public boolean deleteEventOnCISCalendar(String eventId, String calendarId);
	
	/**
	 * 
	 * @param calendarId
	 * @param eventId
	 * @param subscriberId the identifier for the subscriber
	 * @return true if the subscription is performed correctly false otherwise
	 */
	public boolean subscribeToEvent(String calendarId,String eventId, String subscriberId);
	
	/**
	 * 
	 * @param calendarId
	 * @param keyWord
	 * @return the list of events that match the input keyword
	 */
	public List<Event> findEvents(String calendarId,String keyWord);
	
	/**
	 * 
	 * @param calendarId
	 * @param eventId
	 * @param subscriberId
	 * @return true if the unsubscription is performed correctly, false otherwise
	 */
	public boolean unsubscribeFromEvent(String calendarId,String eventId, String subscriberId);
	
	/**
	 * This method create a private calendar for a CSS
	 * @param calendarSummary
	 * @return
	 */
	public boolean createPrivateCalendar(String calendarSummary);
	
	/**
	 * This method removes a private calendar for a CSS
	 * @return
	 */
	public boolean deletePrivateCalendar();
	
	
	/**
	 * this method create an event inside a private calendar using as identifier the CSS Jid
	 * @param eventTitle
	 * @param description
	 * @param startDate
	 * @param endDate
	 * @param attendeeName
	 * @param attendeeEmail
	 * @return
	 */
	public String createEventOnPrivateCalendar(Event newEvent);
	
	/**
	 * This method is used to retrieve all events in a CSS calendar.
	 * The calendar id is not used because it is retrieved using the Jid in the XMPP message sent to the server. 
	 * @return
	 */
	public List<Event> retrieveEventsOnPrivateCalendar();
	
	
	/**
	 * This method is used to delete an event on a CSS calendar.
	 * @param eventId
	 * @return true if the event is deleted successfully.
	 */
	public boolean deleteEventOnPrivateCalendar(String eventId);
	
	
}
