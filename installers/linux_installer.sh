#!/bin/bash

username=$USER
installDir=/opt/qmk
version=1.0.10

Help()
{
   # Display Help
   echo "Description: Install QMK Music Manager as a systemd daemon."
   echo
   echo "Syntax: linux_installer.sh [-h|u|d|v]"
   echo "options:"
   echo "h     Print this help."
   echo "u     User used to run the installed service. Default user is $username."
   echo "d     Directory where the application will be installed. Default location is $installDir/."
   echo "v     Version to install. Default version is $version."
   echo
}

while getopts :h:u:d:v: flag
do
    case "${flag}" in
        h) Help
          exit;;
        u) username=${OPTARG};;
        d) installDir=${OPTARG};;
        v) version=${OPTARG};;
        *) echo "Invalid option."
          echo
          Help
          exit;;
    esac
done

echo "Version $version will be installed.";
echo "Service will run under $username user.";
echo "Will install package to $installDir.";

echo "Creating necessary directories..."
mkdir -p "$installDir"
rm "$installDir"/musicmanager-*.*.*.jar
mkdir -p /home/"$username"/.qmkmusicmanager
chown "$username" /home/"$username"/.qmkmusicmanager

echo "Fetching application..."
wget -P "$installDir" "https://github.com/hQamonky/QmkMusicManager/releases/download/v$version/musicmanager-$version.jar"

echo "Creating service..."
serviceFile=/etc/systemd/system/qmk_music_manager.service
echo "[Unit]" > $serviceFile
echo "Description=QMK Music Manager" >> $serviceFile
echo "After=network.target" >> $serviceFile
echo >> $serviceFile
echo "[Service]" >> $serviceFile
echo "User=$username" >> $serviceFile
echo "WorkingDirectory=/home/$username/.qmkmusicmanager" >> $serviceFile
echo "ExecStart=java -jar $installDir/musicmanager-$version.jar" >> $serviceFile
echo "Restart=always" >> $serviceFile
echo >> $serviceFile
echo "[Install]" >> $serviceFile
echo "WantedBy=multi-user.target" >> $serviceFile

systemctl daemon-reload
systemctl enable qmk_music_manager.service
systemctl start qmk_music_manager.service

echo "Installation finished."

