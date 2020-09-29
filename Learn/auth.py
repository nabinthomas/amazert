import pyrebase
import requests
import json

userEmail = "nabin@test.com"
password = "amazert"
firebaseConfig = {
    "apiKey": "AIzaSyBoNiOEg2atuC_Js5_lvue9hhVXeD4SQ1k",
    "authDomain": "amazert.firebaseapp.com",
    "databaseURL": "https://amazert.firebaseio.com",
    "projectId": "amazert",
    "storageBucket": "amazert.appspot.com",
    "messagingSenderId": "447659316293",
    "appId": "1:447659316293:web:80afbfe74c0d6218ab7f2c",
    "measurementId": "G-XQXW3T2PK9"
  };

firebase = pyrebase.initialize_app(firebaseConfig);

authSuccess = False

print ("Attempting...")
try:
  auth = firebase.auth(); 
  user = auth.sign_in_with_email_and_password(userEmail, password);

  accountInfo = auth.get_account_info(user['idToken']);
  print ("ID TOken is ", user['idToken'])
  print ("Acc Info: " , accountInfo);
  authSuccess = True
except requests.exceptions.HTTPError as e:
  error_json = e.args[1]
  error = json.loads(error_json)['error']
  print ("Error :" , error);
  
print ("End of Auth Attempt")

# Result
if authSuccess == True: 
  print ("User " + userEmail + " was authenticated successfully")
else:
  print ("Failed to authenticate user " + userEmail )
