# YtMusicDownloader-api
Manage, through this API, a daemon that automatically downloads music from YouTube playlists.
## Features
- Handle multiple playlists :
    - Add/Remove playlists
    - Edit playlists
    - Trigger playlist download
    - Generate playlist files for Mopidy and PowerAmp
- Set interval time between each automatic download
- Handle newly downloaded music :
    - See list of downloaded music "not seen yet"
    - Rename music title and artist manually
    - Set new music to "seen"
- Handle title and artist naming :
    - Add/Edit/Delete rules
    - Strings replace (ex: replace " [Official Music Video]" by "")
    - Set title/artist format to apply depending on youtube channel 
    - Set title/artist default format to apply for new channels
## List of endpoints
- `/`
- `/settings`
- `/settings/music-folder`
- `/settings/download-occurrence`
- `/factory-reset`
- `/youtube-dl/update`
- `/playlists`
- `/playlists/download`
- `/playlists/<identifier>`
- `/playlists/<identifier>/download`
- `/music/new`
- `/music/<identifier>`
- `/naming-rules`
- `/naming-rules/<identifier>`
- `/uploaders`
- `/uploaders/<identifier>`
## Usage
All requests will have the `Application/json` header.  
All `POST` requests will take a `json` as a body.  
All responses will have the form :  
```json
{
    "data": "Mixed type holding the content of the response",
    "message": "Description of what happened"
}
```
Subsequent response definitions will only detail the expected value of the `data field`.  
Also, they will only define the responses of `GET` request. Post requests usually return the full json object that was modified.  

## `/settings`
### `GET`  
Get the configuration file.  
*Response*  
```json
{
    "musicFolder": "~/Music",
    "downloadOccurrence": 1
}
```
### `POST`  
Set the configuration.  
*Body*  
```json
{
    "musicFolder": "~/Music",
    "downloadOccurrence": 1
}
```

## `/settings/music-folder`
### `POST`  
Set the path to the music library.  
*Body*  
```json
"~/Music"
```

## `/settings/download-occurrence`
### `POST`
Give an interval of time for when the service will automatically download all the playlists.  
The interval is defined in hours and the default is 12. Set it at -1 to disable it.  
The first download will occur in an amount of time equal to the "interval" value, starting from the execution of this command.  
*Body*  
```json
1
```

## `/factory-reset`
### `POST`  
Reset configuration and database to default. 

## `/youtube-dl/update`
If you get an internal server error, updating youtube-dl might be a quick fix.  
If not, you'll have to wait for an update from qmk YtMusicDownloader.  

## `/playlists`
### `GET`  
Get information on the registered playlists in the database.  
*Response*  
```json
[
    {
        "id": "PLCVGGn6GhhDu_4yn_9eN3xBYB4POkLBYT",
        "name": "Best of Willi tracks",
        "musicIds": []
    },{
        "id": "PLCVGGn6GhhDtHxCJcPNymXhCtyEisxERY",
        "name": "Best of Chill Music",
        "musicIds": []
    }
]
```
### `POST`  
Register a playlist in the database.  
*Body*  
```json
{
    "name": "Best of Willy tracks",
    "url": "https://www.youtube.com/playlist?list=PLCVGGn6GhhDu_4yn_9eN3xBYB4POkLBYT"
}
```

## `/playlists/download`
*Response*  
### `GET`  
Trigger download of all registered playlists.   

## `/playlists/<identifier>`
### `GET`
Get specified playlist
### `POST`  
Edit a specified playlist.  
*Body*  
```json
{
    "name": "Best of Willy tracks",
    "url": "https://www.youtube.com/playlist?list=PLCVGGn6GhhDu_4yn_9eN3xBYB4POkLBYT"
}
```
### `DELETE`
Remove a registered playlist from the database. Downloaded files from this playlist are not touched.  
If you re-register a removed playlist, it will re-download all the music (excluding those that were already downloaded).   

