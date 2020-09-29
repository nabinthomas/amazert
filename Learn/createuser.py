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


userCreated = False
print ("Attempting...")
try:
  auth = firebase.auth(); 
  user = auth.create_user_with_email_and_password(userEmail, password);
  accountInfo = auth.get_account_info(user['idToken']);
  print ("User created Account : ", accountInfo);  authCompleted = True;
  userCreated = True
except requests.exceptions.HTTPError as e:
  error_json = e.args[1]
  error = json.loads(error_json)['error']
  print ("Error :" , error);
  
print ("End of User Creation")

# Result
if userCreated == True: 
  print ("User " + userEmail + " was created successfully")
else:
  print ("Failed to create user " + userEmail )



