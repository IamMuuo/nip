-- add key_id to api_keys
ALTER TABLE api_keys
   ADD COLUMN IF NOT EXISTS key_id TEXT;
