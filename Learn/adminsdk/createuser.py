import pyrebase
import requests
import json

userEmail = "nabin@test.com"
password = "amazert"
firebaseConfigFile = open("firebase.json")
firebaseConfig = json.load(firebaseConfigFile);
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



