package com.meeting.meetingplanner.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meeting.meetingplanner.business.services.MeetingBusinessService;

@RestController
@RequestMapping(path = "/")
public class MeetingController {
	
	@Autowired
	private MeetingBusinessService meetingBusinessService;
	
	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/allRooms")
	public ResponseEntity<String> getAllRooms(){
		return new ResponseEntity<>(meetingBusinessService.getAllrooms(), HttpStatus.OK);
	}
	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/allReservations")
	public ResponseEntity<String> getAllReservations(){
		return new ResponseEntity<>(meetingBusinessService.getAllReservation(), HttpStatus.OK);
	}
	@CrossOrigin(origins = "http://localhost:4200")
	@GetMapping("/associations")
	public ResponseEntity<String> getAssociations(){
		return new ResponseEntity<>(meetingBusinessService.associateRommAndReservation(), HttpStatus.OK);
	}
}
