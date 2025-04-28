CREATE TYPE RoleEnum AS ENUM ('USER', 'ADMIN');
CREATE TYPE JwtTokenTypeEnum AS ENUM ('BEARER');

CREATE TABLE IF NOT EXISTS USER_ (
             id UUID PRIMARY KEY,
             username VARCHAR(255) UNIQUE NOT NULL,
             email VARCHAR(255) UNIQUE NOT NULL,
             password VARCHAR(255) NOT NULL,
             firstname VARCHAR(255) NOT NULL,
             lastname VARCHAR(255) NOT NULL,
             phone VARCHAR(255) NOT NULL,
             created_at TIMESTAMP WITH TIME ZONE NOT NULL,
             updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
             role RoleEnum NOT NULL
);

CREATE TABLE IF NOT EXISTS POST (
            id UUID PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            preview_description VARCHAR,
            description TEXT,
            max_enrolls INT NOT NULL,
            is_personal BOOLEAN NOT NULL,
            place VARCHAR(255) NOT NULL,
            user_id UUID NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
            CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ENROLL_TABLE (
            user_id UUID NOT NULL,
            post_id UUID NOT NULL,
            PRIMARY KEY (user_id, post_id),
            CONSTRAINT fk_enroll_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE,
            CONSTRAINT fk_enroll_post FOREIGN KEY (post_id) REFERENCES POST(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS JWT_TOKEN (
           id UUID PRIMARY KEY,
           token VARCHAR(255) NOT NULL UNIQUE,
           token_type_enum JwtTokenTypeEnum NOT NULL,
           expired BOOLEAN NOT NULL,
           revoked BOOLEAN NOT NULL,
           user_id UUID,
           CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES USER_(id)
);

CREATE TABLE IF NOT EXISTS SECURE_TOKEN (
            id UUID PRIMARY KEY,
            token VARCHAR NOT NULL UNIQUE,
            expired_at TIMESTAMP NOT NULL,
            created_at TIMESTAMP NOT NULL,
            user_id UUID NOT NULL,
            used BOOLEAN NOT NULL ,
            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES USER_(id) ON DELETE CASCADE
);