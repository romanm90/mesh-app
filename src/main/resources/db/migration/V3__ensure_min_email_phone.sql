-- Ensure each user keeps at least one email and one phone.
-- Plain CHECK with subquery is not allowed in Postgres; use BEFORE DELETE triggers.

CREATE OR REPLACE FUNCTION prevent_delete_last_email()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM email_data WHERE user_id = OLD.user_id) <= 1 THEN
        RAISE EXCEPTION 'Cannot delete the last email for user_id=%', OLD.user_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_delete_last_email
    BEFORE DELETE ON email_data
    FOR EACH ROW
    EXECUTE FUNCTION prevent_delete_last_email();

CREATE OR REPLACE FUNCTION prevent_delete_last_phone()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM phone_data WHERE user_id = OLD.user_id) <= 1 THEN
        RAISE EXCEPTION 'Cannot delete the last phone for user_id=%', OLD.user_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_delete_last_phone
    BEFORE DELETE ON phone_data
    FOR EACH ROW
    EXECUTE FUNCTION prevent_delete_last_phone();
