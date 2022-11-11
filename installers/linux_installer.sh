#!/bin/bash

username=$USER
installDir=/opt/qmk

Help()
{
   # Display Help
   echo "Description: Install QMK Music Manager as a systemd daemon."
   echo
   echo "Syntax: linux_installer.sh [-h|u|d]"
   echo "options:"
   echo "h     Print this help."
   echo "u     User used to run the installed service. Default user is $username."
   echo "d     Directory where the application will be installed. Default location is $installDir/."
   echo
}

while getopts :h:u:d: flag
do
    case "${flag}" in
        h) Help
          exit;;
        u) username=${OPTARG};;
        d) installDir=${OPTARG};;
        *) echo "Invalid option."
          echo
          Help
          exit;;
    esac
done

echo "Service will run under $username user.";
echo "Will install package to $installDir.";

echo "Creating installation directory..."
mkdir -p "$installDir"

echo "Fetching application..."
wget -P "$installDir" https://raw.githubusercontent.com/hQamonky/QmkMusicManager/master/package/musicmanager-1.0.1.jar

echo "Creating service..."
serviceFile=/etc/systemd/system/qmk_music_manager.service
echo "[Unit]" > $serviceFile
echo "Description=Download and manage music from youtube." >> $serviceFile
echo "After=network.target" >> $serviceFile
echo >> $serviceFile
echo "[Service]" >> $serviceFile
echo "User=$username" >> $serviceFile
echo "WorkingDirectory=$installDir" >> $serviceFile
echo "ExecStart=java -jar $installDir/musicmanager-1.0.1.jar" >> $serviceFile
echo "Restart=always" >> $serviceFile
echo >> $serviceFile
echo "[Install]" >> $serviceFile
echo "WantedBy=multi-user.target" >> $serviceFile

systemctl daemon-reload
systemctl enable qmk_music_manager.service
systemctl start qmk_music_manager.service

echo "Installation finished."

