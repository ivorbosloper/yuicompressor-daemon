## yuicompressor-daemon

Provide yuicompressor functionality as a service. Avoids jvm startup time if your compression pipeline calls the yuicompressor java process many times, and allows caching over your different developers.

## Example

Run server with

> java -jar target/yuicompressor-daemon-1.0-jar-with-dependencies.jar 8000

Call the client with:

> echo -e "filename.js\nvar variable=0;\n\0\n" | nc localhost 8000

## Installation

> mvn clean package

> java -jar target/yuicompressor-daemon-1.0-jar-with-dependencies.jar 8000

Or download the jar file from the dist directory in this repo

## Contributors

- Zhang Erning / http://erning.net
- Ivor Bosloper / https://github.com/ivorbosloper/

## License

Not sure, cloned from https://github.com/anjuke/yuicompressor-daemon
