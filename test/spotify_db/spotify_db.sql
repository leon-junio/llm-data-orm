USE `spotify_db`;

CREATE TABLE playlists (
    id INT AUTO_INCREMENT PRIMARY KEY,
    music VARCHAR(510) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    popularity INT,
    album VARCHAR(255),
    duration varchar(8),
    added_at DATE,
    spotify_track_id VARCHAR(255) NOT NULL UNIQUE,
    playlist_name VARCHAR(255) NOT NULL,
    album_year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
