Problem 4: Completes task of Problem 3 more efficiently: remove the need of truster dealer by enabling a random number generator on the client side
In this example, each client produces their own share of 'a' and 'b'. Their shares of 'a' and 'b' are shared with the server which reconstructs 'a' and 'b'. As a result the server can calculate c (and gets shares of 'c'), x_prime = x-a and y_prime = y-b
The server then sends to each client a share of c, a share of x, and a share of y. Also sends x_prime and y_prime.
The server receives back the share of the product computed by each client, reconstructs the product, and sends it to all parties.

Run script run-server.sh to run the server side.
Run script run-client.sh to run the client.

