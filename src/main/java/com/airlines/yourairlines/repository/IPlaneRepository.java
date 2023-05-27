package com.airlines.yourairlines.repository;

import com.airlines.yourairlines.entity.Plane;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Repository
public interface IPlaneRepository extends IBaseRepository<Plane> {
    @Query(
            value = "(SELECT DISTINCT ON(flight.reserved_plane_id) plane.*, flight.departure_time, flight.arrival_time FROM plane, flight " +
                    "WHERE plane.id = flight.reserved_plane_id AND plane.max_flight_range >= :distance " +
                    "ORDER BY flight.reserved_plane_id, flight.arrival_time DESC) " +
                    "INTERSECT SELECT plane.*, flight.departure_time, flight.arrival_time FROM plane, flight " +
                    "WHERE plane.id = flight.reserved_plane_id AND flight.arrival_time <= :time " +
                    "AND flight.arrival_airport_id = :airport",
            nativeQuery = true)
    ArrayList<Plane> findSuitablePlanes(@Param("distance") Integer flightDistance,
                                        @Param("time") LocalDateTime requiredDateTime,
                                        @Param("airport") Long departureAirportId);

}
