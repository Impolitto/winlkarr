package com.winlkar.app.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Station {
    public final String name;
    public final LatLng location;

    public Station(String name, LatLng location) {
        this.name = name;
        this.location = location;
    }

    public static List<Station> getDefaultStations() {
        List<Station> stations = new ArrayList<>();
        stations.add(new Station("Sousse Centrale", new LatLng(35.8282, 10.6358)));
        stations.add(new Station("Trocadero", new LatLng(35.8315, 10.6315)));
        stations.add(new Station("Sahloul", new LatLng(35.8340, 10.5930)));
        stations.add(new Station("Sousse Riadh", new LatLng(35.7955, 10.5843)));
        stations.add(new Station("Hammam Sousse", new LatLng(35.8594, 10.5985)));
        stations.add(new Station("Port El Kantaoui", new LatLng(35.8947, 10.5982)));
        stations.add(new Station("Akouda", new LatLng(35.8694, 10.5644)));
        stations.add(new Station("Kalaa Sghira", new LatLng(35.8200, 10.5600)));
        stations.add(new Station("M'saken", new LatLng(35.7297, 10.5800)));
        stations.add(new Station("Monastir Airport", new LatLng(35.7643, 10.8113)));
        return stations;
    }
}
