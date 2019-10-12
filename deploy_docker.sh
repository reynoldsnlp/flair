docker build -t reynoldsnlp/flair-2.0 .
docker run -dit -p 8080:8080 -e BING_API=$BING_API --name flair --restart always reynoldsnlp/flair-2.0
