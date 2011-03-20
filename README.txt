To Install:

0. Download the zip containing plugin and hsqldb.

1. Unzip file into same directory that your craftbukkit.jar exists in. This should put 
   the hsqldb.jar into the lib/ folder, the WormholeXTreme.jar into the plugins/ folder,
   and the latest versions of GateShapes into the plugins/WormholeXTreme/GateShapes/ folder.
   
2. start server

3.1 (Optional) Stop the server and edit the newly generated /plugins/WormholeXTreme/settings.txt 
   file as needed.
   
3.2 (Optional) If using the Permissions plugin based plugin, set up the appropriate permissions.
   If using SIMPLE_PERMISSIONS = true remember to use permissions from the Simple Mode list, 
   otherwise use nodes from Complex Node.
   
3.2 (Optional) Start Server again


To Update:

0. Download the updated JAR.

1. Rename file to WormholeXTreme.jar and place in your plugins/ folder, overwriting existing file.

2. If upgrading from v0.750 or earlier either remove or update shapes in 
   plugins/WormholeXTreme/GateShapes/ to the latest specifications.
   
3. If hsqldb.jar exists in minecraft/ folder and does not exist in minecraft/lib/, move it into 
   minecraft/lib/ (or NPE).
   
4. If upgrading from v0.750 or earlier, CHANGE YOUR SETTINGS FOR WORMHOLE COST TO DOUBLE VALUES in 
   plugins/WormholeXTreme/settings.txt. I.E. 0.0 or 1.0 or 10.0!!! OR CRASH!
   
5. Start server.

6. If upgrading from v0.750 or earlier, Remove a gate with '/wxremove <gatename>' then re-add the gate. 



Permissions Plugin Nodes: (Controlled by plugins/WormholeXTreme/settings.txt - SIMPLE_PERMISSIONS)

    SIMPLE_PERMISSIONS = false
    Complex Mode:
    wormhole.use.sign - lets a user use sign gates.
    wormhole.use.dialer - lets a user use '/dial' gates
    wormhole.use.compass - lets a user use '/wxcompass' command.
    wormhole.remove.own - lets a user remove a gate that they own
    wormhole.remove.all - lets a user remove any gate
    wormhole.build - Able to build new wormholes
    wormhole.config - Able to configure settings like material and timeout
    wormhole.list - Able to use '/wxlist' command to list wormholes
    wormhole.network.use.NETWORKNAME - Able to use wormholes on NETWORKNAME
    wormhole.network.build.NETWORKNAME - Able to build wormholes on NETWORKNAME
    wormhole.go - allows user to use '/wxgo' command.

    SIMPLE_PERMISSIONS = true
    Simple Mode:
    wormhole.simple.use - Lets a user use wormholes.
    wormhole.simple.build - Lets a user build wormholes.
    wormhole.simple.remove - Lets a user remove wormholes.
    wormhole.simple.config - Lets a user configure WormholeXTreme settings


To Build:

-1. Install Maven and all of its dependencies. 

 0. Git clone the repo. (Or download the source zip)
 
 1. Download iConomy version 4.4. Git pull Permissions, use tag 2.5.4. Do a mvn install for Permissions.
 
 2. Force iConomy into your local repo. 
 
 2.1 (example commands) 'mvn install:install-file -Dfile=iConomy.jar -DgroupId=com.nijiko.coelho.iConomy \
     -DartifactId=iConomy -Dversion=4.4 -Dpackaging=jar -DgeneratePom=true'
     
 2.2 iConomy location: http://forums.bukkit.org/threads/40/
 
 3. Run a 'mvn install' in the directory you ran the git clone into (or unpacked the source zip).
  
 4. Enjoy plugin that is now in target/. It will be a jar or a package zip.   