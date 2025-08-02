CREATE TABLE users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL
);

-- ROLES table
CREATE TABLE roles
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- USER_ROLES (many-to-many)
CREATE TABLE users_roles
(
    user_id INT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- PERMISSIONS table
CREATE TABLE permissions
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- ROLE_PERMISSIONS (many-to-many)
CREATE TABLE roles_permissions
(
    role_id       INT NOT NULL REFERENCES roles (id) ON DELETE CASCADE,
    permission_id INT NOT NULL REFERENCES permissions (id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

INSERT INTO roles (name) VALUES
('USER'),
('ADMIN');