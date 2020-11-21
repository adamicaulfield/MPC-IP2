# Problem 2: 
Five Secrets from five parties with the goal to sum their secrets. Shares of each secret are sent to each party, and each party receives the sum without their secret being revealed

## How to Run: Client-Server Model
### Run Server
- execute `./run-server.sh`

### Run Client
- open 5 terminal windows for the five clients
- execute `./run-client.sh`
- when prompted on each client, enter `SECRET:#` replacing `#` with the secret integer for that client

### Result
- After all shares have been communicated, the result will be computed by the Server
- The server will send the sum to all parties and then disconnect

## How to Run: Standalone Program
To run this model all in one program without client-server
- `javac -d ./bin Prob2.java`
- `java -cp ./bin Prob2`
