# Problem 1: 
Secret shares amongst 5 parties with the goal to reconstruct the secret

## How to Run
### Run Server
- execute `./run-server.sh`

### Run Client
- open 5 terminal windows for the five clients
- execute `./run-client.sh`

### Result
- After all shares have been communicated, the result will be computed by the Server
- The sever will disconnect with all clients and display the secret in the server's console

## How to Run: Standalone Program
To run this model all in one program without client-server
- `javac -d ./bin Prob1.java`
- `java -cp ./bin Prob1`