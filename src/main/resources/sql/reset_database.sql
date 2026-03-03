-- ==========================================
-- Database Cleanup & Reset Script
-- ==========================================

-- Drop existing database if it exists
DROP DATABASE IF EXISTS ITP_41_LMS;

-- Create fresh database
CREATE DATABASE ITP_41_LMS;
USE ITP_41_LMS;

-- ==========================================
-- Users Table (Student Accounts)
-- ==========================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Courses Table (Future Use)
-- ==========================================
CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description LONGTEXT,
    tutor_id BIGINT,
    category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tutor_id (tutor_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Student Enrollments (Future Use)
-- ==========================================
CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    progress_percentage INT DEFAULT 0,
    status ENUM('ACTIVE', 'COMPLETED', 'DROPPED') DEFAULT 'ACTIVE',
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    UNIQUE KEY unique_enrollment (student_id, course_id),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Lessons Table (Future Use)
-- ==========================================
CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description LONGTEXT,
    video_url VARCHAR(500),
    content LONGTEXT,
    order_number INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Assignments Table (Future Use)
-- ==========================================
CREATE TABLE assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description LONGTEXT,
    due_date DATETIME,
    max_score INT DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Assignment Submissions (Future Use)
-- ==========================================
CREATE TABLE submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submission_content LONGTEXT,
    file_url VARCHAR(500),
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score INT,
    feedback LONGTEXT,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_assignment_id (assignment_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- Insert Sample Data for Testing
-- ==========================================

-- Insert sample user (password: Test@123 - hashed with BCrypt)
INSERT INTO users (username, email, password, full_name, phone_number, is_active) VALUES
('johndoe', 'john@example.com', '$2a$10$Jvm4S9IJVPXZ9C6eYRYFxuGu5/R4D.FIo/E7V2pNGqpaMZ4L6j98u', 'John Doe', '0712345678', TRUE),
('nisapen', 'nisa@example.com', '$2a$10$Jvm4S9IJVPXZ9C6eYRYFxuGu5/R4D.FIo/E7V2pNGqpaMZ4L6j98u', 'Nisa Perera', '0787654321', TRUE);

-- Sample courses
INSERT INTO courses (title, description, category) VALUES
('Mathematics O/L', 'Comprehensive Mathematics course for O/Level students', 'Science'),
('English Literature A/L', 'Advanced English Literature course for A/Level students', 'Language'),
('Biology O/L', 'Science Biology course for O/Level students', 'Science');

-- ==========================================
-- Create Indexes for Performance
-- ==========================================
CREATE INDEX idx_user_active ON users(is_active);
CREATE INDEX idx_enrollment_status ON enrollments(status);

-- ==========================================
-- Database Reset Complete
-- ==========================================
