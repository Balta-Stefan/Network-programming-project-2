package mdp2021.backend.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class REDIS_CustomPool
{
	private static JedisPool pool;
	private static final String propertiesPath = "Resources\\backend constants.properties";
	private static final String JedisPool_URI_Property = "JedisPool_URI";
	
	static
	{
		Properties backendProperties = new Properties();
		try(FileInputStream fis = new FileInputStream(new File(propertiesPath)))
		{
			backendProperties.load(fis);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		String redisURI = backendProperties.getProperty(JedisPool_URI_Property);
		pool = new JedisPool(redisURI);
	}
	
	public static Jedis getConnection()
	{
		return pool.getResource();
	}
}
