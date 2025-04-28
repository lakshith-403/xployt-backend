import hashlib

def hash_password(password):
    # Create SHA-256 hash object
    hash_obj = hashlib.sha256()
    # Update with password bytes (UTF-8 encoded)
    hash_obj.update(password.encode('utf-8'))
    # Get the hexadecimal digest
    return hash_obj.hexdigest()

# Example usage
if __name__ == "__main__":
    password = input("Enter password to hash: ")
    hashed = hash_password(password)
    print(f"Hashed password: {hashed}")