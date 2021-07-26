package mdp2021.backend.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class REDIS_CustomPool
{
	private static final Logger log = Logger.getLogger(REDIS_CustomPool.class.getName());
	static
	{
		log.setLevel(Level.FINEST);
		FileHandler txtHandler;
		try
		{		
			txtHandler = new FileHandler("Logs/REDIS_CustomPool.txt", true);
			SimpleFormatter txtFormatter = new SimpleFormatter();
			txtHandler.setFormatter(txtFormatter);
			log.addHandler(txtHandler);
		} catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
	}
	
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
			log.info(e.getMessage());
		}
		
		String redisURI = backendProperties.getProperty(JedisPool_URI_Property);
		pool = new JedisPool(redisURI);
	}
	
	public static Jedis getConnection()
	{
		return pool.getResource();
	}
}
