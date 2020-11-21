# Problem 3: 
Three parties work together to compute the product of two secrets using Beaver's Triples. 
A trusted dealer generates a list of 1 million triples, and the server selects a random triple from the list.

## How to Run
### Run Server
- open 1 terminal window for 1 server
- execute `./run-server.sh`

### Run Trusted dealer
- open 1 terminal window for 1 trusted dealer
- execute `./run-dealer.sh`

### Run Client
- open 3 terminal windows for the three clients
- execute `./run-client.sh`

### Result
- The server will first request a triple from the trusted dealer
- The dealer will send it to the server, and then disconnect
- The server will then send all shares of the triple and the secret data to the clients
- The clients send back their shares of the differences (x' = x-a and y' = y-b)
- The server will send to all clients the accumulated result of x' and y'
- Clients will compute and send their result for their share of product
- Server will accumulate shares to produce the product z and share will all clients
- Server disconnects with all clients

## How to Run: Standalone Program
To run this model all in one program without client-server-dealer
- `javac -d ./bin Prob3.java`
- `java -cp ./bin Prob3`

