package com.smartcampus.controller;

import com.smartcampus.model.SensorReading;
import com.smartcampus.service.ReadingService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

/**
 * Part 4 — Sub-resource for sensor readings.
 * Handles GET and POST for /api/v1/sensors/{sensorId}/readings.
 * Instantiated by SensorController via the sub-resource locator pattern.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingController {

    private static final Logger LOGGER = Logger.getLogger(SensorReadingController.class.getName());

    private final String        sensorId;
    private final ReadingService readingService;

    public SensorReadingController(String sensorId) {
        this.sensorId      = sensorId;
        this.readingService = new ReadingService();
    }

    /** GET /api/v1/sensors/{sensorId}/readings — fetch reading history (200 OK). */
    @GET
    public Response getReadings() {
        LOGGER.info("GET /sensors/" + sensorId + "/readings");
        List<SensorReading> history = readingService.fetchReadingHistory(sensorId);
        return Response.ok(history).build();
    }

    /** POST /api/v1/sensors/{sensorId}/readings — record a new reading (201 | 400 | 403). */
    @POST
    public Response addReading(SensorReading reading) {
        LOGGER.info("POST /sensors/" + sensorId + "/readings");
        SensorReading saved = readingService.recordReading(sensorId, reading);
        return Response
                .created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + saved.getId()))
                .entity(saved)
                .build();
    }
}
