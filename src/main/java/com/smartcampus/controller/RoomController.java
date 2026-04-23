package com.smartcampus.controller;

import com.smartcampus.model.Room;
import com.smartcampus.service.RoomService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

/**
 * Part 2 — Room Management.
 * Manages /api/v1/rooms — thin controller, all logic delegated to RoomService.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomController {

    private static final Logger LOGGER = Logger.getLogger(RoomController.class.getName());
    private final RoomService roomService = new RoomService();

    /** GET /api/v1/rooms — list all rooms (200 OK). */
    @GET
    public Response getAllRooms() {
        LOGGER.info("GET /rooms");
        List<Room> rooms = roomService.fetchAllRooms();
        return Response.ok(rooms).build();
    }

    /** POST /api/v1/rooms — create a new room (201 Created). */
    @POST
    public Response createRoom(Room room) {
        LOGGER.info("POST /rooms");
        Room created = roomService.createRoom(room);
        return Response
                .created(URI.create("/api/v1/rooms/" + created.getId()))
                .entity(created)
                .build();
    }

    /** GET /api/v1/rooms/{roomId} — get a specific room (200 | 404). */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        LOGGER.info("GET /rooms/" + roomId);
        Room room = roomService.fetchRoomById(roomId);
        return Response.ok(room).build();
    }

    /** DELETE /api/v1/rooms/{roomId} — decommission a room (204 | 404 | 409). */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        LOGGER.info("DELETE /rooms/" + roomId);
        roomService.deleteRoom(roomId);
        return Response.noContent().build();
    }
}
