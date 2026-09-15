INSERT INTO parking_rates (spot_type, hourly_rate)
VALUES ('STANDARD', 10.00)
ON CONFLICT (spot_type) DO NOTHING;

INSERT INTO parking_rates (spot_type, hourly_rate)
VALUES ('EV', 15.00)
ON CONFLICT (spot_type) DO NOTHING;

INSERT INTO parking_rates (spot_type, hourly_rate)
VALUES ('HANDICAPPED', 5.00)
ON CONFLICT (spot_type) DO NOTHING;