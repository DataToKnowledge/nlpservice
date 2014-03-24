## How to initialize lemmatizer database

```
    $ wget http://dev.sslmit.unibo.it/linguistics/downloads/morph-it.tgz
    $ tar xf morph-it.tgz
    $ iconv -f ISO-8859-1 -t UTF-8 current_version/morph-it_048.txt > morph-it-utf8.txt
    $ mongoimport --host 10.1.0.62 --db morph --collection lemmas --type tsv --file morph-it-utf8.txt --fields word,lemma,features
```