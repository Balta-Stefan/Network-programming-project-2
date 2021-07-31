package mdp2021.frontend.GUI;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class BaseFXController
{
	protected static final ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(1);
}
