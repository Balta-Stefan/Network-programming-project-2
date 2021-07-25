package mdp2021.backend.persistence;

import java.util.Optional;

import mdp2021.backend.model.User;

public interface IUserDAO
{
	public Optional<User> getUser(String username);
	public boolean addUser(User user);
	public boolean removeUser(User user);
}
