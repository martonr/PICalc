@echo off

set gui_module=com.github.martonr.picalc.gui
set cli_module=com.github.martonr.picalc.cli
set eng_module=com.github.martonr.picalc.engine

rem Create custom JVM for the GUI
jlink ^
--module-path ".\\target" ^
--add-modules java.base,javafx.controls,javafx.fxml,%eng_module%,%gui_module% ^
--add-options "-Xms512M -XX:+UseZGC -XX:+UnlockExperimentalVMOptions -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -XX:+PerfDisableSharedMem -XX:-UsePerfData -XX:ZFragmentationLimit=20 -XX:SurvivorRatio=32 -XX:MaxGCPauseMillis=200 -XX:MaxTenuringThreshold=1 -XX:ParallelGCThreads=8 -XX:ConcGCThreads=4 --add-exports=javafx.controls/com.sun.javafx.charts=%gui_module% -Djavafx.cachedir=./.openjfx/cache" ^
--vm server ^
--no-header-files ^
--no-man-pages ^
--strip-debug ^
--strip-java-debug-attributes ^
--compress=2 ^
--launcher run=%gui_module% ^
--output ".\\target\\jvm_gui"

rem Copy run script
xcopy .\run_gui.bat .\target\jvm_gui\ /K

rem Create custom JVM for the CLI
jlink ^
--module-path ".\\target" ^
--add-modules java.base,javafx.controls,javafx.fxml,%eng_module%,%cli_module% ^
--add-options "-Xms512M -XX:+UseZGC -XX:+UnlockExperimentalVMOptions -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -XX:+PerfDisableSharedMem -XX:-UsePerfData -XX:ZFragmentationLimit=20 -XX:SurvivorRatio=32 -XX:MaxGCPauseMillis=200 -XX:MaxTenuringThreshold=1 -XX:ParallelGCThreads=8 -XX:ConcGCThreads=4" ^
--vm server ^
--no-header-files ^
--no-man-pages ^
--strip-debug ^
--strip-java-debug-attributes ^
--compress=2 ^
--launcher run=%cli_module% ^
--output ".\\target\\jvm_cli"

rem Copy run script
xcopy .\run_cli.bat .\target\jvm_cli\ /K

echo Done.