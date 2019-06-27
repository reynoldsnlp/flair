
# from github user vladalive [here](https://gist.github.com/iamtekeste/3cdfd0366ebfd2c0d805)
function gdrive_download () {
  CONFIRM=$(wget --quiet --save-cookies /tmp/g_cookie.txt --keep-session-cookies --no-check-certificate "https://docs.google.com/uc?export=download&id=$1" -O- | sed -rn 's/.*confirm=([0-9A-Za-z_]+).*/\1\n/p')
  wget --load-cookies /tmp/g_cookie.txt "https://docs.google.com/uc?export=download&confirm=$CONFIRM&id=$1" -O $2
  rm -rf /tmp/g_cookie.txt
}

MINSIZE=5000  # unsuccessful downloads are usually ~3281B

# Make and change to `lib` directory
mkdir src/main/webapp/WEB-INF/lib
pushd src/main/webapp/WEB-INF/lib

echo "Downloading tregex files from Google Drive..."
if (( $(stat -c%s stanford-tregex-3.6.0-javadoc.jar) < MINSIZE ))
then
	gdrive_download 1zKBMKJ49iZchuLWFCuqLXHNIazQKrFpy stanford-tregex-3.6.0-javadoc.jar
else
	echo "stanford-tregex-3.6.0-javadoc.jar is already downloaded."
fi

if (( $(stat -c%s stanford-tregex-3.6.0-sources.jar) < MINSIZE ))
then
	gdrive_download 1-1wxv-S4LLMf5VEHzEwu3qfuDl5N9Wp2 stanford-tregex-3.6.0-sources.jar
else
	echo "stanford-tregex-3.6.0-sources.jar is already downloaded."
fi

if (( $(stat -c%s stanford-tregex-3.6.0.jar) < MINSIZE ))
then
	gdrive_download 1xM-1EWl2sUau4gpu_cxQrN30YR40smgD stanford-tregex-3.6.0.jar
else
	echo "stanford-tregex-3.6.0.jar is already downloaded."
fi

echo "Installing tregex in maven repository..."
mvn install:install-file -Dfile=stanford-tregex-3.6.0-javadoc.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-tregex-javadoc -Dversion=3.6.0 -Dpackaging=jar
mvn install:install-file -Dfile=stanford-tregex-3.6.0-sources.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-tregex-sources -Dversion=3.6.0 -Dpackaging=jar
mvn install:install-file -Dfile=stanford-tregex-3.6.0.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-tregex -Dversion=3.6.0 -Dpackaging=jar


echo "Downloading Russian CoreNLP models from Google Drive..."
if (( $(stat -c%s stanford-tregex-3.6.0.jar) < MINSIZE ))
then
	gdrive_download 1_0oU8BOiYCqHvItSsz0BjJnSNp8PRWlC stanford-russian-corenlp-models.jar
echo "Installing Russian CoreNLP models in maven repository..."
mvn install:install-file -Dfile=stanford-russian-corenlp-models.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp-russian-models -Dversion=master-SNAPSHOT -Dpackaging=jar
popd
