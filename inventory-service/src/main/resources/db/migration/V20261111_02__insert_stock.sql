-- Demo data, in its own migration so it's easy to spot.
INSERT INTO stock (sku, available) VALUES
    ('BOOK-1',   10),
    ('PEN-1',    100),
    ('LAPTOP-1', 2);