package mdp2021.backend.utilities;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

public class CookieGenerator
{
	public static final int cookieByteLength = 32;
	
	private static final SecureRandom randomGenerator = new SecureRandom();
	
	public static String generateCookie()
	{
		byte[] cookieBytes = new byte[cookieByteLength];
		randomGenerator.nextBytes(cookieBytes);
		
		Encoder base64Encoder = Base64.getEncoder();
		String stringCookie = base64Encoder.encodeToString(cookieBytes);
		
		return stringCookie;
	}
}
