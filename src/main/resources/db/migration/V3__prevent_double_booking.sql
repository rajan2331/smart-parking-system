-- Optimistic locking for parking spots: concurrent updates to the same spot (e.g. two drivers
-- taking it at the same moment) are detected and the later one is rejected.
ALTER TABLE parking_spot ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Database-level guarantee: at most one ACTIVE session per spot and per vehicle.
CREATE UNIQUE INDEX ux_active_session_per_spot
    ON parking_sessions (parking_spot_id) WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX ux_active_session_per_vehicle
    ON parking_sessions (vehicle_id) WHERE status = 'ACTIVE';
