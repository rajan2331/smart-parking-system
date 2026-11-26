package com.parking.reservation.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.parking.parking.entity.ParkingSpot;
import com.parking.parking.entity.ParkingSpotType;
import com.parking.reservation.entity.Reservation;
import com.parking.reservation.entity.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    long countByUserIdAndStatus(Long userId, ReservationStatus status);

    List<Reservation> findByUserEmailOrderByStartTimeDesc(String email);

    List<Reservation> findByUserEmailAndStatusOrderByStartTimeDesc(String email, ReservationStatus status);

    /**
     * Is a walk-in blocked on this spot? True if a CONFIRMED reservation is running now
     * or starts before {@code until}.
     */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.parkingSpot.id = :spotId
              and r.status = com.parking.reservation.entity.ReservationStatus.CONFIRMED
              and r.startTime < :until
              and r.endTime > :now
            """)
    boolean existsBlockingReservation(@Param("spotId") Long spotId,
                                      @Param("now") LocalDateTime now,
                                      @Param("until") LocalDateTime until);

    /** Ids of spots that are blocked for walk-ins (see {@link #existsBlockingReservation}). */
    @Query("""
            select distinct r.parkingSpot.id from Reservation r
            where r.status = com.parking.reservation.entity.ReservationStatus.CONFIRMED
              and r.startTime < :until
              and r.endTime > :now
            """)
    List<Long> findSpotIdsBlockedForWalkIns(@Param("now") LocalDateTime now,
                                            @Param("until") LocalDateTime until);

    /** Spots (optionally of one type) with no live reservation overlapping [start, end). */
    @Query("""
            select s from ParkingSpot s
            where s.status <> com.parking.parking.entity.ParkingSpotStatus.MAINTENANCE
              and (:type is null or s.type = :type)
              and not exists (
                  select 1 from Reservation r
                  where r.parkingSpot = s
                    and r.status in :liveStatuses
                    and r.startTime < :end
                    and r.endTime > :start)
            order by s.spotNumber
            """)
    List<ParkingSpot> findSpotsFreeForWindow(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("type") ParkingSpotType type,
                                             @Param("liveStatuses") Collection<ReservationStatus> liveStatuses);

    /** Marks no-shows as EXPIRED. Returns how many were expired. */
    @Modifying
    @Query("""
            update Reservation r
               set r.status = com.parking.reservation.entity.ReservationStatus.EXPIRED
             where r.status = com.parking.reservation.entity.ReservationStatus.CONFIRMED
               and r.startTime < :startedBefore
            """)
    int expireNoShows(@Param("startedBefore") LocalDateTime startedBefore);
}