## `/playlists/<identifier>/download`  
### `GET`  
Trigger download of the specified playlist.  

## `/music/new`
### `GET`  
Returns list of "not seen" music (where the "isNew" parameter equals "true").  
*Response*  
```json
[
    {
        "id": "ftshNCG_RPk",
        "fileName": "Bad Computer - Riddle [Monstercat Release]",
        "fileExtension": "mp3",
        "title": "Riddle",
        "artist": "Bad Computer",
        "uploaderId": "Monstercat: Uncaged",
        "uploadDate": "13/04/2020",
        "isNew": "true",
        "playlistIds": ["PLCVGGn6GhhDu_4yn_9eN3xBYB4POkLBYT"]
    },
    {
        "id": "5S5zfXao-h0",
        "fileName": "Netrum - Colorblind (feat. Halvorsen) [NCS Release]",
        "fileExtension": "mp3",
        "title": "Colorblind (feat. Halvorsen)",
        "artist": "Netrum",
        "uploaderId": "NoCopyrightSounds",
        "uploadDate": "14/04/2020",
        "isNew": "true",
        "playlistIds": ["PLCVGGn6GhhDu_4yn_9eN3xBYB4POkLBYT"]
    }
]
```

## `/music/<identifier>`
### `POST`  
- Rename a musics title and artist.  
*Body*  
```json
{
    "title": "Riddle",
    "artist": "Bad Computer",
    "isNew": "false"
}
```

## `/naming-rules`
### `GET`  
Returns the list of rules. These rules are applied in order to help to determine the title and artist from the name of the video.  
It is useful to remove strings like " (Official Video)" or handle special characters.   
*Response*  
```json
[
    {
        "id": "0",
        "replace": "‒",
        "replace_by": "-",
        "priority": "1"
    },
    {
        "id": "1",
        "replace": "u00e9",
        "replace_by": "é",
        "priority": "2"
    },
    {
        "id": "2",
        "replace": " [Monstercat Release]",
        "replace_by": "",
        "priority": "2"
    }
]
```
### `POST`
*Body*  
Add a new rule with the following parameters :  
    - `replace` *(string to replace)*.  
    - `replace_by` *(new string that replaces old)*.  
    - `priority` *(in what order should the rules apply relatively to other rules, lowest number will apply first. Naming rules occur before naming format.)*  
```json
{
    "replace": "‒",
    "replace_by": "-",
    "priority": "1"
}
```

## `/naming-rules/<identifier>`
### `GET`  
Get the specified rule.
*Response*  
```json
{
    "replace": "‒",
    "replace_by": "-",
    "priority": "1"
}
```
### `POST`
Edit the specified rule.  
*Body*  
```json
{
    "replace": "‒",
    "replace_by": "-",
    "priority": "1"
}
```
### `DELETE`
Delete the specified rule.  

## `/uploaders`
### `GET`  
Get the list of channels registered in the database. For each channel there is a naming format that applies.  
The first time that a video is downloaded from a channel, the channel is automatically registered.  
*Response*  
```json
[
    {
        "id": "sdfkjgnsower",
        "name": "Monstercat: Uncaged",
        "namingFormat": {
            "separator": " - ",
            "artist_before_title": "false"
        }
    },
    {
        "id": "pmwsfbvhgbvsfdgsdoq",
        "name": "Pegboard Nerds",
        "namingFormat": {
            "separator": " - ",
            "artist_before_title": "false"
        }
    }
]
```

## `/uploaders/<identifier>`
### `GET`  
Get the specified channel.  
*Response*  
```json
{
    "id": "sdfkjgnsower",
    "name": "Monstercat: Uncaged",
    "namingFormat": {
        "separator": " - ",
        "artist_before_title": "false"
    }
}
```
### `POST`
Edit the naming format for the specified channel.  
*Body*  
```json
{
    "separator": " - ",
    "artist_before_title": "true"
}
```

