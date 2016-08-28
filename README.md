## What is it?

This is a Play Framework 2.5.x plugin to test the client's IP address
against [Project Honeypot](http://www.projecthoneypot.org/) database of
suspicious IP addresses (e.g. spammers, email harvesters)

## How to use it?

Just like any other Play Framework plugin (see below "Installation"). Do not
forget to specify your Project Honeypot API key in the application configuration.

Then, install the filter:

```
object Global extends WithFilters(HttpBLFilter)
```

and all the app responses will have `X-HttpBL` header with e.g. `spammer` or
`searchengine:1` (ID means one of search engines Project Honeypot recognizes)
or `none` (if this IP isn't listed), etc.

You may also check this information from your controllers:

```
HttpBLRequest(requestHeader)
```

this method returns `Future[HttpBL.Response]` where `HttpBL.Response` is
from [HttpBL](https://github.com/osinka/httpbl) library.

## Details

1. There is no caching. DNS resolver is very effective by its own.
2. The request to Http:BL is fired asynchronously, so a controller
   code doesn't need to wait.

## Installation

In SBT:

```
libararyDependencies += "com.osinka.play" %% "play-httpbl" % "2.0.0-SNAPSHOT"
```

In `play.plugins`:

```
9000:com.osinka.play.httpbl.HttpBLPlugin
```

In `application.conf`:

```
httpbl.apiKey = "abcdef"
```
