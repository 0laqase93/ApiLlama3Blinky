-- Add the 'username' column to the 'users' table
ALTER TABLE users ADD COLUMN username VARCHAR(255);

-- Make the 'username' column nullable (optional)
-- ALTER TABLE users ALTER COLUMN username SET NOT NULL;