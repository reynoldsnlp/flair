# if reynoldsnlp/flair-base image does not exist, build it
docker inspect reynoldsnlp/flair-base 2> /dev/null \
	|| docker build -t reynoldsnlp/flair-base -f Dockerfile_base .
docker build -t reynoldsnlp/flair --no-cache -f Dockerfile .

# if `flair` container is already running, stop/rm the old one
docker inspect flair 2> /dev/null \
	&& docker stop flair \
	&& docker rm flair

# run the `flair` container
docker run -dit -p 8080:8080 -e BING_API=$BING_API --name flair --restart always reynoldsnlp/flair
