package dev.eder.oreogeofence.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class Reminder {
    public String id = String.valueOf(UUID.randomUUID());
    public LatLng latLng;
    public Double radius;
    public String message;
}
