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

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.societies.api.activity.IActivity;
import org.societies.api.activity.IActivityFeed;
import org.societies.api.activity.IActivityFeedCallback;
import org.societies.api.cis.management.ICisManager;
import org.societies.api.cis.management.ICisOwned;
import org.societies.api.css.devicemgmt.IDevice;
import org.societies.api.ext3p.schema.sharedcalendar.Calendar;
import org.societies.api.ext3p.schema.sharedcalendar.Event;
import org.societies.api.osgi.event.IEventMgr;
import org.societies.api.schema.activityfeed.Activityfeed;
import org.societies.api.services.IServices;
import org.societies.rdPartyService.enterprise.sharedCalendar.persistence.DAO.CISCalendarDAO;
import org.societies.rdPartyService.enterprise.sharedCalendar.persistence.DAO.CSSCalendarDAO;
import org.societies.rdPartyService.enterprise.sharedCalendar.privateCalendarUtil.IPrivateCalendarUtil;

import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.EventAttendee;

/**
 * Back-end class that implements the calendar management logic.
 * 
 * @author solutanet
 * 
 */
public class SharedCalendar implements ISharedCalendar, IPrivateCalendarUtil {
	private SharedCalendarUtil util;
	private SessionFactory sessionFactory;
	private static Logger log = LoggerFactory.getLogger(SharedCalendar.class);
	private IEventMgr evtMgr;
	/**
	 * This is the set of all available instances of the IDevice interface.
	 * A DeviceListener bean instance tracks whenever a new IDevice is bound or unbound.
	 * See http://static.springsource.org/osgi/docs/1.2.1/reference/html-single/#service-registry:refs:collection:dynamics
	 */
	private Set<IDevice> availableDevices;
	private IServices serviceMetadataUtil;
	private ICisManager cisManager;
	//private IActivityFeed activityFeed;
	
	/*Start constants definition*/
	String CIS_MANAGER_CSS_ID ;
	final String TEST_CSS_PWD="TEST_CSS_PWD";
	final String TEST_CIS_NAME_1="CALENDAR CIS";
	final String TEST_CIS_TYPW ="TEST_CIS_TYPW"; 
	final int TEST_CIS_MODE=1;
	String CIS_TEST_ID;
	
