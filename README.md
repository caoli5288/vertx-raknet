# vertx-raknet

## Server

```groovy
def server = RakNet.create(Vertx.vertx(), new RakNetOptions())
server.listen "0.0.0.0", 20000, {
    if (it.succeeded()) {
        it.result().connectHandler { session ->
            session.pingHandler { pong -> pong.setInfo("MCPE;MOTD;390;1.14.60;0;10;13253860892328930865;MOTD2;Survival;") }
            session.connectedHandler {
                // do anythings when data received
                session.dataHandler {
                    println it.toString()
                }
                // send data
                // session.send(Buffer.buffer("hello"))
            }
        }
    }
}
```

## Client

```groovy
def client = RakNet.create(Vertx.vertx(), new RakNetOptions())
client.open "127.0.0.1", 20000, {
    if (it.succeeded()) {
        def session = it.result()
        session.ping { pong ->
            if (pong.succeeded()) {
                println it.result()
                session.connect {
                    // when connected
                    if (it.succeeded()) {
                        session.send(Buffer.buffer("Hello"))
                    }
                }
            }
        }
    }
}
```