import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Random;

public class GSON_testing
{
	private static class Wrapper
	{
		List<Inner> inners = new ArrayList<>();
		
		public Wrapper()
		{
			Random rnd = new Random();
			
			for(int i = 0; i < 2; i++)
				inners.add(new Inner(rnd.nextInt(55)));
		}

		@Override
		public String toString()
		{
			return "Wrapper inners: " + inners + "]";
		}
	}
	
	private static class Inner
	{
		int innerID;
		
		public Inner(int ID) {innerID = ID;}

		@Override
		public String toString()
		{
			return "Inner: " + innerID;
		}
	}
	
	public static void main(String[] args)
	{
		List<Wrapper> wrappers = new ArrayList<>();
		for(int i = 0; i < 3; i++)
			wrappers.add(new Wrapper());
		
		Gson gson = new Gson();
		
		String wrapperJSON = gson.toJson(wrappers);
		
		Type listOfTestObject = new TypeToken<List<Wrapper>>(){}.getType();
		
		List<Wrapper> deserialized = gson.fromJson(wrapperJSON, listOfTestObject);
		
		for(Wrapper w : deserialized)
			System.out.println(w);
	}

}
