-- Remove redundant index on users.email
-- The UNIQUE constraint on the email column already creates an index (users_email_key)
-- Having a second index on the same column wastes disk space and slows down writes

DROP INDEX IF EXISTS idx_users_email;
