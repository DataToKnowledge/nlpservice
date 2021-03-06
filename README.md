See [Wiki](https://bitbucket.org/datatoknowledge/nlpservice/wiki)

NLP Service
-----------

Dependecies
-----------

1.	mongodb docker container with name mongodb
2.	elasticsearch docker container with name elastic1

##[Production] Init

1.	create the app -> dokku create nlpservice
2.	create the link for elasticsearch -> dokku link:create nlpservice elastic1 elastic1
3.	create the link for mongodb -> dokku link:create nlpservice mongodb mongodb
4.	add [Environment variable management](Environment variable management).

```
    dokku config:set nlpservice DOKKU_BUILDSTEP_IMAGE=wheretolive/dokku-alt-buildstep:cedar-14

```

### MongoDB Collections

#### crimes collection

the crimes collection contains is a dictionary of token and type (crime,related) which are used to detect news related to crimes. - In order to load the collection type the following command

```
mongoimport -d wheretolive -c crimes --type csv --file crimesList.csv --headerline -u administrator -p password

```

##[Production] Deploy

1.	create the dokku branch -> git remote add dokku dokku@datatoknowledge.it:nlpservice
2.	check [system.properties for JDK 1.8](https://devcenter.heroku.com/articles/scala-support#specifying-a-java-version)
3.	push to the branch -> git push dokku develop:master
