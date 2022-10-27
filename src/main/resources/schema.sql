CREATE TABLE IF NOT EXISTS Playlists (
id          text PRIMARY KEY,
name        text,
uploader    text
);

CREATE TABLE IF NOT EXISTS Music (
id          text  PRIMARY KEY,
name        text,
title       text,
artist      text,
uploader     text,
upload_date text,
is_new      boolean
);

CREATE TABLE IF NOT EXISTS Playlist_Music (
id          text  PRIMARY KEY,
id_playlist text,
id_music    text
);

CREATE TABLE IF NOT EXISTS Uploaders (
id                  text  PRIMARY KEY,
name                text,
separator           text,
artist_before_title boolean
);

CREATE TABLE IF NOT EXISTS NamingRules (
id          text  PRIMARY KEY,
replace     text,
replace_by  text,
priority    integer
);