# Exchange Match Engine - 568 HW3 

## Introduction
This project is the thrid homeword of 568 enterprise robust server. A server is implemented for handling concurrent request for selling and buying commodities and manage order and accouts with relational database. Thread pool is used for handling request concurency and connection pool is used to handle database concurrency. MyBatis framework is used to access database and reduce complexity. 
There is also a test infrastructure for testing the functionalities and scalability of the server under different server core configurations. For the functionalities tests, the example requests used to test the functionalities and their expected results are put int the file "testInfra/testcases_results.txt", you may see the compare the result to decide the correctness. For the scalability test, different number of client are used to send multiple concurrent random requests. 

## Structure

The root directory includes two projects: one for server, one for testing.

erss-hwk3-zz204-yl730 (project root)

   |- docker-compose.yml

   |- README.md

   |- report.pdf

   |- proj

   |- |- init.sql 

   |- |- Dockerfile

   |- |- gradle 

   |- |- build.gradle 

   |- |- src 

   |- |- |- test

   |- |- |- main

   |- |- |- |- java
   
   |- |- |- |- |- exchange_match_egine

   |- |- |- |- |- |- codes.java

   |- |- |- |- resources

   |- |- |- |- |- mappers.xml

   |- |- |- |- |- mybatis-config.xml

   |

   |- testInfra

   |- |- gradle 

   |- |- build.gradle 

   |- |- testcases_results.txt

   |- |- src 

   |- |- |- test

   |- |- |- main

   |- |- |- |- java
   
   |- |- |- |- |- testInfra

   |- |- |- |- |- |- codes.java



## Usage 
### server:
- sudo docker-compose up
- checking DB via pgAdmin connecting to the host on 5432 (the postgres container's port has been mapped to the host port), with name and password "postgres" and db name "exchange_db".
- closing: sudo docker-compose down --volumes

### client:
You will need 6.3 gradle on your machine to work:
- curl -s "https://get.sdkman.io" | bash
- source "$HOME/.sdkman/bin/sdkman-init.sh"
- sudo sdk install gradle 6.3
Then run the testing clients for either functionalities test or scalability test
- cd testInfra/
- gradle build -x test
#### functionalities:
- change the host in the App.java "static String host = ..."
- gradle run --args='1'
- check the testcases and expected results in "testInfra/testcases_results.txt"
#### scalability:
- gradle run --args='2 100'
- the second arg is the number of requests to send
- find more details in the "report.pdf"

## Reproduce results
### For the first graph in the report:
Single core:
#### in server: 
- sudo taskset --cpu-list 0 docker-compose up
- sudo taskset --cpu-list 0,1 docker-compose up
- sudo taskset --cpu-list 0-2 docker-compose up
- sudo taskset --cpu-list 0-3 docker-compose up
#### in client
For each of the above server settings, do
- gradle run --args='2 1000'
Then you can see the duration of execution printed out in the terminal



### For the second graph:
#### in server: 
- sudo taskset --cpu-list 0-3 docker-compose up
#### in client
- gradle run --args='2 100'
- gradle run --args='2 300'
- gradle run --args='2 650'
- gradle run --args='2 1000'
Then you can see the duration of execution printed out in the terminal


## Authors and acknowledgment 
- Zifeng Zhang (zz204@duke.edu)
- Yijia Li (yl730@duke.edu)

