-- 1. Producers (Solar panels, wind turbines, etc.)
CREATE TABLE IF NOT EXISTS energy_producers (
                                                id SERIAL PRIMARY KEY,
                                                max_capacity FLOAT NOT NULL -- Represents the maximum energy output (e.g., in kWh)
);

-- 2. Users (The households)
CREATE TABLE IF NOT EXISTS energy_users (
                                            id SERIAL PRIMARY KEY,
                                            firstname VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    producer_id INT REFERENCES energy_producers(id)
    -- ^ This is the Foreign Key! It links to the id in energy_producers.
    -- If a user doesn't have a PV panel, this can just be NULL.
    );

-- 3. Production Logs (Minute-by-minute data from producers)
CREATE TABLE IF NOT EXISTS energy_production_logs (
                                                      id SERIAL PRIMARY KEY,
                                                      producer_id INT NOT NULL REFERENCES energy_producers(id),
    provided_energy FLOAT NOT NULL,
    recorded_at TIMESTAMP NOT NULL
    );

-- 4. Usage Logs (Minute-by-minute data from users)
CREATE TABLE IF NOT EXISTS energy_usage_logs (
                                                 id SERIAL PRIMARY KEY,
                                                 user_id INT NOT NULL REFERENCES energy_users(id),
    used_energy FLOAT NOT NULL,
    recorded_at TIMESTAMP NOT NULL
    );

-- 5. Hourly Aggregated Usage (What your Usage Service calculates)
CREATE TABLE IF NOT EXISTS hourly_usage (
                                            recorded_hour TIMESTAMP PRIMARY KEY, -- Using the hour as the unique identifier
                                            community_produced FLOAT NOT NULL,
                                            community_used FLOAT NOT NULL,
                                            grid_used FLOAT NOT NULL
);

-- 6. Hourly Percentages (What your Current Percentage Service calculates)
CREATE TABLE IF NOT EXISTS hourly_percentages (
                                                  recorded_hour TIMESTAMP PRIMARY KEY REFERENCES hourly_usage(recorded_hour),
    community_depleted FLOAT NOT NULL,
    grid_portion FLOAT NOT NULL
    );


-- 1. Insert 5 Producers (Total max_capacity = 20)
-- We'll give them 4.0 kW capacity each.
INSERT INTO energy_producers (max_capacity) VALUES
                                                (4.0),
                                                (4.0),
                                                (4.0),
                                                (4.0),
                                                (4.0);

-- 2. Insert 10 Users (5 with PV, 5 without)
-- We link the first 5 users to the producer IDs 1 through 5.
-- The users without PV simply get NULL for their producer_id.
INSERT INTO energy_users (firstname, lastname, address, producer_id) VALUES
                                                                         ('Alice', 'Smith', '101 Solar Way', 1),
                                                                         ('Bob', 'Jones', '102 Solar Way', 2),
                                                                         ('Charlie', 'Brown', '103 Solar Way', 3),
                                                                         ('Diana', 'Prince', '104 Solar Way', 4),
                                                                         ('Evan', 'Wright', '105 Solar Way', 5),
                                                                         ('Fiona', 'Gallagher', '201 Grid St', NULL),
                                                                         ('George', 'Costanza', '202 Grid St', NULL),
                                                                         ('Hannah', 'Abbott', '203 Grid St', NULL),
                                                                         ('Ian', 'Malcolm', '204 Grid St', NULL),
                                                                         ('Jane', 'Doe', '205 Grid St', NULL);

-- 3. Insert the Hourly Usage Data
INSERT INTO hourly_usage (recorded_hour, community_produced, community_used, grid_used) VALUES
                                                                                            ('2025-01-10 14:00:00', 18.05, 18.05, 1.076),
                                                                                            ('2025-01-10 13:00:00', 15.015, 14.033, 2.049),
                                                                                            ('2025-01-10 12:00:00', 12.3, 11.8, 3.2),
                                                                                            ('2025-01-10 11:00:00', 10.5, 10.5, 4.1),
                                                                                            ('2025-01-10 10:00:00', 8.2, 7.9, 5.3);

-- ==========================================
-- INSERT ENERGY PRODUCTION LOGS (5 Producers)
-- ==========================================

-- 14:00 (3.61 each)
INSERT INTO energy_production_logs (producer_id, provided_energy, recorded_at)
SELECT id, 3.61, '2025-01-10 14:00:00' FROM energy_producers;

-- 13:00 (3.003 each)
INSERT INTO energy_production_logs (producer_id, provided_energy, recorded_at)
SELECT id, 3.003, '2025-01-10 13:00:00' FROM energy_producers;

-- 12:00 (2.46 each)
INSERT INTO energy_production_logs (producer_id, provided_energy, recorded_at)
SELECT id, 2.46, '2025-01-10 12:00:00' FROM energy_producers;

-- 11:00 (2.1 each)
INSERT INTO energy_production_logs (producer_id, provided_energy, recorded_at)
SELECT id, 2.1, '2025-01-10 11:00:00' FROM energy_producers;

-- 10:00 (1.64 each)
INSERT INTO energy_production_logs (producer_id, provided_energy, recorded_at)
SELECT id, 1.64, '2025-01-10 10:00:00' FROM energy_producers;


-- ==========================================
-- INSERT ENERGY USAGE LOGS (10 Users)
-- ==========================================

-- 14:00 (1.9126 each)
INSERT INTO energy_usage_logs (user_id, used_energy, recorded_at)
SELECT id, 1.9126, '2025-01-10 14:00:00' FROM energy_users;

-- 13:00 (1.6082 each)
INSERT INTO energy_usage_logs (user_id, used_energy, recorded_at)
SELECT id, 1.6082, '2025-01-10 13:00:00' FROM energy_users;

-- 12:00 (1.5 each)
INSERT INTO energy_usage_logs (user_id, used_energy, recorded_at)
SELECT id, 1.5, '2025-01-10 12:00:00' FROM energy_users;

-- 11:00 (1.46 each)
INSERT INTO energy_usage_logs (user_id, used_energy, recorded_at)
SELECT id, 1.46, '2025-01-10 11:00:00' FROM energy_users;

-- 10:00 (1.32 each)
INSERT INTO energy_usage_logs (user_id, used_energy, recorded_at)
SELECT id, 1.32, '2025-01-10 10:00:00' FROM energy_users;

INSERT INTO hourly_percentages (recorded_hour, community_depleted, grid_portion)
SELECT
    recorded_hour,
    (community_used / community_produced) * 100,
    (grid_used / (grid_used + community_used)) * 100
FROM hourly_usage;