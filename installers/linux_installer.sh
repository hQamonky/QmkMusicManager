#!/bin/bash

username=$USER
installDir=/opt/qmk
workDir=/home/$username/qmk-tmp-workdir

Help()
{
   # Display Help
   echo "Description: Install QMK Music Manager as a systemd daemon."
   echo
   echo "Syntax: linux_installer.sh [-h|u|d|w]"
   echo "options:"
   echo "h     Print this help."
   echo "u     User used to run the installed service. Default user is $username."
   echo "d     Directory where the application will be installed. Default location is $installDir/."
   echo "w     Temporary directory. Default directory is $workDir."
   echo
}

while getopts :h:u:d:w: flag
do
    case "${flag}" in
        h) Help
          exit;;
        u) username=${OPTARG}
          workDir=/home/$username/qmk-tmp-workdir;;
        d) installDir=${OPTARG};;
        w) workDir=${OPTARG};;
        *) echo "Invalid option."
          echo
          Help
          exit;;
    esac
done

echo "Service will run under $username user.";
echo "Will install package to $installDir.";
echo "Temporary directory $workDir will be created (and deleted).";

echo "Creating necessary directories..."
mkdir -p "$installDir"
mkdir -p "$workDir"

echo "Fetching application..."
git -C "$workDir" clone https://github.com/hQamonky/QmkMusicManager.git
cp "$workDir"/QmkMusicManager/packages/musicmanager-1.0.0.jar "$installDir"/musicmanager-1.0.0.jar
rm -rf "$workDir"

echo "Creating service..."
serviceFile=/etc/systemd/system/qmk_music_manager.service
echo "[Unit]" > $serviceFile
echo "Description=Download and manage music from youtube." >> $serviceFile
echo "After=network.target" >> $serviceFile
echo >> $serviceFile
echo "[Service]" >> $serviceFile
echo "User=$username" >> $serviceFile
echo "WorkingDirectory=$installDir" >> $serviceFile
echo "ExecStart=java -jar $installDir/musicmanager-1.0.0.jar" >> $serviceFile
echo "Restart=always" >> $serviceFile
echo >> $serviceFile
echo "[Install]" >> $serviceFile
echo "WantedBy=multi-user.target" >> $serviceFile

systemctl daemon-reload
systemctl enable qmk_music_manager.service
systemctl start qmk_music_manager.service

echo "Installation finished."

