package mdp2021.backend.utilities;

import java.util.Base64;
import java.util.Base64.Encoder;

import org.bouncycastle.crypto.generators.BCrypt;

public class BCrypt_hasher extends PasswordHasher
{
	private static final int bcrypt_cost = 5;
	private static final int bcrypt_salt_size = 16;
	
	@Override
	public String hash(byte[] salt, String password) throws Exception
	{
		if(salt.length != bcrypt_salt_size)
			throw new Exception("Salt must be 16 bytes long.");
		
		byte[] passwordByteArray = BCrypt.passwordToByteArray(password.toCharArray());
		byte[] hashedPassword = BCrypt.generate(passwordByteArray, salt, bcrypt_cost);
		
		Encoder base64Encoder = Base64.getEncoder();
		
		return base64Encoder.encodeToString(hashedPassword);
		//return new String(hashedPassword, StandardCharsets.UTF_8);
	}
	
	@Override
	public byte[] getSalt()
	{
		byte[] salt = new byte[bcrypt_salt_size];
		randomGenerator.nextBytes(salt);
		return salt;
	}
}
