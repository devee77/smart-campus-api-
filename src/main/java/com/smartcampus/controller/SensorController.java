package com.smartcampus.controller;

import com.smartcampus.model.Sensor;
import com.smartcampus.service.SensorService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

/**
 * Part 3 — Sensor Operations & Linking.
 * Manages /api/v1/sensors — thin controller, all logic delegated to SensorService.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorController {

    private static final Logger LOGGER = Logger.getLogger(SensorController.class.getName());
    private final SensorService sensorService = new SensorService();

    /** GET /api/v1/sensors — list sensors, optional ?type= filter (200 OK). */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        LOGGER.info("GET /sensors — type filter: " + type);
        List<Sensor> sensors = sensorService.fetchAllSensors(type);
        return Response.ok(sensors).build();
    }

    /** POST /api/v1/sensors — register a new sensor (201 | 400 | 409 | 422). */
    @POST
    public Response createSensor(Sensor sensor) {
        LOGGER.info("POST /sensors");
        Sensor registered = sensorService.registerSensor(sensor);
        return Response
                .created(URI.create("/api/v1/sensors/" + registered.getId()))
                .entity(registered)
                .build();
    }

    /** GET /api/v1/sensors/{sensorId} — get a specific sensor (200 | 404). */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        LOGGER.info("GET /sensors/" + sensorId);
        Sensor sensor = sensorService.fetchSensorById(sensorId);
        return Response.ok(sensor).build();
    }

    /** PUT /api/v1/sensors/{sensorId} — update sensor metadata (200 | 400 | 404 | 422). */
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor sensor) {
        LOGGER.info("PUT /sensors/" + sensorId);
        Sensor updated = sensorService.updateSensor(sensorId, sensor);
        return Response.ok(updated).build();
    }

    /** DELETE /api/v1/sensors/{sensorId} — decommission a sensor (204 | 404). */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        LOGGER.info("DELETE /sensors/" + sensorId);
        sensorService.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    /**
     * Part 4.1 — Sub-resource locator.
     * Validates sensor exists, then delegates to SensorReadingController.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingController getReadingsResource(@PathParam("sensorId") String sensorId) {
        LOGGER.info("Resolving readings sub-resource for sensor: " + sensorId);
        sensorService.assertSensorExists(sensorId);
        return new SensorReadingController(sensorId);
    }
}
