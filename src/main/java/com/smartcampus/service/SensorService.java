package com.smartcampus.service;

import com.smartcampus.exception.InvalidRequestException;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.ResourceConflictException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic for sensor management.
 */
public class SensorService {

    private static final Logger LOGGER = Logger.getLogger(SensorService.class.getName());
    private final DataStore dataStore = DataStore.getInstance();

    public List<Sensor> fetchAllSensors(String typeFilter) {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : dataStore.getSensorCollection().values()) {
            if (typeFilter == null || s.getType().equalsIgnoreCase(typeFilter)) {
                result.add(s);
            }
        }
        return result;
    }

    public Sensor fetchSensorById(String sensorId) {
        Sensor sensor = dataStore.findSensorById(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor not found with ID: " + sensorId);
        }
        return sensor;
    }

    public Sensor registerSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            throw new LinkedResourceNotFoundException("Sensor 'id' is required.");
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            throw new LinkedResourceNotFoundException("Sensor 'type' is required.");
        }
        if (dataStore.sensorExists(sensor.getId())) {
            throw new ResourceConflictException(
                    "Sensor with ID '" + sensor.getId() + "' already exists.");
        }
        if (sensor.getRoomId() == null || !dataStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "roomId '" + sensor.getRoomId() + "' does not exist. "
                    + "Register the room first before assigning sensors to it.");
        }
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }
        dataStore.persistSensor(sensor);
        dataStore.findRoomById(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        LOGGER.info("Sensor registered: " + sensor.getId());
        return sensor;
    }

    public Sensor updateSensor(String sensorId, Sensor sensor) {
        if (sensor == null) {
            throw new InvalidRequestException("Request body is required.");
        }
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            throw new InvalidRequestException("Sensor 'id' is required.");
        }
        if (!sensorId.equals(sensor.getId())) {
            throw new InvalidRequestException(
                    "Sensor ID in the request body must match the URL path.");
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            throw new InvalidRequestException("Sensor 'type' is required.");
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            throw new InvalidRequestException("Sensor 'roomId' is required.");
        }
        if (!dataStore.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "roomId '" + sensor.getRoomId() + "' does not exist. "
                    + "Register the room first before assigning sensors to it.");
        }

        Sensor existing = fetchSensorById(sensorId);
        String previousRoomId = existing.getRoomId();

        existing.setType(sensor.getType());
        existing.setRoomId(sensor.getRoomId());
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            existing.setStatus("ACTIVE");
        } else {
            existing.setStatus(sensor.getStatus());
        }

        synchronizeRoomMembership(sensorId, previousRoomId, existing.getRoomId());
        dataStore.persistSensor(existing);
        LOGGER.info("Sensor updated: " + sensorId);
        return existing;
    }

    public void deleteSensor(String sensorId) {
        Sensor sensor = fetchSensorById(sensorId);
        Room room = dataStore.findRoomById(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }
        dataStore.removeSensor(sensorId);
        dataStore.removeReadings(sensorId);
        LOGGER.info("Sensor deleted: " + sensorId);
    }

    public void assertSensorExists(String sensorId) {
        if (!dataStore.sensorExists(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found with ID: " + sensorId);
        }
    }

    private void synchronizeRoomMembership(
            String sensorId,
            String previousRoomId,
            String updatedRoomId
    ) {
        if (previousRoomId != null && !previousRoomId.equals(updatedRoomId)) {
            Room previousRoom = dataStore.findRoomById(previousRoomId);
            if (previousRoom != null) {
                previousRoom.getSensorIds().remove(sensorId);
            }
        }

        Room updatedRoom = dataStore.findRoomById(updatedRoomId);
        if (updatedRoom != null && !updatedRoom.getSensorIds().contains(sensorId)) {
            updatedRoom.getSensorIds().add(sensorId);
        }
    }
}
