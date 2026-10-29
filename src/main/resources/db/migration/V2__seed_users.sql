-- Seed users for local/dev. Test password for all seeded users: password
-- BCrypt hash generated with cost 10 ($2y$ treated as $2a$ by Spring BCryptPasswordEncoder).

INSERT INTO users (id, name, date_of_birth, password_hash) VALUES
    (1, 'Ivan Ivanov', DATE '1990-01-15', '$2a$10$oXfvCkgDCzrcH.rv8Wdj1.YN5UJQOX7LVy3q/rwYw.SoGHITHDVZK'),
    (2, 'Petr Petrov', DATE '1985-06-20', '$2a$10$oXfvCkgDCzrcH.rv8Wdj1.YN5UJQOX7LVy3q/rwYw.SoGHITHDVZK'),
    (3, 'Anna Sidorova', DATE '1995-12-01', '$2a$10$oXfvCkgDCzrcH.rv8Wdj1.YN5UJQOX7LVy3q/rwYw.SoGHITHDVZK');

SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));

INSERT INTO account (user_id, balance, initial_balance) VALUES
    (1, 1000.00, 1000.00),
    (2, 500.00, 500.00),
    (3, 2000.00, 2000.00);

INSERT INTO email_data (user_id, email) VALUES
    (1, 'ivan@meshgroup.ru'),
    (2, 'petr@meshgroup.ru'),
    (3, 'anna@meshgroup.ru');

INSERT INTO phone_data (user_id, phone) VALUES
    (1, '79001112233'),
    (2, '79002223344'),
    (3, '79003334455');
