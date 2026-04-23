package com.smartcampus.service;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.repository.DataStore;

import java.util.List;
import java.util.logging.Logger;

/**
 * Business logic for sensor reading operations.
 */
public class ReadingService {

    private static final Logger LOGGER = Logger.getLogger(ReadingService.class.getName());
    private final DataStore dataStore = DataStore.getInstance();

    public List<SensorReading> fetchReadingHistory(String sensorId) {
        return dataStore.fetchReadingsForSensor(sensorId);
    }

    public SensorReading recordReading(String sensorId, SensorReading incoming) {
        if (incoming == null) {
            throw new IllegalArgumentException("Request body must contain a numeric 'value' field.");
        }
        Sensor sensor = dataStore.findSensorById(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is under MAINTENANCE and cannot accept new readings.");
        }
        SensorReading saved = new SensorReading(incoming.getValue());
        dataStore.archiveReading(sensorId, saved);
        // Side effect: keep parent sensor currentValue in sync
        sensor.setCurrentValue(saved.getValue());
        LOGGER.info("Reading recorded for sensor " + sensorId + ", value=" + saved.getValue());
        return saved;
    }
}
