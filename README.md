# api-jsonstore

Api allowing to store objects serialized as JSON, available to scripting through the $services.jsonstore service. Also contains a simple implementation based on storing files in the XWiki permanent directory.

* Project Lead: [Anca Luca](http://www.xwiki.org/xwiki/bin/view/XWiki/lucaa)
* [Documentation & Downloads] N/A
* [Issue Tracker] N/A
* Communication: [Forum](<https://forum.xwiki.org/>), [Chat](https://dev.xwiki.org/xwiki/bin/view/Community/Chat)
* [Development Practices](http://dev.xwiki.org)
* Minimal XWiki version supported: XWiki 9.11.3
* License: LGPL 2.1
* Translations: N/A
* Sonar Dashboard: N/A
* Continuous Integration Status: N/A

## How to use

All APIs require programming rights.

Save some object as JSON from velocity under a name:

```
$services.jsonstore.permdir.persistAsJson(data, "my id")
```

Getting some JSON stored on disk as an object, for a given name:

```
#set($object = $services.jsonstore.permdir.getFromJsonStore("my id"))
```

Checking if anything is stored for a given id:

```
## will return true in case of failure to actually check the store (exception accessing the store)
$services.jsonstore.permdir.exists("my id", true)

## will return false in case of failure to actually check the store (exception accessing the store)
$services.jsonstore.permdir.exists("my id", false)

```

Ids can be of form ```item1/item2/item3``` .

Remove API not yet added, for the XWiki permdir based implementation all cleanup should be done by accessing the actual server permanent directory. When a new implementation would be added, the API should be enriched with a remove function.
