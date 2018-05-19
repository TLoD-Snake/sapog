# sapog
Prototype of akka-http websockets server

REST Endpoints:
- POST http://localhost:8080/size/convert
body: `{
	"from": "usa",
	"to": "eur",
	"size": 6
}`
- GET http://localhost:8080/size/list/eur
- GET http://localhost:8080/size/list/usa