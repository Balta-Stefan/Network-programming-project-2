package mdp2021.backend.services.REST;

import javax.ws.rs.Path;

import java.util.Optional;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import mdp2021.backend.model.LinesOfTrainstation;
import mdp2021.backend.model.StationArrival;
import mdp2021.backend.model.TrainPassReport;
import mdp2021.backend.model.User;
import mdp2021.backend.persistence.ITrainstationPersistence;
import mdp2021.backend.persistence.REDIS_TrainstationPersistence;
import mdp2021.backend.utilities.REDIS_UserSessions;
import mdp2021.backend.utilities.UserSessions;


@Path("/v1")
public class REST_service
{
	private static final int sessionDurationSeconds = 7200;
	//private ITrainstationPersistence trainstationPersistence;
	//private UserSessions userSessions;
	
	@GET
	@Path("/train-schedule")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTrainSchedule(@CookieParam("cookie") Cookie cookie)
	{
		if(cookie == null)
			return Response.status(403).entity("Cookie called 'cookie' not found in the request.").build();
		
		
		ITrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();
		UserSessions userSessions = new REDIS_UserSessions(sessionDurationSeconds);
		
		String cookieValue = cookie.getValue();
		Optional<User> session = userSessions.getUser(cookieValue);
		
		if(session.isEmpty())
			return Response.status(400).entity("User not logged in.").build();
		
		User user = session.get();
		
		Optional<LinesOfTrainstation> schedules = trainstationPersistence.getTrainstationLines(user.getTrainStation());
		
		if(schedules.isEmpty())
			return Response.status(500).build();
		
		return Response.status(200).entity(schedules.get()).build();
		
	}
	
	@PUT
	@Path("/train-schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logPassingTrain(@CookieParam("cookie") Cookie cookie, TrainPassReport report)
	{
		if(cookie == null)
			return Response.status(403).entity("Cookie called 'cookie' not found in the request.").build();
		
		
		ITrainstationPersistence trainstationPersistence = new REDIS_TrainstationPersistence();
		UserSessions userSessions = new REDIS_UserSessions(sessionDurationSeconds);
		
		String cookieValue = cookie.getValue();
		Optional<User> session = userSessions.getUser(cookieValue);
		
		if(session.isEmpty())
			return Response.status(403).entity("User not logged in.").build();
		
		User user = session.get();
		
		if(report.trainstation.getID() != user.getTrainStation().getID())
			return Response.status(400).entity("Given identifications are incorrect.").build();
		
		// check whether the given line passes through the user's station
		Optional<StationArrival> arrival = trainstationPersistence.getArrivalOfLine(report.trainstation, report.trainLine);
		if(arrival.isEmpty())
			return Response.status(401).build();
		
		boolean status = trainstationPersistence.reportTrainPass(report);
		
		if(status == false)
			return Response.status(500).build();
		
		return Response.status(200).build();
		
	}
}
