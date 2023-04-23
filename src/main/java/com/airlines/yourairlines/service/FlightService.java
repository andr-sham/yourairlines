package com.airlines.yourairlines.service;

import com.airlines.yourairlines.entity.Airport;
import com.airlines.yourairlines.entity.Flight;
import com.airlines.yourairlines.entity.Plane;
import com.airlines.yourairlines.repository.IBaseRepository;
import com.airlines.yourairlines.repository.IFlightRepository;
import com.airlines.yourairlines.repository.IPlaneRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service

public class FlightService extends CrudService<Flight> implements IFlightService {
    @Autowired
    private IFlightRepository flightRepository;
    @Autowired
    private IPlaneRepository planeRepository;

    @Override
    public IBaseRepository<Flight> getRepository() {
        return flightRepository;
    }

    @Override
    protected void validate(Flight entity) {

    }

    private Integer calcFlightDuration(Airport departureAirport, Airport arrivalAirport) {
        if (departureAirport == arrivalAirport) {
            return 0;
        }
        return 5;
    }


    private Integer calcFlightDistance(Airport departureAirport, Airport arrivalAirport) {
        if (departureAirport == arrivalAirport) {
            return 0;
        }
        return 1000;
    }


    public ArrayList<Plane> getSuitablePlanes(Airport departureAirport, Airport arrivalAirport, LocalDateTime departureTime) {

        ArrayList<Plane> suitablePlanes = planeRepository.findByMaxFlightRangeGreaterThanEqualAndEndOfReserveTimeBefore(calcFlightDistance(departureAirport, arrivalAirport), departureTime);

        return filterPlanesWithHop(suitablePlanes, departureAirport, departureTime);
    }

    private ArrayList<Plane> filterPlanesWithHop(ArrayList<Plane> planes, Airport departureAirport, LocalDateTime departureTime) {

        return planes.stream().filter(plane -> plane.getMaxFlightRange() < calcFlightDistance(departureAirport, calcLastReservedAirport(plane)))
                .filter(plane -> departureTime.isBefore(calcLastReservedArrivalTime(plane).plusHours(calcFlightDuration(departureAirport, calcLastReservedAirport(plane)))))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    private Airport calcLastReservedAirport(Plane plane) {
        List<Flight> reservedFlights = plane.getReservedFlights();
        boolean a = reservedFlights.isEmpty();
        return plane.getReservedFlights().stream().max(Comparator.comparing(Flight::getArrivalTime)).get().getDepartureAirport();
    }

    private LocalDateTime calcLastReservedArrivalTime(Plane plane) {
        return plane.getReservedFlights().stream().max(Comparator.comparing(Flight::getArrivalTime)).get().getArrivalTime();
    }

}