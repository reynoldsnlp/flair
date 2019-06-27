
# Make and change to `lib` directory
mkdir src/main/webapp/WEB-INF/lib
pushd src/main/webapp/WEB-INF/lib

echo "Downloading tregex files from Google Drive..."
wget --no-check-certificate -O stanford-tregex-3.6.0-javadoc.jar https://docs.google.com/uc?export=download&id=1zKBMKJ49iZchuLWFCuqLXHNIazQKrFpy
wget --no-check-certificate -O stanford-tregex-3.6.0-sources.jar https://docs.google.com/uc?export=download&id=1-1wxv-S4LLMf5VEHzEwu3qfuDl5N9Wp2
wget --no-check-certificate -O stanford-tregex-3.6.0.jar         https://docs.google.com/uc?export=download&id=1xM-1EWl2sUau4gpu_cxQrN30YR40smgD
# echo "Installing tregex in maven repository..."
# mvn install:install-file -Dfile=<stanford-tregex-3.6.0.jar> -DgroupId=edu.stanford.nlp -DartifactId=stanford-tregex -Dversion=3.6.0 -Dpackaging=jar


echo "Downloading Russian CoreNLP models from Google Drive..."
wget --no-check-certificate -O stanford-russian-corenlp-models.jar https://docs.google.com/uc?export=download&id=1_0oU8BOiYCqHvItSsz0BjJnSNp8PRWlC
# echo "Installing Russian CoreNLP models in maven repository..."
# mvn install:install-file -Dfile=<stanford-russian-corenlp-models.jar> -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp-russian-models -Dversion=master-SNAPSHOT -Dpackaging=jar
