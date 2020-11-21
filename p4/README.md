# Problem 4: 
Completes task of Problem 3 more efficiently: remove the need of truster dealer by enabling a random number generator on the client side

## How to Run
### Run Server
- open 1 terminal window for 1 server
- execute `./run-server.sh`

### Run Client
- open 3 terminal windows for the three clients
- execute `./run-client.sh`

### Result
- Clients will generate their share of a and b, and will send it to the server
- The server will accumulate shares of a and b to get: c = a*b, x' = x-a, and y' = y-b
- The server will send shares of x,y and c, and will send the true value of x' and y' 
= Clients will use this data to compute their share of the product z'
- Server will accumulate shares to produce the true product z and share will all clients
- Server disconnects with all clients


