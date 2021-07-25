package mdp2021.backend.utilities;

import java.util.Optional;

import mdp2021.backend.model.User;

public interface UserSessions
{
	public String login(User user); // returns session cookie even if present
	public Optional<User> getUser(String userCookie);
	public boolean logout(String userCookie);
}
