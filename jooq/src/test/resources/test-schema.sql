CREATE TABLE test_table (
    id int AUTO_INCREMENT PRIMARY KEY,
    created timestamp NOT NULL,
    modified timestamp,
    name varchar NOT NULL,
    type varchar,
    date date NOT NULL
);

CREATE SEQUENCE test_sequence;