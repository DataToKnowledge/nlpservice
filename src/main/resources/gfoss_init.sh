#!/bin/sh

curl -XPUT 'http://localhost:9200/geodata' -d '
{
     "settings": {
        "analysis": {
            "filter": {
                "italian_elision": {
                    "type": "elision",
                    "articles": [
                        "c",
                        "l",
                        "all",
                        "dall",
                        "dell",
                        "nell",
                        "sull",
                        "coll",
                        "pell",
                        "gl",
                        "agl",
                        "dagl",
                        "degl",
                        "negl",
                        "sugl",
                        "un",
                        "m",
                        "t",
                        "s",
                        "v",
                        "d"
                    ]
                },
                "italian_stop": {
                    "type": "stop",
                    "stopwords": "_italian_"
                },
                "italian_stemmer_light": {
                    "type": "stemmer",
                    "language": "light_italian"
                },
                "italian_keywords": {
                    "type": "keyword_marker",
                    "keywords": [""]
                }
            },
            "analyzer": {
                "my_italian": {
                    "type": "custom",
                    "tokenizer": "standard",
                    "filter": [
                        "italian_elision",
                        "lowercase",
                        "italian_stop",
                        "italian_stemmer_light"
                    ],
                    "char_filter": ["html_strip"]
                },
                "my_italian_icu": {
                    "type": "custom",
                    "tokenizer": "icu_tokenizer",
                    "filter": [
                        "italian_elision",
                        "lowercase",
                        "italian_stop",
                        "italian_stemmer_light"
                    ],
                    "char_filter": ["html_strip"]
                },
                "my_url": {
                    "type": "custom",
                    "tokenizer": "uax_url_email",
                    "filter": [],
                    "char_filter": []
                },
                "my_path": {
                    "type": "custom",
                    "tokenizer": "path_hierarchy",
                    "filter": [],
                    "char_filter": []
                }
            }
        }
    }
}'

curl -XPUT 'http://localhost:9200/geodata/gfoss/_mapping?ignore_conflicts=true' -d '
{
    "gfoss" : {
        "properties" : {
            "id" : {"type" : "integer"},
            "istat_id": { "type": "string"},
            "city_name": {"type" : "string", "analyzer": "my_italian_icu"},
            "province_id": {"type" : "string"},
            "province_name": {"type": "string","analyzer": "my_italian_icu"},
            "region_id": {"type" : "string"},
            "region_name": {"type": "string","analyzer": "my_italian_icu"},
            "population": {"type": "integer"},
            "source_id": {"type": "integer"},
            "wikipedia_url": {"type": "string", "analyzer": "my_url"},
            "geoname_url": {"type": "string", "analyzer": "my_url"},
            "geo_location": {"type":"geo_point"}
        }
    }
}'


