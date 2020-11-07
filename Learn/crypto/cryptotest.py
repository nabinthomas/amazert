import base64
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

from Crypto.Cipher import AES
import os, binascii
import sys

def generateKey(password):
    password = password.encode()
    salt = b'salt_'
    kdf = PBKDF2HMAC(
        algorithm=hashes.SHA256(),
        length=32,
        salt=salt, 
        iterations=100000,
        backend=default_backend()
    )
    #key = base64.urlsafe_b64encode(kdf.derive(password)) 
    key = kdf.derive(password) 
    print("key = [" + str(key) + "]")
    return key

def encrypt_AES_GCM(msg, password):
    secretKey = generateKey(password)
    aesCipher = AES.new(secretKey, AES.MODE_GCM)
    ciphertext, authTag = aesCipher.encrypt_and_digest(msg)
    return (ciphertext, aesCipher.nonce, authTag)

def decrypt_AES_GCM(encryptedMsg, password):
    (ciphertext, nonce, authTag) = encryptedMsg
    secretKey = generateKey(password)
    aesCipher = AES.new(secretKey, AES.MODE_GCM, nonce)
    plaintext = aesCipher.decrypt_and_verify(ciphertext, authTag)
    return plaintext

msg = b'Message for AES-256-GCM + Scrypt encryption'
password = "TestPassword"
password = sys.argv[1]
encryptedMsg = encrypt_AES_GCM(msg, password)
print("encryptedMsg", {
    'ciphertext': binascii.hexlify(encryptedMsg[0]),
    'aesIV': binascii.hexlify(encryptedMsg[1]),
    'authTag': binascii.hexlify(encryptedMsg[2])
})

decryptedMsg = decrypt_AES_GCM(encryptedMsg, password)
print("decryptedMsg", decryptedMsg)

