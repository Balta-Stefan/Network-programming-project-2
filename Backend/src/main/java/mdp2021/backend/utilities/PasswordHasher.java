package mdp2021.backend.utilities;

import java.security.SecureRandom;

public abstract class PasswordHasher
{
	protected static SecureRandom randomGenerator = new SecureRandom();
	
	public abstract String hash(byte[] salt, String password) throws Exception;
	public abstract byte[] getSalt();
}
