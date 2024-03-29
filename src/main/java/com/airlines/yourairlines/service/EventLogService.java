package com.airlines.yourairlines.service;

import com.airlines.yourairlines.entity.Flight;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Getter
@Setter
public class EventLogService {
  private final HashMap<LocalDateTime, String> eventLog = new HashMap<>();
  @Lazy @Autowired private IFlightService flightService;
  @Autowired private DayChangeService dayChangeService;
  @Autowired private IPlaneService planeService;
  @Autowired private IAirportService airportService;

  @Value("${frequency.of.sync}")
  private String numberOfTimeSyncStr;

  public void displayEvent() {
    ArrayList<LocalDateTime> arrayList =
        eventLog.keySet().stream()
            .filter(eventDate -> eventDate.isBefore(dayChangeService.getCurrentDate()))
            .peek((event) -> log.info(eventLog.get(event)))
            .collect(Collectors.toCollection(ArrayList::new));

    arrayList.forEach(eventLog::remove);
  }

  public void syncEventLog() {
    log.info("!!!!!");

    ArrayList<Flight> departureList =
        flightService.findByDepartureTimeAfterAndDepartureTimeBefore(
            dayChangeService.getCurrentDate(), calcEndTimeOfSync());
    ArrayList<Flight> arrivalList =
        flightService.findByArrivalTimeAfterAndArrivalTimeBefore(
            dayChangeService.getCurrentDate(), calcEndTimeOfSync());

    eventLog.clear();

    for (Flight flight : departureList) {
      if (eventLog.containsKey(flight.getDepartureTime())) {
        eventLog.put(
            flight.getDepartureTime(),
            eventLog.get(flight.getDepartureTime()) + "\n" + fillDepartureEventString(flight));
      } else {
        eventLog.put(flight.getDepartureTime(), fillDepartureEventString(flight));
      }
    }

    for (Flight flight : arrivalList) {
      if (eventLog.containsKey(flight.getArrivalTime())) {
        eventLog.put(
            flight.getArrivalTime(),
            eventLog.get(flight.getArrivalTime()) + "\n" + fillArrivalEventString(flight));
      } else {
        eventLog.put(flight.getArrivalTime(), fillArrivalEventString(flight));
      }
    }
  }

  private String fillDepartureEventString(Flight flight) {
    return String.format(
        "Борт %s вылетел из аэропорта  %s в аэропорт  %s",
        planeService.getSideNumberById(flight.getReservedPlaneId()),
        airportService.getAirportNameById(flight.getDepartureAirportId()),
        airportService.getAirportNameById(flight.getArrivalAirportId()));
  }

  private String fillArrivalEventString(Flight flight) {
    return String.format(
        "Борт %s прилетел в аэропорт %s из аэропорта %s ",
        planeService.getSideNumberById(flight.getReservedPlaneId()),
        airportService.getAirportNameById(flight.getArrivalAirportId()),
        airportService.getAirportNameById(flight.getDepartureAirportId()));
  }

  public LocalDateTime calcEndTimeOfSync() {

    Integer numberOfTimeSync = Integer.parseInt(numberOfTimeSyncStr);
    LocalDateTime endTimeOfSync = dayChangeService.getCurrentDate();

    for (int i = 0; i < numberOfTimeSync; i++) {
      endTimeOfSync =
          endTimeOfSync
              .plusHours(dayChangeService.getIncreaseHour())
              .plusMinutes(dayChangeService.getIncreaseMinutes());
    }

    return endTimeOfSync;
  }
}
