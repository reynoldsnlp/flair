logfile="deploy_prod.out"
rm ${logfile}
touch ${logfile}

echo "Sending output to ${logfile} ..."

# if reynoldsnlp/flair-base image does not exist, build it
docker inspect reynoldsnlp/flair-base 2>&1 >> ${logfile} \
	|| docker build -t reynoldsnlp/flair-base -f Dockerfile_base . 2>&1 >> ${logfile}
docker build -t reynoldsnlp/flair --no-cache -f Dockerfile . 2>&1 >> ${logfile}

# if `flair` container is already running, stop/rm the old one
docker inspect flair 2>&1 >> ${logfile} \
	&& docker stop flair 2>&1 >> ${logfile} \
	&& docker rm flair 2>&1 >> ${logfile}

# run the `flair` container
docker run -dit -p 8080:8080 -e BING_API=$BING_API --name flair --restart always reynoldsnlp/flair 2>&1 >> ${logfile}
