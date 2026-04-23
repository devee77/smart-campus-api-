package com.smartcampus.service;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic for room management.
 * Controllers delegate all decisions here — they only handle HTTP concerns.
 */
public class RoomService {

    private static final Logger LOGGER = Logger.getLogger(RoomService.class.getName());
    private final DataStore dataStore = DataStore.getInstance();

    public List<Room> fetchAllRooms() {
        LOGGER.info("Fetching all rooms.");
        return new ArrayList<>(dataStore.getRoomRegistry().values());
    }

    public Room fetchRoomById(String roomId) {
        Room room = dataStore.findRoomById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found with ID: " + roomId);
        }
        return room;
    }

    public Room createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            throw new LinkedResourceNotFoundException("Room 'id' is required.");
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            throw new LinkedResourceNotFoundException("Room 'name' is required.");
        }
        if (dataStore.roomExists(room.getId())) {
            throw new LinkedResourceNotFoundException(
                    "Room with ID '" + room.getId() + "' already exists.");
        }
        dataStore.persistRoom(room);
        LOGGER.info("Room created: " + room.getId());
        return room;
    }

    public void deleteRoom(String roomId) {
        Room room = dataStore.findRoomById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room not found with ID: " + roomId);
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted — it still has "
                    + room.getSensorIds().size()
                    + " sensor(s) assigned. Decommission all sensors first.");
        }
        dataStore.removeRoom(roomId);
        LOGGER.info("Room deleted: " + roomId);
    }
}
