-- PostgreSQL initialization script for Docker
-- This script runs when the container is first created

-- Create additional databases if needed
-- CREATE DATABASE ums_test;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'UTC';

-- Performance optimizations for Docker environment
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET pg_stat_statements.track = 'all';
ALTER SYSTEM SET log_statement = 'none';
ALTER SYSTEM SET log_min_duration_statement = 1000;