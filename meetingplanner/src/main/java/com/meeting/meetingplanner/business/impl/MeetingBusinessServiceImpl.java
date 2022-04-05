package com.meeting.meetingplanner.business.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meeting.meetingplanner.business.services.MeetingBusinessService;
import com.meeting.meetingplanner.repository.repository.ReservationsRepository;
import com.meeting.meetingplanner.repository.repository.RoomRepository;
import com.meeting.meetingplanner.domain.Meeting;
import com.meeting.meetingplanner.domain.Reservation;
import com.meeting.meetingplanner.domain.Room;

@Service
public class MeetingBusinessServiceImpl implements MeetingBusinessService {

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private ReservationsRepository reservationsRepository;

	List<Room> allRooms = new ArrayList<>();
	List<Reservation> allReservations = new ArrayList<>();
	List<Meeting> meetings = new ArrayList<>();

	@Override
	public String getAllReservation() {
		this.allReservations = reservationsRepository.findAllReservation();
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this.allReservations);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getAllrooms() {
		this.allRooms = roomRepository.findAllrooms();
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this.allRooms);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String associateRommAndReservation() {
		meetings.clear();
		ArrayList<Room> rooms = (ArrayList<Room>) roomRepository.findAllrooms();
		ArrayList<Reservation> reservations = (ArrayList<Reservation>) reservationsRepository.findAllReservation();
		this.allReservations = reservations.stream().map(reservation -> {
			reservation.updateReservationEndTime();
			return reservation;
		}).collect(Collectors.toList());
		this.allRooms = rooms.stream().map(room -> {
			room.calculateRealCapacityOfRooms();
			return room;
		}).collect(Collectors.toList());

		ArrayList<Reservation> vcTypeReservations = (ArrayList<Reservation>) this.getOrderedMeetingByTypeAndTime("VC");
		this.associateReservationToRoom(vcTypeReservations, "VC");
		ArrayList<Reservation> specTypeReservations = (ArrayList<Reservation>) this
				.getOrderedMeetingByTypeAndTime("SPEC");
		ArrayList<Reservation> rcTypeReservations = (ArrayList<Reservation>) this.getOrderedMeetingByTypeAndTime("RC");
		ArrayList<Reservation> specAndRcReservations = new ArrayList<Reservation>();
		specAndRcReservations.addAll(specTypeReservations);
		specAndRcReservations.addAll(rcTypeReservations);
		this.associateReservationToRoom(specAndRcReservations, "SPEC;RC");
		ArrayList<Reservation> rsTypeReservations = (ArrayList<Reservation>) this.getOrderedMeetingByTypeAndTime("RS");
		this.associateReservationToRoom(rsTypeReservations, "RS");
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(meetings);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private List<Reservation> getOrderedMeetingByTypeAndTime(String type) {
		Comparator<Reservation> compareByStartOnAndMeetingOrder = Comparator.comparingInt(Reservation::getStartOn)
				.thenComparingInt(Reservation::getMeetingNumber);
		return this.allReservations.stream().filter(elemnt -> elemnt.getMeetingType().equals(type))
				.sorted(compareByStartOnAndMeetingOrder).collect(Collectors.toList());
	}

	private void associateReservationToRoom(ArrayList<Reservation> reservations, String type) {
 		if (type.equals("VC")) {
			ArrayList<Room> vcRooms = new ArrayList<>();
			for (Room room : this.allRooms) {
				if (room.getRoomMaterial().contains("Webcam") && room.getRoomMaterial().contains("Pieuvre")
						&& room.getRoomMaterial().contains("Ecran")) {
					vcRooms.add(room);
				}
			}
			vcRooms = (ArrayList<Room>) vcRooms.stream().sorted(Comparator.comparingInt(Room::getCapacity))
					.collect(Collectors.toList());
			for (Reservation reservation : reservations) {
				ArrayList<Reservation> previousReservation = (ArrayList<Reservation>) reservations.stream()
						.filter(elm -> elm.getMeetingNumber() < reservation.getMeetingNumber())
						.collect(Collectors.toList());
				int previousReservationsWithSameTimeNumber = previousReservation.stream()
						.filter(elm -> elm.getReservationEndTime() > reservation.getStartOn())
						.collect(Collectors.toList()).size();
				boolean allAvailablePrevious = onePreviousAreAvailavble(previousReservation, vcRooms);
				if (previousReservationsWithSameTimeNumber == 0 || !allAvailablePrevious) {
					for (Room room : vcRooms) {
						if (reservation.getParticipantNumber() <= room.getCapacity()
								&& reservation.getStartOn() >= room.getOccupoedTo()) {
							room.setOccupationTimeEnd(reservation.getReservationEndTime());
							Meeting meeting = new Meeting(reservation, room);
							meeting.createComment("OK");
							meetings.add(meeting);
							break;
						} else {
							Meeting meeting = new Meeting(reservation,null);
							meeting.createComment("Limited number of place");
							meetings.add(meeting);
							break;
						}
					}
				} else {
					Meeting meeting = new Meeting(reservation, null);
					meeting.createComment("Room Occupied in the same time");
					meetings.add(meeting);
				}
			}
		} else if (type.equals("SPEC;RC")) {
			int usedTable = 0;
			int allRoomsChecked = 0;
			HashMap<Integer, Integer> tablesUsed = new HashMap<>();
			tablesUsed.put(0, 0);
			tablesUsed.put(1, 0);
			Comparator<Reservation> compareByStartOnAndMeetingOrder = Comparator.comparingInt(Reservation::getStartOn)
					.thenComparingInt(Reservation::getMeetingNumber);
			ArrayList<Reservation> sortedRcSpecReservations = (ArrayList<Reservation>) reservations.stream()
					.sorted(compareByStartOnAndMeetingOrder).collect(Collectors.toList());
			for (Reservation reservation : sortedRcSpecReservations) {
				if (reservation.getMeetingType().equals("RC")) {
					Predicate<Room> predicate1 = elm -> elm.getRoomMaterial().contains("Ecran");
					Predicate<Room> predicate2 = elm -> elm.getRoomMaterial().contains("Pieuvre");
					ArrayList<Room> rcRooms = (ArrayList<Room>) this.allRooms.stream().filter(predicate1.and(predicate2))
							.collect(Collectors.toList()).stream().sorted(Comparator.comparingInt(Room::getCapacity))
							.collect(Collectors.toList());
					for (Room room : rcRooms) {
						allRoomsChecked++;
						if (room.getCapacity() >= reservation.getParticipantNumber()
								&& room.getOccupoedTo() <= reservation.getStartOn()
								&& (tablesUsed.get(0) <= reservation.getStartOn()
										|| tablesUsed.get(1) <= reservation.getStartOn())) {
							this.allRooms.stream().filter(rm -> rm.getRoomMaterial().contains(("Tableau")))
									.collect(Collectors.toList()).get(0).removeMaterial("Tableau");
							if (usedTable == 0) {
								tablesUsed.replace(0, reservation.getReservationEndTime());
							} else {
								tablesUsed.replace(1, reservation.getReservationEndTime());
							}

							usedTable++;
							room.setOccupationTimeEnd(reservation.getReservationEndTime());
							room.addMaterialToRomm("Tableau");
							Meeting meeting = new Meeting(reservation, room);
							meetings.add(meeting);
							allRoomsChecked = 0;
							break;
						} else  if(allRoomsChecked == rcRooms.size())  {
							Meeting meeting = new Meeting(reservation, null);
							meeting.createComment("No avalable room");
							meetings.add(meeting);
							allRoomsChecked = 0;
							break;
						}

					}

				} else if (reservation.getMeetingType().equals("SPEC")) {
					ArrayList<Room> sortedRoomForSpec = (ArrayList<Room>) this.allRooms.stream()
							.filter(elm -> elm.getRoomMaterial().size() < 2).collect(Collectors.toList()).stream()
							.sorted(Comparator.comparingInt(Room::getCapacity)).collect(Collectors.toList());
					for (Room room : sortedRoomForSpec) {
						allRoomsChecked ++;
						if (room.getCapacity() >= reservation.getParticipantNumber()
								&& room.getOccupoedTo() <= reservation.getStartOn()
								&& (tablesUsed.get(0) <= reservation.getStartOn()
										|| tablesUsed.get(1) <= reservation.getStartOn())) {
							this.allRooms.stream().filter(rm -> rm.getRoomMaterial().contains(("Tableau")))
									.collect(Collectors.toList()).get(0).removeMaterial("Tableau");
							if (usedTable == 0) {
								tablesUsed.replace(0, reservation.getReservationEndTime());
							} else {
								tablesUsed.replace(1, reservation.getReservationEndTime());
							}
							usedTable++;
							room.setOccupationTimeEnd(reservation.getReservationEndTime());
							room.addMaterialToRomm("Tableau");
							Meeting meeting = new Meeting(reservation, room);
							meeting.createComment("OK");
							meetings.add(meeting);
							allRoomsChecked=0;
							break;

						} else  if(allRoomsChecked == sortedRoomForSpec.size()) {
							Meeting meeting = new Meeting(reservation, null);
							meeting.createComment("No avalable room");
							meetings.add(meeting);
							allRoomsChecked = 0;
							break;
						}
					}
				}
			}

		} else if (type.equals("RS")) {
			Comparator<Reservation> compareByStartOnAndMeetingOrder = Comparator.comparingInt(Reservation::getStartOn)
					.thenComparingInt(Reservation::getMeetingNumber);
			ArrayList<Reservation> sortedRsReservations = (ArrayList<Reservation>) reservations.stream()
					.sorted(compareByStartOnAndMeetingOrder).collect(Collectors.toList());
			ArrayList<Room> sortedRoomForRs = (ArrayList<Room>) this.allRooms.stream()
					.sorted(Comparator.comparingInt(Room::getCapacity)).collect(Collectors.toList());
			int allRoomsChecked = 0;
			for (Reservation reservation : sortedRsReservations) {
				for (Room room : sortedRoomForRs) {
					allRoomsChecked++;
					if(room.getCapacity() >= reservation.getParticipantNumber()
							&& reservation.getStartOn()>=room.getOccupoedTo()) {
						room.setOccupationTimeEnd(reservation.getReservationEndTime());
						Meeting meeting = new Meeting(reservation, room);
						meeting.createComment("OK");
						meetings.add(meeting);
						allRoomsChecked = 0;
						break;
					} else if(allRoomsChecked == sortedRoomForRs.size()) {
						Meeting meeting = new Meeting(reservation, null);
						meeting.createComment("No room available");
						allRoomsChecked = 0;
						meetings.add(meeting);
						break;
					}
				}
			}
			
		}
	}

	private boolean onePreviousAreAvailavble(ArrayList<Reservation> reservations, ArrayList<Room> rooms) {
		for (Reservation previous : reservations) {
			for (Room room2 : rooms) {
				if (previous.getParticipantNumber() <= room2.getCapacity()) {
					return true;
				}
			}
		}
		return false;
	}

}
