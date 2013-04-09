rm -r SimLab
mkdir SimLab
javac @BMV/compileList -d ./SimLab/
cp ./BMV/Run.sh ./SimLab/Run.sh
cp -r ./BMV/Resources ./SimLab/Resources
cp -r ./BMV/Models ./SimLab/Models
cp -r ./BMV/ScreenCaptures ./SimLab/ScreenCaptures
cp ./BMV/Tutorial.* ./SimLab/
cd SimLab
chmod 755 Run.sh
jar cmf ../BMV/BMV.mf SimLab.jar bmv/
rm ./Resources/CycloneSource/*.o
cd ..
rm SimLab.tgz
rm SimLab.zip
tar -czf SimLab.tgz SimLab/
zip -r -q SimLab.zip SimLab/
echo Done making jar

