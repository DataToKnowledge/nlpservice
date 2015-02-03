#!/bin/sh

curl -XPUT 'http://localhost:9200/wheretolive' -d '
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

curl -X PUT 'http://localhost:9200/wheretolive/news/_mapping?ignore_conflicts=true' -d '
{
  "news": {
    "_all": {
      "enabled": true,
      "analyzer": "my_italian_icu"
    },
    "properties": {
      "newspaper": {
        "type": "string",
        "include_in_all": false,
        "analyzer": "keyword"
      },
      "urlWebsite": {
        "type": "string",
        "include_in_all": false,
        "analyzer": "my_url"
      },
      "urlNews": {
        "type": "string",
        "include_in_all": false,
        "analyzer": "my_url"
      },
      "imageLink": {
        "type": "string",
        "include_in_all": false,
        "index": "not_analyzed"
      },
      "title": {
        "type": "string",
        "include_in_all": true,
        "score": 3,
        "analyzer": "my_italian_icu"
      },
      "summary": {
        "type": "string",
        "include_in_all": true,
        "score": 2,
        "analyzer": "my_italian_icu"
      },
      "corpus": {
        "type": "string",
        "include_in_all": true,
        "score": 1,
        "analyzer": "my_italian_icu"
      },
      "focusDate": {
        "type": "date",
        "format": "basic_date||basic_date_time||date||date_time||dd-MM-yyyy||dd//MM/yyyy||dd-MM-yyyy HH:mm",
        "include_in_all": false
      },
      "focusLocation": {
        "type": "nested",
        "properties": {
          "city_name": {
            "type": "string",
            "include_in_all": false,
            "analyzer": "keyword"
          },
          "province_name": {
            "type": "string",
            "include_in_all": false,
            "analyzer": "keyword"
          },
          "region_name": {
            "type": "string",
            "include_in_all": false,
            "analyzer": "keyword"
          },
          "population": {
            "type": "integer",
            "include_in_all": false,
            "analyzer": "keyword"
          },
          "wikipedia_url": {
            "type": "string",
            "include_in_all": false,
            "analyzer": "keyword"
          },
          "geoname_url": {
            "type": "string",
            "include_in_all": false,
            "analyzer": "keyword"
          },
          "position": {
            "type": "geo_point",
            "geohash": true,
            "lat_lon": true,
            "index_name": "position",
            "fielddata": {
              "format": "compressed",
              "precision": "3m"
            },
            "include_in_all": false
          }
        }
      },
      "namedEntities": {
        "type": "nested",
        "properties": {
          "crimes": {
            "type": "string",
            "index_name": "crime",
            "include_in_all": true,
            "score": 6,
            "analyzer": "keyword"
          },
          "addresses": {
            "type": "string",
            "index_name": "address",
            "include_in_all": true,
            "score": 6,
            "analyzer": "keyword"
          },
          "persons": {
            "type": "string",
            "index_name": "person",
            "include_in_all": true,
            "score": 6,
            "analyzer": "keyword"
          },
          "locations": {
            "type": "string",
            "index_name": "location",
            "include_in_all": true,
            "score": 6,
            "analyzer": "keyword"
          },
          "geopoliticals": {
            "type": "string",
            "index_name": "geopolitical",
            "include_in_all": true,
            "score": 6,
            "analyzer": "keyword"
          },
          "organizations": {
            "type": "string",
            "index_name": "organization",
            "include_in_all": true,
            "score": 6,
            "analyzer": "keyword"
          }
        }
      },
      "tags": {
        "type": "nested",
        "properties": {
          "name": {
            "type": "string",
            "include_in_all": false,
            "score": 6,
            "analyzer": "keyword"
          },
          "rank": {
            "type": "double",
            "null_value": 1,
            "include_in_all": false,
            "index": "not_analyzed"
          }
        }
      }
    }
  }
}'

