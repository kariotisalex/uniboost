CREATE TABLE USER_ (
                       id UUID PRIMARY KEY,
                       username VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       firstname VARCHAR(255) NOT NULL,
                       lastname VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL,
                       phone VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP
);

CREATE TABLE POST (
                      id UUID PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      max_enrolls INT NOT NULL,
                      is_personal BOOLEAN NOT NULL,
                      user_id UUID NOT NULL,
                      created_at TIMESTAMP,
                      updated_at TIMESTAMP,
                      CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE
);

CREATE TABLE enroll_table (
                              user_id UUID NOT NULL,
                              post_id UUID NOT NULL,
                              PRIMARY KEY (user_id, post_id),
                              CONSTRAINT fk_enroll_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE,
                              CONSTRAINT fk_enroll_post FOREIGN KEY (post_id) REFERENCES POST(id) ON DELETE CASCADE
);