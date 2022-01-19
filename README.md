# WayinIE
A set of tools and scripts to help make Wayin shareable.

**Prerequisites**
* Java 8 (JRE 1.8)
* Python 3.x
* Android build-tools 31.0.0 (or apk signing tools)

**Procedure**  
After making sure you have all the required files, head to **binaries\mine\decompile.bat** and run it.  
Wait till it finishes and decompiles **app-debug.apk** to a side directory, **app-debug**.  
Copy that path and go to **WayinInjector\paths.ini**.  
Edit it with some text editor and replace the value for entry ie_path with the clipboard content.  
Now, go to **binaries\decompile\download.ps1**, right-click on it and select **Run with PowerShell**, accept the execution policy by hitting **Y**.  
Make sure the script downloads latest **wayin.apk** to the currect directory.  
Now run **decompile.bat** and wait for it to finish.  
Copy the path of the new folder, **wayin**, and go back to **paths.ini**, now setting the value of host_path to this path.  
Save **paths.ini**.  
Run **main.py** in WayinInjector. If you have configured paths correctly, things should go smooth.  
Head back to **binaries\decompile\compile.bat** and run it. Wait till it builds the new **wayin_mod.apk** file.  
All left now is signing the apk. Edit **sign.bat** so that it references the necessary files on your machine i.e. **apksigner.bat** from (android build-tools) and your custom key store file (create one if haven't already)  
All set, you should be good to go now! You can install the signed apk.  

**Disclaimer**  
I will not be held responsible for any of the consequences of such tampering especially that I am providing the source of the ImportExport whose modification can be dangerous and a liability.
