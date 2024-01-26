# QmkMusicManager
QMK Music Manager is a REST API to download and manage music. It is a Kotlin application packaged in a jar file.  
[Here](https://github.com/hQamonky/QmkMusicManager/blob/master/docs/Api%20User%20Guide.md)
is the API reference and list of features. Once the server is up and running, you can see the documentation and use the API through *serverip*:*port*/swagger-ui/index.html.
## Requirements
- You need to have java 11 installed in order to run the application.  
- You also need to have [youtube-dlp](https://github.com/yt-dlp/yt-dlp/) installed. It is recommended to install using the release binaries.  
- youtube-dlp do not work without Python, so you will need that as well.
- You need an [AccoustID](https://acoustid.org/) API key. See below section "How to set AccoustID API key".

### How to install youtube-dlp on linux
``` shell
sudo curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp
sudo chmod a+rx /usr/local/bin/yt-dlp
```

Alternatively you can create a docker image and run it in a container (the container will need java 11, youtube-dl and youtube-dlp installed).
You can use the Dockerfile and docker-compose.yml from this project.
### How to set AccoustID API key
#### Get an API key
First of all you have to get an API key from [the AccoustID website](https://acoustid.org/).
Create an account and click on ["Register your application"](https://acoustid.org/new-application).
Fill in the fields, click on "Register", and you will gat your API key.
#### Set the API key
Now you need to set the API key in QMK MusicManager. 
To do so, create a file "data/accoustidapikey" in the folder where you run QMK MusicManager.
Put your api key inside it, without anything else in it.
Alternatively you can also set it via the `/settings/accoustid-api-key` endpoint once QMK MusicManager is running.
## Installation
There is an installation script for linux that creates a systemd service.  
For other operating systems, you will have to use docker or install it manually (see below).
### Using the installation script
#### Download using the terminal
``` shell
wget -P ~/Downloads https://raw.githubusercontent.com/hQamonky/QmkMusicManager/master/installers/linux_installer.sh
chmod +x ~/Downloads/linux_installer.sh
sudo ~/Downloads/linux_installer.sh -u qmk
```
For the last command, replace "qmk" by your username.  
The script must run as sudo because is uses systemctl commands.  
You can also download the script through a web browser by visiting [this page](https://raw.githubusercontent.com/hQamonky/QmkMusicManager/master/installers/linux_installer.sh),
right-clicking anywhere on the page and clicking "Save Page As...".
### Manual installation
Download the jar package from [here](https://github.com/hQamonky/QmkMusicManager/releases/download/v1.0.10/musicmanager-1.0.10.jar).
Then you can run it with `java -jar path/to/musicmanager-*.jar`.  

## Service Management
### If installed on linux by using the linux_installer.sh script
linux_installer.sh configures a systemd service.  
Here are the commands to manage the service :  
**Start service**  
`sudo systemctl start qmk_music_manager.service`  
**Stop service**  
`sudo systemctl stop qmk_music_manager.service`  
**Restart service**  
`sudo systemctl restart qmk_music_manager.service`  
**Get service status**  
`sudo systemctl status qmk_music_manager.service`  
**Enable service to run at startup**  
*Note : service is already enabled by the linux_installer.sh*  
`sudo systemctl enable qmk_music_manager.service`  
**Disable service running at startup**  
`sudo systemctl disable qmk_music_manager.service`  
