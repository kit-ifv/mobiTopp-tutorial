echo "Compile mobiTopp Rastatt model."

cd ..
call ./gradlew --refresh-dependencies clean dependencies build

echo "Build finished."
pause