To Install:
0. Download the zip containing plugin and hsqldb.
1. Put /plugins/WormholeXTreme.jar in your /minecraft/plugins/ folder.
2. This plugin uses HSQLDB to store stargates - so put /lib/hsqldb.jar in /minecraft/lib/
3. GO! (start server)
3.1 (Optional) STOP! (... stop server?)
3.2 (Optional) Edit gate shapes in plugins/WormholeXTreme/GateShapes/ folder.
3.3 (Optional) GO! (Again)
4. Ignore /src/ and /doc/ folders unless you have interest in the WormholeXTreme project from a programming standpoint.


To Update:
0. Download the updated JAR.
1. Put WormholeXTreme.jar in your plugins/ folder, overwriting existing file.
2. If you have a plugins/WormholeXTreme/GateShapes/ folder, either remove it, or update shapes to the latest specifications.
4. Move hsqldb.jar from your /minecraft/ folder into /minecraft/lib/.
5. Start server.
5.1 (Optional) Stop Server if you deleted your plugins/WormholeXTreme/GateShapes/ folder.
5.2 (Optional) Edit Gate Shapes in plugins/WormholeXTreme/GateShapes/
5.3 (Optional) Start Server.
6. Remove a gate with /wormhole remove <gatename>
7. Re-add gate. 
8. CHANGE YOUR SETTINGS FOR WORMHOLE COST TO DOUBLE VALUES. I.E. 0.0 or 1.0 or 10.0!!! OR CRASH!
9. Enjoy. 

Permissions Plugin Nodes:

wormhole.use.sign - lets a user use sign gates.
wormhole.use.dialer - lets a user use /dial gates
wormhole.use.compass - lets a user use /wxcompass (/wormhole compass)
wormhole.remove.own - lets a user remove a gate that they own
wormhole.remove.all - lets a user remove any gate
wormhole.build - Able to build new wormholes
wormhole.config - Able to configure settings like material and timeout
wormhole.list - Able to list wormholes
wormhole.network.use.NETWORKNAME - Able to use wormholes on NETWORKNAME
wormhole.network.build.NETWORKNAME - Able to build wormholes on NETWORKNAME

To Build:

-1. Install Maven and all of its dependencies. 
 0. Git clone the repo. (Or download the source zip)
 1. Download iConomy version 4.3. Download Permissions version 2.5.3 or greater.
 2. Force iConomy and Permissions into your local repo. 
 2.1 (example commands) mvn install:install-file -Dfile=Permissions.jar -DgroupId=com.nijikokun.bukkit.Permissions -DartifactId=Permissions -Dversion=2.5.3 -Dpackaging=jar -DgeneratePom=true
   - http://forums.bukkit.org/threads/5974/
 2.2 (example commands) mvn install:install-file -Dfile=iConomy.jar -DgroupId=com.nijiko.coelho.iConomy -DartifactId=iConomy -Dversion=4.3 -Dpackaging=jar -DgeneratePom=true
   - http://forums.bukkit.org/threads/40/
 3. Run a 'mvn install' in the directory you ran the git clone into (or unpacked the source zip). 
 4. Enjoy plugin that is now in target/. It will be a jar, a zip, and a tar.bz2.   