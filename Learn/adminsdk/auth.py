import pyrebase
import requests
import json

userEmail = "nabin@test.com"
password = "amazert"
firebaseConfigFile = open("firebase.json")
firebaseConfig = json.load(firebaseConfigFile);
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
