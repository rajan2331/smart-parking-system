-- Default hourly rates (moved here from data.sql). Safe on databases that already have them.
INSERT INTO parking_rates (spot_type, hourly_rate) VALUES
    ('STANDARD',    10.00),
    ('EV',          15.00),
    ('HANDICAPPED',  5.00)
ON CONFLICT (spot_type) DO NOTHING;
