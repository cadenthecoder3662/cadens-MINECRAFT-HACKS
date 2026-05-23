1
Install Java 17 one time
Go to adoptium.net — pick Windows, x64, JDK 17, download the .msi and run it.
2
Install Fabric for Minecraft 1.21.4 one time
Go to fabricmc.net/use/installer — download the Windows .exe, open it, set version to 1.21.4, click Install.
3
Download Fabric API one time
Go to modrinth.com — click the download icon on the first result. Save the .jar file.
Build the mod
4
Extract the zip
Right-click CadensHacks3-source.zip → Extract All → choose C:\CadensHacks
5
Run BUILD.bat
Open C:\CadensHacks\OreRadar and double-click BUILD.bat. Wait for it to say SUCCESS. First run downloads Gradle and takes a few minutes.
6
Find your .jar
It appears at C:\CadensHacks\OreRadar\build\libs\CadensHacks3-3.0.0.jar
Install into Minecraft
7
Open your mods folder
Press Windows + R, type %appdata%\.minecraft\mods and hit Enter. If no mods folder exists, create one.
8
Drop in both jars
Put CadensHacks3-3.0.0.jar and the fabric-api-xxxx.jar you downloaded in step 3 into the mods folder.
9
Launch with Fabric
Open Minecraft Launcher → click the dropdown next to Play → select fabric-loader-1.21.4 → hit Play. The bottom left should say Minecraft 1.21.4/Fabric.
10
Use the mod done
Join any 
