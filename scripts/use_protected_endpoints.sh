
# general note -
# if you want less debug info in your output remove "v" or "-v" from curl cmds.

# cd <whereever you want>
COOKIE_FILE=./saved_cookie.txt

# address the "Login" endpoint, and save recieved cookie to a file for use in consecutive requests
curl localhost:8888/Login -vX POST -u "user1:pass1" -c $COOKIE_FILE

# address the "GetOrders" endpoint, and use the same cookie file to use your current login
curl localhost:8888/GetOrders -v -b $COOKIE_FILE
