CREATE TABLE IF NOT EXISTS user_actions (
    user_id   BIGINT           NOT NULL,
    event_id  BIGINT           NOT NULL,
    max_weight DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP        NOT NULL,
    PRIMARY KEY (user_id, event_id)
);

CREATE TABLE IF NOT EXISTS event_similarity (
    event_a_id BIGINT           NOT NULL,
    event_b_id BIGINT           NOT NULL,
    score      DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (event_a_id, event_b_id)
);
