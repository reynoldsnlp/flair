# if reynoldsnlp/flair-dev-base image does not exist, build it
docker inspect reynoldsnlp/flair-dev-base 2> /dev/null \
	|| docker build -t reynoldsnlp/flair-dev-base -f Dockerfile_dev_base .
docker build -t reynoldsnlp/flair-dev --no-cache -f Dockerfile_dev .

# if `flair-dev` container is already running, stop/rm the old one
docker inspect flair-dev 2> /dev/null \
	&& docker stop flair-dev \
	&& docker rm flair-dev

# run the `flair` container
echo "Running the container 'flair-dev' on port 8081..."
docker run -dit -p 8081:8080 -e BING_API=$BING_API --name flair-dev --restart always reynoldsnlp/flair-dev
