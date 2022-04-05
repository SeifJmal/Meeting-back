package com.meeting.meetingplanner.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meeting {
 @SuppressWarnings("unused")
private Room room;
 @SuppressWarnings("unused")
private Reservation reservation;
@SuppressWarnings("unused")
private String comment;
 
 public Meeting(Reservation reservation, Room room) {
	 this.reservation = reservation;
	 this.room = room;
 }
 public void createComment(String comment) {
	 this.comment = comment;
 }
}
