See [Wiki](https://bitbucket.org/datatoknowledge/nlpservice/wiki)


NLP Service
--------------

## Dependecies

1. mongodb
2. elasticsearch

### MongoDB Collections

#### crimes collection
the crimes collection contains is a dictionary of token and type (crime,related) which are used
to detect news related to crimes.
- In order to load the collection type the following command

```
mongoimport -d wheretolive -c crimes --type csv --file crimesList.csv --headerline -u administrator -p password

```
- add a text index on attribute name

```
can anyone add the command?
```
