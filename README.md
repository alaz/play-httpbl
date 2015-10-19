## What is it?

This is a Play Framework 2.3.x plugin to test the client's IP address
against [Project Honeypot](http://www.projecthoneypot.org/) database of
suspicious IP addresses (e.g. spammers, email harvesters)

## How to use it?

Just like any other Play Framework plugin (see below "Installation"). You
would also need to specify your Project Honeypot API key in the configuration.

Then, install the filter:

```
object Global extends WithFilters(CSRFFilter(), HttpBLFilter)
```

and all the app responses will have `X-HttpBL` header with e.g. `spammer` or
`searchengine:1` (means Google) or `none` (if this IP isn't listed), etc.

You may also check this information from your controllers:

```
HttpBLRequest(requestHeader)
```

this method returns `Future[HttpBL.Response]` where `HttpBL.Response` is
from [HttpBL](https://github.com/osinka/httpbl) library.

## Installation

In SBT:

```
libararyDependencies += "com.osinka.play" %% "play-httpbl" % "1.0.0-SNAPSHOT"
```

In `play.plugins`:

```
9000:com.osinka.play.httpbl.HttpBLPlugin
```
