FROM flangelier/scala

RUN wget https://github.com/robertohf/Lenguajes_Scala/archive/master.zip
RUN unzip master.zip

EXPOSE 8080

CMD cd Lenguajes_Scala-master && scala main.scala 