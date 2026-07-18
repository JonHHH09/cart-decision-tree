CREATE SCHEMA IF NOT EXISTS training_data;
CREATE SCHEMA IF NOT EXISTS test_data;

CREATE TABLE training_data.fruit_examples (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    color VARCHAR(32) NOT NULL,
    shape VARCHAR(32) NOT NULL,
    weight_grams INTEGER NOT NULL CHECK (weight_grams > 0),
    skin VARCHAR(32) NOT NULL,
    fruit VARCHAR(32) NOT NULL
);

CREATE TABLE test_data.fruit_examples (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    color VARCHAR(32) NOT NULL,
    shape VARCHAR(32) NOT NULL,
    weight_grams INTEGER NOT NULL CHECK (weight_grams > 0),
    skin VARCHAR(32) NOT NULL,
    fruit VARCHAR(32) NOT NULL
);

INSERT INTO training_data.fruit_examples (color, shape, weight_grams, skin, fruit) VALUES
    ('red', 'round', 182, 'smooth', 'apple'),
    ('green', 'round', 160, 'smooth', 'apple'),
    ('yellow', 'round', 175, 'smooth', 'apple'),
    ('red', 'round', 205, 'smooth', 'apple'),
    ('green', 'round', 140, 'smooth', 'apple'),
    ('red', 'round', 168, 'smooth', 'apple'),
    ('yellow', 'long', 118, 'smooth', 'banana'),
    ('yellow', 'long', 132, 'smooth', 'banana'),
    ('green', 'long', 110, 'smooth', 'banana'),
    ('yellow', 'curved', 125, 'smooth', 'banana'),
    ('yellow', 'long', 145, 'smooth', 'banana'),
    ('orange', 'round', 170, 'pebbled', 'orange'),
    ('orange', 'round', 190, 'pebbled', 'orange'),
    ('orange', 'round', 155, 'pebbled', 'orange'),
    ('orange', 'round', 210, 'pebbled', 'orange'),
    ('green', 'bell', 175, 'smooth', 'pear'),
    ('yellow', 'bell', 160, 'smooth', 'pear'),
    ('green', 'bell', 190, 'rough', 'pear'),
    ('brown', 'bell', 185, 'rough', 'pear'),
    ('purple', 'oval', 7, 'smooth', 'grape'),
    ('green', 'oval', 6, 'smooth', 'grape'),
    ('red', 'oval', 8, 'smooth', 'grape'),
    ('purple', 'round', 5, 'smooth', 'grape'),
    ('green', 'round', 6, 'smooth', 'grape');

INSERT INTO test_data.fruit_examples (color, shape, weight_grams, skin, fruit) VALUES
    ('red', 'round', 190, 'smooth', 'apple'),
    ('green', 'round', 150, 'smooth', 'apple'),
    ('yellow', 'round', 165, 'smooth', 'apple'),
    ('yellow', 'long', 120, 'smooth', 'banana'),
    ('green', 'long', 115, 'smooth', 'banana'),
    ('orange', 'round', 180, 'pebbled', 'orange'),
    ('orange', 'round', 165, 'pebbled', 'orange'),
    ('green', 'bell', 180, 'smooth', 'pear'),
    ('yellow', 'bell', 170, 'smooth', 'pear'),
    ('purple', 'oval', 6, 'smooth', 'grape'),
    ('red', 'oval', 7, 'smooth', 'grape');
