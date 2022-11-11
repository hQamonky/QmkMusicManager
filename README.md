# QmkMusicManager
QMK Music Manager is a REST API to download and manage music.  
[Here](https://github.com/hQamonky/QmkMusicManager/blob/master/docs/Api%20User%20Guide.md)
is the API reference and list of features.  
It is a Kotlin application packaged in a jar file.
## Requirements
- You need to have java 11 installed in order to run the application.  
- You also need to have [youtube-dl](https://youtube-dl.org/) installed.  

Alternatively you can create a docker image and run it in a container (the container will need java 11 and youtube-dl installed).
## Installation
There is an installation script for linux that creates a systemd service.  
For other operating systems, you will have to install it manually (see below).
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
Download the jar package from [here](https://raw.githubusercontent.com/hQamonky/QmkMusicManager/master/package/musicmanager-1.0.1.jar).
Then you can run it with `java -jar path/to/musicmanager-*.jar`.