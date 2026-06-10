-- 1. Hourly Aggregated Usage (What your Usage Service calculates)
CREATE TABLE IF NOT EXISTS hourly_usage (
                                            recorded_hour TIMESTAMP PRIMARY KEY, -- Using the hour as the unique identifier
                                            community_produced FLOAT NOT NULL,
                                            community_used FLOAT NOT NULL,
                                            grid_used FLOAT NOT NULL
);

-- 2. Hourly Percentages (What your Current Percentage Service calculates)
CREATE TABLE IF NOT EXISTS hourly_percentages (
                                                  recorded_hour TIMESTAMP PRIMARY KEY REFERENCES hourly_usage(recorded_hour),
                                                  community_depleted FLOAT NOT NULL,
                                                  grid_portion FLOAT NOT NULL
);