	/*Init method used for test purpose only*/
	public void init() throws Exception {
		log.debug("Init method.");
//		ServiceResourceIdentifier tmpServiceIdentifier=serviceMetadataUtil.getMyServiceId(this.getClass());
//		CIS_MANAGER_CSS_ID=serviceMetadataUtil.getServer(tmpServiceIdentifier).getJid();
//		Future<ICisOwned> testCIS = cisManager.createCis(CIS_MANAGER_CSS_ID, TEST_CSS_PWD,
//                TEST_CIS_NAME_1, TEST_CIS_TYPW , TEST_CIS_MODE);
//		log.info("TEST CIS CREATED WITH ID: "+testCIS.get().getCisId());
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SharedCalendarUtil getUtil() {
		return util;
	}

	public void setUtil(SharedCalendarUtil util) {
		this.util = util;
	}
	
	private void notifyCisCalendarCreation(String calendarSummary, String CISId){
		if (cisManager != null) {
			ICisOwned iCis = cisManager.getOwnedCis(CISId);
			if (iCis != null) {
				IActivityFeed activityFeed = iCis.getActivityFeed();
				if (activityFeed!=null){
					IActivity notifyActivity = activityFeed.getEmptyIActivity();
					// notifyActivity.setId(new Long(1));
					notifyActivity.setActor(CISId);
					notifyActivity.setVerb(ISharedCalendar.VERB_CIS_CALENDAR_CREATED);
					notifyActivity.setObject(calendarSummary);
					// IActivityFeed activityFeed=iCis.getActivityFeed();
					activityFeed.addActivity(notifyActivity,
							new IActivityFeedCallback() {
								@Override
								public void receiveResult(
										Activityfeed activityFeedObject) {
									log.debug("Added a 'Create CIS Calendar' activity to the Activity Feed.");
								}
							}
					);
				}
			}
		} else {
			log.debug("CIS manager or ActivityFeed service not available.");
		}
	}

	/* (non-Javadoc)
	 * @see org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar#createCISCalendar(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean createCISCalendar(String calendarSummary, String CISId) {
		
		//Test purpose only
		log.info("SERVICE INSTANCE IDENTIFIER: "+serviceMetadataUtil.getMyServiceId(this.getClass()).getServiceInstanceIdentifier());
		String storedCalendarId = null;
		Transaction t = null;
		Session session = null;
		boolean result = false;

		try {
			session = sessionFactory.openSession();
			
				storedCalendarId = util.createCalendar(calendarSummary);
				CISCalendarDAO cisCalendarDAO = new CISCalendarDAO(CISId,
						storedCalendarId);

				t = session.beginTransaction();
				session.save(cisCalendarDAO);
				t.commit();
				result = true;
				
			/*Notify to CIS that a new calendar is created*/				
			this.notifyCisCalendarCreation(calendarSummary, CISId);
		} catch (HibernateException he) {
			log.error(he.getMessage());
			if (t != null) {
				t.rollback();
				try {
					util.deleteCalendar(storedCalendarId);
				} catch (IOException e) {

					log.error(e.getMessage());
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {

			if (session != null) {
				session.close();
			}

		}
		return result;
	}
	
	private void notifyCisCalendarDeletion(String calendarId, String CISId){
		if (cisManager != null) {
			ICisOwned iCis = cisManager.getOwnedCis(CISId);
			if (iCis != null) {
				IActivityFeed activityFeed = iCis.getActivityFeed();
				if (activityFeed!=null){
					IActivity notifyActivity = activityFeed.getEmptyIActivity();
					notifyActivity.setActor(CISId);
					notifyActivity.setVerb(ISharedCalendar.VERB_CIS_CALENDAR_DELETED);
					notifyActivity.setObject(calendarId);
					activityFeed.addActivity(notifyActivity,
							new IActivityFeedCallback() {
								@Override
								public void receiveResult(
										Activityfeed activityFeedObject) {
									log.debug("Added a 'Delete CIS Calendar' activity to the Activity Feed.");
								}
							}
					);
				}
			}
		} else {
			log.debug("CIS manager or ActivityFeed service not available.");
		}
	}
	
	@Override
	public boolean deleteCISCalendar(String calendarId) {
		
			
			Transaction t = null;
			Session session = null;
			boolean result = false;

			try {
				session = sessionFactory.openSession();
				CISCalendarDAO template = new CISCalendarDAO();
				template.setCalendarId(calendarId);
				List<CISCalendarDAO> results = session
						.createCriteria(CISCalendarDAO.class)
						.add(Example.create(template)).list();
				if (results.size() == 1) {					
					t = session.beginTransaction();
					CISCalendarDAO d = results.get(0);
					session.delete(d);
					util.deleteCalendar(calendarId);
					t.commit();										
					result = true;		
					this.notifyCisCalendarDeletion(calendarId, d.getCISId());
				} else {
					log.info("The CIS has not been found.");
				}
			} catch (HibernateException he) {
				log.error(he.getMessage());
				if (t != null) {
					t.rollback();
				}
			} catch (IOException e) {
				log.error(e.getMessage());
				if (t != null) {
					t.rollback();
				}
			} finally {
				if (session != null) {
					session.close();
				}
			}
			
			return result;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #retrieveCalendarList()
	 */
	@Override
	public List<Calendar> retrieveCISCalendarList(String CISId) {
		List<Calendar> returnedCalendarList = new ArrayList<Calendar>();
		try {
			List<CalendarListEntry> tmpCalendarList=util.retrieveAllCISCalendar(CISId);			
			returnedCalendarList = calendarListFromCalendarEntry(filterCISCalendar(tmpCalendarList, CISId));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return returnedCalendarList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #retrieveCalendarEvents(java.lang.String)
	 */
	@Override
	public List<Event> retrieveCISCalendarEvents(String calendarId) {
		List<Event> returnedEventList = new ArrayList<Event>();
		try {
			returnedEventList = eventListFromGoogleEventList(util.retrieveAllEvents(calendarId));			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return returnedEventList;
	}
	
	private void notifyCisCalendarEvtCreation(String newEvtId, String calendarId, String CISId){
		if (CISId==null){
			log.debug("CIS ID parameter not provided, skipping activity creation.");
			return;
		}
		if (cisManager != null) {
			ICisOwned iCis = cisManager.getOwnedCis(CISId);
			if (iCis != null) {
				IActivityFeed activityFeed = iCis.getActivityFeed();
				if (activityFeed!=null){
					IActivity notifyActivity = activityFeed.getEmptyIActivity();
					notifyActivity.setActor(CISId);
					notifyActivity.setVerb(ISharedCalendar.VERB_CIS_CALENDAR_EVENT_CREATED);
					notifyActivity.setObject(newEvtId);
					notifyActivity.setTarget(calendarId);
					activityFeed.addActivity(notifyActivity,
							new IActivityFeedCallback() {
								@Override
								public void receiveResult(
										Activityfeed activityFeedObject) {
									log.debug("Added a 'Create CIS Event' activity to the Activity Feed.");
								}
							}
					);
				}
			}
		} else {
			log.debug("CIS manager or ActivityFeed service not available.");
		}
	}

	/* (non-Javadoc)
	 * @see org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar#createEventOnCISCalendar(org.societies.rdpartyservice.enterprise.sharedcalendar.Event, java.lang.String)
	 */
	@Override
	public String createEventOnCISCalendar(Event newEvent, String calendarId) {
		String returnedEventId = "";
		try {
			returnedEventId = util.createEvent(calendarId, newEvent
					.getEventSummary(), newEvent.getEventDescription(),
					XMLGregorianCalendarConverter.asDate(newEvent
							.getStartDate()), XMLGregorianCalendarConverter
							.asDate(newEvent.getEndDate()), newEvent
							.getLocation());
			this.notifyCisCalendarEvtCreation(returnedEventId, calendarId, this.getCisIdFromCalendarId(calendarId));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return returnedEventId;
	}
	
	
	/**
	 * Returns the CisId of the CIS that owns the calendar with the provided id, or null if not found
	 * @param calendarId the CIS Calendar Id to search for
	 * @return The CisId (or null if not found)
	 */
	private String getCisIdFromCalendarId(String calendarId){
		String result = null;
		Transaction t = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			CISCalendarDAO template = new CISCalendarDAO();
			template.setCalendarId(calendarId);
			List<CISCalendarDAO> results = session
					.createCriteria(CISCalendarDAO.class)
					.add(Example.create(template)).list();
			if (results.size() == 1) {					
				t = session.beginTransaction();
				CISCalendarDAO d = results.get(0);
				result = d.getCISId();
				t.commit();
			} else {
				log.info("The CIS has not been found.");
			}
		} catch (HibernateException he) {
			log.error(he.getMessage());
			if (t != null) {
				t.rollback();
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}	
		return result;
	}
	
	/**
	 * Returns the CssId of the Css that owns the calendar with the provided id, or null if not found
	 * @param calendarId the Css Calendar Id to search for
	 * @return The CssId (or null if not found)
	 */
	private String getCssIdFromCalendarId(String calendarId){
		String result = null;
		Transaction t = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			CSSCalendarDAO template = new CSSCalendarDAO();
			template.setCalendarId(calendarId);
			List<CSSCalendarDAO> results = session
					.createCriteria(CSSCalendarDAO.class)
					.add(Example.create(template)).list();
			if (results.size() == 1) {					
				t = session.beginTransaction();
				CSSCalendarDAO d = results.get(0);
				result = d.getCSSId();
				t.commit();
			} else {
				log.info("The CSS has not been found.");
			}
		} catch (HibernateException he) {
			log.error(he.getMessage());
			if (t != null) {
				t.rollback();
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}	
		return result;
	}
	
	private void notifyCisCalendarEvtDeletion(String deletedEvtId, String calendarId, String CISId){
		if (CISId==null){
			log.debug("CIS ID parameter not provided, skipping activity creation.");
			return;
		}
		if (cisManager != null) {
			ICisOwned iCis = cisManager.getOwnedCis(CISId);
			if (iCis != null) {
				IActivityFeed activityFeed = iCis.getActivityFeed();
				if (activityFeed!=null){
					IActivity notifyActivity = activityFeed.getEmptyIActivity();
					notifyActivity.setActor(CISId);
					notifyActivity.setVerb(ISharedCalendar.VERB_CIS_CALENDAR_EVENT_DELETED);
					notifyActivity.setObject(deletedEvtId);
					notifyActivity.setTarget(calendarId);
					activityFeed.addActivity(notifyActivity,
							new IActivityFeedCallback() {
								@Override
								public void receiveResult(
										Activityfeed activityFeedObject) {
									log.debug("Added a 'Delete CIS Event' activity to the Activity Feed.");
								}
							}
					);
				}
			}
		} else {
			log.debug("CIS manager or ActivityFeed service not available.");
		}
	}
	
//	private void notifyCssCalendarEvtDeletion(String deletedEvtId, String calendarId, String CISId){
//		if (CISId==null){
//			log.debug("CIS ID parameter not provided, skipping activity creation.");
//			return;
//		}
//		if (cisManager != null) {
//			ICisOwned iCis = cisManager.getOwnedCis(CISId);
//			if (iCis != null) {
//				IActivityFeed activityFeed = iCis.getActivityFeed();
//				if (activityFeed!=null){
//					IActivity notifyActivity = activityFeed.getEmptyIActivity();
//					notifyActivity.setActor(CISId);
//					notifyActivity.setVerb(ISharedCalendar.VERB_CSS_CALENDAR_EVENT_DELETED);
//					notifyActivity.setObject(deletedEvtId);
//					notifyActivity.setTarget(calendarId);
//					activityFeed.addActivity(notifyActivity,
//							new IActivityFeedCallback() {
//								@Override
//								public void receiveResult(
//										Activityfeed activityFeedObject) {
//									log.debug("Added a 'Delete CSS Event' activity to the Activity Feed.");
//								}
//							}
//					);
//				}
//			}
//		} else {
//			log.debug("CIS manager or ActivityFeed service not available.");
//		}
//	}
	
	/* (non-Javadoc)
	 * @see org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar#deleteEventOnCISCalendar(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean deleteEventOnCISCalendar(String eventId, String calendarId) {
		boolean deletionOk=false;
		try {
			util.deleteEvent(calendarId, eventId);
			this.notifyCisCalendarEvtDeletion(eventId, calendarId, this.getCisIdFromCalendarId(calendarId));
			deletionOk=true;
		} catch (Exception e) {
			log.error("Error during deletion of CIS calendar event: "+e.getMessage());
		}
		return deletionOk;
	}
	
	public boolean deleteEventOnCSSCalendar(String eventId, String calendarId) {
		boolean deletionOk=false;
		try {
			util.deleteEvent(calendarId, eventId);
//			this.notifyCssCalendarEvtDeletion(eventId, calendarId, this.getCssIdFromCalendarId(calendarId));
			deletionOk=true;
		} catch (Exception e) {
			log.error("Error during deletion of CSS calendar event: "+e.getMessage());
		}
		return deletionOk;
	}
	
	private void notifyCalendarEvtSubscription(String subscribedEvtId, String calendarId, String CISId){
		if (CISId==null){
			log.debug("CIS ID parameter not provided, skipping activity creation.");
			return;
		}
		if (cisManager != null) {
			ICisOwned iCis = cisManager.getOwnedCis(CISId);
			if (iCis != null) {
				IActivityFeed activityFeed = iCis.getActivityFeed();
				if (activityFeed!=null){
					IActivity notifyActivity = activityFeed.getEmptyIActivity();
					notifyActivity.setActor(CISId);
					notifyActivity.setVerb(ISharedCalendar.VERB_CALENDAR_EVENT_SUBSCRIPTION);
					notifyActivity.setObject(subscribedEvtId);
					notifyActivity.setTarget(calendarId);
					activityFeed.addActivity(notifyActivity,
							new IActivityFeedCallback() {
								@Override
								public void receiveResult(
										Activityfeed activityFeedObject) {
									log.debug("Added a 'Subscription to Calendar Event' activity to the Activity Feed.");
								}
							}
					);
				}
			}
		} else {
			log.debug("CIS manager or ActivityFeed service not available.");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #subscribeToEvent(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean subscribeToEvent(String calendarId, String eventId,
			String subscriberId) {
		boolean returnedValue = false;
		try {
			com.google.api.services.calendar.model.Event event = util
					.findEventUsingId(calendarId, eventId);
			/*
			 * Check if event has attendees
			 */
			if (event.getAttendees() != null) {
				event.getAttendees().add(createEventAttendee(subscriberId));
			} else {
				List<EventAttendee> attendeesList = new ArrayList<EventAttendee>();
				attendeesList.add(createEventAttendee(subscriberId));
				event.setAttendees(attendeesList);
			}

			util.updateEvent(calendarId, event);
			this.notifyCalendarEvtSubscription(eventId, calendarId, this.getCisIdFromCalendarId(calendarId));
			returnedValue = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		return returnedValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #findEvents(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Event> findEvents(String calendarId, String keyWord) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void notifyCalendarEvtUnsubscription(String subscribedEvtId, String calendarId, String CISId){
		if (CISId==null){
			log.debug("CIS ID parameter not provided, skipping activity creation.");
			return;
		}
		if (cisManager != null) {
			ICisOwned iCis = cisManager.getOwnedCis(CISId);
			if (iCis != null) {
				IActivityFeed activityFeed = iCis.getActivityFeed();
				if (activityFeed!=null){
					IActivity notifyActivity = activityFeed.getEmptyIActivity();
					notifyActivity.setActor(CISId);
					notifyActivity.setVerb(ISharedCalendar.VERB_CALENDAR_EVENT_UNSUBSCRIPTION);
					notifyActivity.setObject(subscribedEvtId);
					notifyActivity.setTarget(calendarId);
					activityFeed.addActivity(notifyActivity,
							new IActivityFeedCallback() {
								@Override
								public void receiveResult(
										Activityfeed activityFeedObject) {
									log.debug("Added a 'Unsubscription from Calendar Event' activity to the Activity Feed.");
								}
							}
					);
				}
			}
		} else {
			log.debug("CIS manager or ActivityFeed service not available.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #unsubscribeFromEvent(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean unsubscribeFromEvent(String calendarId, String eventId,
			String subscriberId) {
		boolean returnedValue = false;
		try {
			com.google.api.services.calendar.model.Event event = util
					.findEventUsingId(calendarId, eventId);

			List<EventAttendee> tmpAttendeeList = event.getAttendees();
			EventAttendee attendeeToRemove = null;
			
			for (EventAttendee ea : tmpAttendeeList) {
				if (subscriberId.equalsIgnoreCase(ea.getDisplayName())) {
					attendeeToRemove = ea;
					break;
				}
			}
			
			boolean foundAndRemoved = attendeeToRemove != null && tmpAttendeeList.remove(attendeeToRemove);
			event.setAttendees(tmpAttendeeList);
			util.updateEvent(calendarId, event);
			this.notifyCalendarEvtUnsubscription(eventId, calendarId, this.getCisIdFromCalendarId(calendarId));
			returnedValue = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		return returnedValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #createPrivateCalendar(java.lang.String) THIS METHOD HAS NO
	 * IMPLEMENTATION. USE THE createPrivateCalendarUsingCSSId TO STORE THE
	 * MAPPING BETWEEN CSSID AND CALENDARID
	 */
	@Override
	public boolean createPrivateCalendar(String calendarSummary) {

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.privateCalendarUtil
	 * .IPrivateCalendarUtil#createPrivateCalendarUsingCSSId(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean createPrivateCalendarUsingCSSId(String CSSId,
			String calendarSummary) {
		String storedCalendarId = null;
		Transaction t = null;
		Session session = null;
		boolean result = false;

		try {
			session = sessionFactory.openSession();
			CSSCalendarDAO template = new CSSCalendarDAO();
			template.setCSSId(CSSId);
			List<CSSCalendarDAO> results = session
					.createCriteria(CSSCalendarDAO.class)
					.add(Example.create(template)).list();
			if (results.size() == 0) {
				storedCalendarId = util.createCalendar(calendarSummary);
				CSSCalendarDAO cssCalendarDAO = new CSSCalendarDAO(CSSId, storedCalendarId);
				t = session.beginTransaction();
				session.save(cssCalendarDAO);
				t.commit();
				result = true;
			} else {
				log.info("The CSS already has a calendar.");
				result = true;
			}
		} catch (HibernateException he) {
			log.error(he.getMessage());
			if (t != null) {
				t.rollback();
				try {
					util.deleteCalendar(storedCalendarId);
				} catch (IOException e) {

					log.error(e.getMessage());
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {

			if (session != null) {
				session.close();
			}

		}
		return result;
	}
	
	/* (non-Javadoc)
	 * DO NOT USE THIS METHOD BUT USE THE deletePrivateCalendar PASSING THE CALENDAR ID RETRIEVED USING THE CSSID 
	 */
	@Override	
	public boolean deletePrivateCalendar() {
		// TODO Auto-generated method stub
		return false;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.privateCalendarUtil
	 * .IPrivateCalendarUtil#deletePrivateCalendarUsingCSSId(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean deletePrivateCalendarUsingCSSId(String CSSId) {
		boolean result = false;
		String storedCalendarId = null;
		Session session = null;
		Transaction t = null;
		try {
			session = sessionFactory.openSession();
			CSSCalendarDAO template = new CSSCalendarDAO();
			template.setCSSId(CSSId);
			List<CSSCalendarDAO> results = session
					.createCriteria(CSSCalendarDAO.class)
					.add(Example.create(template)).list();
			if (results.size() != 0) {
				CSSCalendarDAO tmpCSSCalendarDAO = (CSSCalendarDAO) session
						.get(CSSCalendarDAO.class, results.get(0).getId());
				util.deleteCalendar(results.get(0).getCalendarId());
				t = session.beginTransaction();

				session.delete(tmpCSSCalendarDAO);
				t.commit();
			}
		} catch (HibernateException he) {
			if (t != null) {
				t.rollback();
			}
			log.error(he.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar
	 * #createEventOnPrivateCalendar(java.lang.String, java.lang.String,
	 * java.util.Date, java.util.Date, java.lang.String, java.lang.String)
	 * DO NOT USE THIS IMPLEMENTATION. USE THE createEventOnPrivateCalendarUsingCSSId TO STORE
	 * THE MAPPING BETWEEN CSSID AND CALENDARID
	 */
	@Override
	public String createEventOnPrivateCalendar(Event newEvent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.societies.rdPartyService.enterprise.sharedCalendar.privateCalendarUtil
	 * .
	 * IPrivateCalendarUtil#createEventOnPrivateCalendarUsingCSSId(java.lang.String
	 * , java.lang.String, java.lang.String, java.util.Date, java.util.Date,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public String createEventOnPrivateCalendarUsingCSSId(String calendarId,
			Event newEvent) {
		String returnedEventId = "";
		try {
			returnedEventId = util.createEvent(calendarId, newEvent
					.getEventSummary(), newEvent.getEventDescription(),
					XMLGregorianCalendarConverter.asDate(newEvent
							.getStartDate()), XMLGregorianCalendarConverter
							.asDate(newEvent.getEndDate()), newEvent
							.getLocation());

		} catch (IOException e) {

			log.error(e.getMessage());
		}
		return returnedEventId;
	}
	
	/* (non-Javadoc)
	 * @see org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar#retrieveEventsPrivateCalendar()
	 * DO NOT USE THIS METHOD BUT USE THE retrieveCISCalendarEvents PASSING THE CALENDAR ID RETRIEVED USING THE CSSID 
	 */
	@Override
	public List<Event> retrieveEventsOnPrivateCalendar() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.societies.rdPartyService.enterprise.sharedCalendar.ISharedCalendar#deleteEventOnPrivateCalendar(java.lang.String)
	 *DO NOT USE THIS METHOD BUT USE THE deleteEventOnCISCalendar(String, String) PASSING THE CALENDAR ID RETRIEVED USING THE CSSID
	 */
	@Override
	public boolean deleteEventOnPrivateCalendar(String eventId) {
		return false;
	}

	/**
	 * Utility methods
	 */

	private List<Calendar> calendarListFromCalendarEntry(
			List<CalendarListEntry> inList) {
		List<Calendar> tmpCalendarList = new ArrayList<Calendar>();
		for (CalendarListEntry calendarListEntry : inList) {
			Calendar tmpCalendar = new Calendar();
			tmpCalendar.setSummary(calendarListEntry.getSummary());
			tmpCalendar.setCalendarId(calendarListEntry.getId());
			tmpCalendar.setDescription(calendarListEntry.getDescription());
			tmpCalendar.setLocation(calendarListEntry.getLocation());
			tmpCalendarList.add(tmpCalendar);
		}
		return tmpCalendarList;
	}

	private List<Event> eventListFromGoogleEventList(
			List<com.google.api.services.calendar.model.Event> inList) {
		List<Event> tmpEventList = new ArrayList<Event>();
		for (com.google.api.services.calendar.model.Event event : inList) {
			Event tmpEvent = new Event();
			tmpEvent.setEndDate(XMLGregorianCalendarConverter
					.asXMLGregorianCalendar(new Date(event.getEnd()
							.getDateTime().getValue())));
			tmpEvent.setStartDate(XMLGregorianCalendarConverter
					.asXMLGregorianCalendar(new Date(event.getStart()
							.getDateTime().getValue())));
			tmpEvent.setEventId(event.getId());
			tmpEvent.setEventSummary(event.getSummary());
			tmpEvent.setLocation(event.getLocation());
			tmpEvent.setEventDescription(event.getDescription());
			tmpEventList.add(tmpEvent);
		}
		return tmpEventList;
	}

	/**
	 * Used to map Societies subscriber to google EventAttendee
	 */

	private EventAttendee createEventAttendee(String subscriberId) {
		EventAttendee attendee = new EventAttendee();
		// Create the MD5 to use in the email field
		MessageDigest messageDigest;
		String mailField = "";
		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();
			messageDigest
					.update(subscriberId.getBytes(Charset.forName("UTF8")));
			byte[] resultByte = messageDigest.digest();
			mailField = new String(Hex.encodeHex(resultByte));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		//
		attendee.setEmail(mailField + "@societies.eu");
		attendee.setDisplayName(subscriberId);
		return attendee;
	}

	/**
	 * This method is used to retrieve the id associated by Google to the CSS calendar.
	 * @param CSSId
	 * @return the calendar Id associated to the CSS calendar by Google.
	 */
	public String retrievePrivateCalendarId(String CSSId) {
		String returnedCalendarId = null;
		Session session = sessionFactory.openSession();
		try {
			List<CSSCalendarDAO> returnedCSSCalendarDAO = session
					.createCriteria(CSSCalendarDAO.class)
					.add(Restrictions.eq("CSSId", CSSId)).list();
			if (returnedCSSCalendarDAO.size() != 0) {
				returnedCalendarId = returnedCSSCalendarDAO.get(0).getCalendarId();
			}
		} catch (HibernateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (session!=null){
				session.close();
			}
		}
		return returnedCalendarId;
	}

	/**
	 * This method retrieve from the list of all Calendars only ones that belong to the CIS
	 * @param listToFilter
	 * @param CISId
	 * @return
	 */
private List<CalendarListEntry> filterCISCalendar(List<CalendarListEntry> listToFilter, String CISId){
		Session session=sessionFactory.openSession();
		try{
			List<String> cisCalendarIdList=session.createCriteria(CISCalendarDAO.class).add(Restrictions.eq("CISId", CISId)).setProjection(Projections.property("calendarId")).list();
			Iterator<CalendarListEntry> iterator=listToFilter.iterator();
			while (iterator.hasNext()){
				if (!(cisCalendarIdList.contains(iterator.next().getId()))){
					iterator.remove();
				}
			}
		}catch (Exception e) {
			// TODO: handle exception
		}finally{
			if (session!=null){
				session.close();
			}
		}
		
		return listToFilter;
	}

	public IEventMgr getEvtMgr() {
		return evtMgr;
	}

	public void setEvtMgr(IEventMgr evtMgr) {
		this.evtMgr = evtMgr;
	}

	public Set<IDevice> getAvailableDevices() {
		return availableDevices;
	}

	public IServices getServiceMetadataUtil() {
		return serviceMetadataUtil;
	}

	public void setServiceMetadataUtil(IServices serviceMetadataUtil) {
		this.serviceMetadataUtil = serviceMetadataUtil;
	}

	public void setAvailableDevices(Set<IDevice> availableDevices) {
		this.availableDevices = availableDevices;
	}

	public ICisManager getCisManager() {
		return cisManager;
	}

	public void setCisManager(ICisManager cisManager) {
		this.cisManager = cisManager;
	}

	

	

	
	
	

}
