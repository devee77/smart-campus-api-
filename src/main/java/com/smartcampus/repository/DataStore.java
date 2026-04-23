package com.smartcampus.repository;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Central in-memory data store for all campus entities.
 *
 * Thread-safe singleton backed by ConcurrentHashMap.
 * All service-layer read/write access goes through this class.
 */
public class DataStore {

    private static final Logger LOGGER = Logger.getLogger(DataStore.class.getName());
    private static final DataStore INSTANCE = new DataStore();

    // Named to reflect domain role
    private final Map<String, Room>                roomRegistry     = new ConcurrentHashMap<>();
    private final Map<String, Sensor>              sensorCollection = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readingArchive   = new ConcurrentHashMap<>();

    private DataStore() {
        seedDemoData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── Room operations ──────────────────────────────────────────────

    public Map<String, Room> getRoomRegistry()           { return roomRegistry; }
    public Room   findRoomById(String id)                { return roomRegistry.get(id); }
    public boolean roomExists(String id)                 { return roomRegistry.containsKey(id); }
    public void   persistRoom(Room r)                    { roomRegistry.put(r.getId(), r); }
    public void   removeRoom(String id)                  { roomRegistry.remove(id); }

    // ── Sensor operations ─────────────────────────────────────────────

    public Map<String, Sensor> getSensorCollection()     { return sensorCollection; }
    public Sensor findSensorById(String id)              { return sensorCollection.get(id); }
    public boolean sensorExists(String id)               { return sensorCollection.containsKey(id); }
    public void   persistSensor(Sensor s)                { sensorCollection.put(s.getId(), s); }
    public void   removeSensor(String id)                { sensorCollection.remove(id); }

    // ── Reading operations ────────────────────────────────────────────

    public List<SensorReading> fetchReadingsForSensor(String sensorId) {
        return readingArchive.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    public void archiveReading(String sensorId, SensorReading reading) {
        fetchReadingsForSensor(sensorId).add(reading);
    }

    public void removeReadings(String sensorId) {
        readingArchive.remove(sensorId);
    }

    // ── Seed data ─────────────────────────────────────────────────────

    private void seedDemoData() {
        Room r1 = new Room("ADMIN-100", "Facilities Office", 20);
        Room r2 = new Room("LAB-102", "Computer Science Lab", 30);
        roomRegistry.put(r1.getId(), r1);
        roomRegistry.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-900", "Temperature", "ACTIVE",       22.5,  "ADMIN-100");
        Sensor s2 = new Sensor("CO2-900",  "CO2",         "ACTIVE",       412.0, "ADMIN-100");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE",  0.0,   "LAB-102");
        sensorCollection.put(s1.getId(), s1);
        sensorCollection.put(s2.getId(), s2);
        sensorCollection.put(s3.getId(), s3);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());

        readingArchive.put(s1.getId(), new ArrayList<>());
        readingArchive.put(s2.getId(), new ArrayList<>());
        readingArchive.put(s3.getId(), new ArrayList<>());

        LOGGER.info("DataStore seeded with demo rooms and sensors.");
    }
}
