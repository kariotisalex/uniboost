CREATE TYPE RoleEnum AS ENUM ('USER', 'ADMIN');
CREATE TABLE IF NOT EXISTS USER_ (
                       id UUID PRIMARY KEY,
                       username VARCHAR(255) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       firstname VARCHAR(255) NOT NULL,
                       lastname VARCHAR(255) NOT NULL,
                       phone VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE,
                       updated_at TIMESTAMP WITH TIME ZONE,
                       role RoleEnum
);

CREATE TABLE IF NOT EXISTS POST (
                      id UUID PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      max_enrolls INT NOT NULL,
                      is_personal BOOLEAN NOT NULL,
                      place VARCHAR NOT NULL,
                      user_id UUID NOT NULL,
                      created_at TIMESTAMP WITH TIME ZONE,
                      updated_at TIMESTAMP WITH TIME ZONE,
                      CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS enroll_table (
                              user_id UUID NOT NULL,
                              post_id UUID NOT NULL,
                              PRIMARY KEY (user_id, post_id),
                              CONSTRAINT fk_enroll_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE,
                              CONSTRAINT fk_enroll_post FOREIGN KEY (post_id) REFERENCES POST(id) ON DELETE CASCADE
);