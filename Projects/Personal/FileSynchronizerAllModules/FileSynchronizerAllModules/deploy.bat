@echo off

pushd ..\..\FileSynchronizerClient\FileSynchronizerClient
call gradlew.bat fatJar sourcesJar --console=plain
popd

pushd ..\..\FileSynchronizerServer\FileSynchronizerServer
call gradlew.bat fatJar sourcesJar --console=plain
popd
