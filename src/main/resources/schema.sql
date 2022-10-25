CREATE TABLE IF NOT EXISTS Playlists (
id          INTEGER PRIMARY KEY     AUTOINCREMENT,
youtube_id  text,
name        text,
uploader    text
);

CREATE TABLE IF NOT EXISTS Music (
id          text        PRIMARY KEY,
name        text,
title       text,
artist      text,
channel     text,
upload_date text,
is_new      integer
);

CREATE TABLE IF NOT EXISTS Playlist_Music (
id          INTEGER PRIMARY KEY     AUTOINCREMENT,
id_playlist text,
id_music    text
);

CREATE TABLE IF NOT EXISTS Channels (
channel             text    PRIMARY KEY,
separator           text,
artist_before_title integer
);

CREATE TABLE IF NOT EXISTS NamingRules (
id          INTEGER PRIMARY KEY     AUTOINCREMENT,
replace     text,
replace_by  text,
priority    integer
);