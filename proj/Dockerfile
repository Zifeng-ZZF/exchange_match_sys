FROM gradle:6.3.0-jdk14
RUN mkdir /deploy

WORKDIR /deploy/
ADD . /deploy/
RUN gradle build -x test

ADD . /deploy
EXPOSE 12345
ENTRYPOINT ["gradle", "run"]
	  
