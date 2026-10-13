CREATE DATABASE IF NOT EXISTS devtrack
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE devtrack;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS topics (
    topic_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    topic_name VARCHAR(100) NOT NULL,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'NEEDS_REVISION')
        NOT NULL DEFAULT 'NOT_STARTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_topics_user_name UNIQUE (user_id, topic_name),
    INDEX idx_topics_user (user_id)
);

CREATE TABLE IF NOT EXISTS practice_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    topic_id INT NOT NULL,
    problem_title VARCHAR(200) NOT NULL,
    platform VARCHAR(100) NOT NULL DEFAULT '',
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') NOT NULL,
    status ENUM('SOLVED', 'ATTEMPTED', 'NEEDS_REVISIT') NOT NULL,
    practised_on DATE NOT NULL,
    notes TEXT NOT NULL,
    CONSTRAINT fk_practice_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_practice_topic FOREIGN KEY (topic_id)
        REFERENCES topics(topic_id) ON DELETE CASCADE,
    INDEX idx_practice_user_date (user_id, practised_on),
    INDEX idx_practice_topic (topic_id)
);

CREATE TABLE IF NOT EXISTS revisions (
    revision_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    topic_id INT NOT NULL,
    revision_no INT NOT NULL,
    scheduled_date DATE NOT NULL,
    completed_date DATE NULL,
    status ENUM('PENDING', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_revisions_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_revisions_topic FOREIGN KEY (topic_id)
        REFERENCES topics(topic_id) ON DELETE CASCADE,
    CONSTRAINT uq_revisions_topic_number UNIQUE (topic_id, revision_no),
    INDEX idx_revisions_due (user_id, status, scheduled_date),
    INDEX idx_revisions_topic (topic_id)
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    reset_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP NULL,
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_password_reset_user (user_id, expires_at)
);
