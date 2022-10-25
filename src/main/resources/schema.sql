CREATE TABLE IF NOT EXISTS Playlists (
id text PRIMARY KEY,
youtube_id text,
name text,
uploader text
);

CREATE TABLE IF NOT EXISTS Music (
id          INTEGER  PRIMARY KEY AUTO_INCREMENT,
name        text,
title       text,
artist      text,
channel     text,
upload_date text,
is_new      integer
);

CREATE TABLE IF NOT EXISTS Playlist_Music (
id          INTEGER  PRIMARY KEY AUTO_INCREMENT,
id_playlist text,
id_music    integer
);

CREATE TABLE IF NOT EXISTS Channels (
id                  INTEGER  PRIMARY KEY AUTO_INCREMENT,
channel             text,
separator           text,
artist_before_title boolean
);

CREATE TABLE IF NOT EXISTS NamingRules (
id          INTEGER  PRIMARY KEY AUTO_INCREMENT,
replace     text,
replace_by  text,
priority    integer
);