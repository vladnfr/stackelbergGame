import comp34120.ex2.PlayerType;
import comp34120.ex2.PlayerImpl;
import comp34120.ex2.Record;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.ArrayList;

/**
 * A very simple leader implementation that only generates random prices
 * @author Xin
 */
final class SupremeLeader
	extends PlayerImpl
{
	/* The randomizer used to generate random price */
	private final Random m_randomizer = new Random(System.currentTimeMillis());

	private SupremeLeader()
		throws RemoteException, NotBoundException
	{
		super(PlayerType.LEADER, "Supreme Leader");
	}

	@Override
	public void goodbye()
		throws RemoteException
	{
		ExitTask.exit(500);
	}

	/**
	 * To inform this instance to proceed to a new simulation day
	 * @param p_date The date of the new day
	 * @throws RemoteException
	 */
	@Override
	public void proceedNewDay(int p_date)
		throws RemoteException
	{
		float[] u_leader = new float[100];
		float[] u_follower = new float[100];

		for(int i=1;i<101;i++)
		{
			Record newRecord = m_platformStub.query(m_type, i);

			u_leader[i-1] = newRecord.m_leaderPrice;
			u_follower[i-1] = newRecord.m_followerPrice;
		}

		

		m_platformStub.publishPrice(m_type, genPrice(1.8f, 0.05f));
		
	}

	/**
	 * Generate a random price based Gaussian distribution. The mean is p_mean,
	 * and the diversity is p_diversity
	 * @param p_mean The mean of the Gaussian distribution
	 * @param p_diversity The diversity of the Gaussian distribution
	 * @return The generated price
	 */
	private float genPrice(final float p_mean, final float p_diversity)
	{
		return (float) (p_mean + m_randomizer.nextGaussian() * p_diversity);
	}

	public static void main(final String[] p_args)
		throws RemoteException, NotBoundException
	{
		new SupremeLeader();
	}

	/**
	 * The task used to automatically exit the leader process
	 * @author Xin
	 */
	private static class ExitTask
		extends TimerTask
	{
		static void exit(final long p_delay)
		{
			(new Timer()).schedule(new ExitTask(), p_delay);
		}
		
		@Override
		public void run()
		{
			System.exit(0);
		}
	}
}
