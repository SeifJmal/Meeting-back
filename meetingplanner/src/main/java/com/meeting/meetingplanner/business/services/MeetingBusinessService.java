package com.meeting.meetingplanner.business.services;

import org.springframework.stereotype.Service;

@Service
public interface MeetingBusinessService {
	public String getAllrooms();
	public String getAllReservation();
	public String associateRommAndReservation();
}
