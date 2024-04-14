
cd ~/Documents  # your local folder

mkdir gilad-danon-test
cd gilad-danon-test

git clone https://github.com/GiladanoN/MicroService-VertX-pg-2024.git
cd MicroService-VertX-pg-2024/parent-project/

# if "package" goal encounters an error, run this first
mvn install

mvn clean package  # build projects

# run in seperate terminals / as background cmds
mvn exec:java -pl auth-test-module   # this repesents RestVerticle
mvn exec:java -pl extra-test-module  # this repesents OrderVerticle

###################################################

# use a third terminal / another http-request tool for these:  (primary POC capabilities)

# 1. runs request for adding an order, which creates/updates the relevant user-orders json file.
curl localhost:8888/TestEB -v

  # currently unprotected endpoint, a dummy object is added each time with the same name.
  # expect a success at first with data appearing under "parent-project/data/orders/*".
  # expect subsequent request failures with "409 conflict" as the fail cause.

# 2. returns a session cookie (raw session id), and saves it to a temp file (--cookie-jar flag)
curl localhost:8888/Login -vX POST -u "user1:pass1" -c saved_cookie.txt

  # expect success with any user-password combo that appears in "authfile.txt" (above is pre-defined)
  # expect failure (401 forbidden / no auth) with any combo which isn't a valid user-password.

# 3. returns some content when user is logged in (e.g. protected path - blah blah 789 - id = <sesId>)
curl localhost:8888/GetOrders -v -b saved_cookie.txt

  # expect a good response when using the session before timeout / logout / server-reset etc.
  # expect a "please log in" response if not (no longer) logged in for any reason.

