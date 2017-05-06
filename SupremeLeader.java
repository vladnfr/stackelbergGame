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
import java.lang.IndexOutOfBoundsException;
import java.lang.Math;

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
		int alpha = 0;

		float[] u_leader = new float[100-alpha*(p_date-101)];
		float[] u_follower = new float[100-alpha*(p_date-101)];

		for(int i=1;i<101-alpha*(p_date-101);i++)
		{
			Record newRecord = m_platformStub.query(m_type, p_date-i);
			//System.out.println(p_date-i);
			u_leader[i-1] = newRecord.m_leaderPrice;
			u_follower[i-1] = newRecord.m_followerPrice;
		}

		m_platformStub.publishPrice(m_type, computePrice(u_leader,u_follower));
	}

	private float getA(float[] leaderHistory, float[] followerHistory)
	{
		float t1, t2, t3, t4, t5;
		short noOfDays = (short) leaderHistory.length;
		t1 = sumOfSquares(leaderHistory);
		t2 = sumOfProducts(leaderHistory, followerHistory);
		t3 = sum(followerHistory);
		t4 = sum(leaderHistory);
		t5 = computeDenominator(noOfDays, t1, t4);
		return (t1 * t3 - t4 * t2) / t5;
	}

	private float getB(float[] leaderHistory, float[] followerHistory)
	{
		float t1, t2, t3, t4, t5;
		short noOfDays = (short) leaderHistory.length;
		t1 = sumOfSquares(leaderHistory);
		t2 = sumOfProducts(leaderHistory, followerHistory);
		t3 = sum(followerHistory);
		t4 = sum(leaderHistory);
		t5 = computeDenominator(noOfDays, t1, t4);
		return (noOfDays * t2 - t3 * t4) / t5;
	}

	private float getAlpha(float b)
	{
		return 0.3f * b - 1;
	}

	private float getBeta(float a, float b)
	{
		return 2 + 0.3f * a + 1 - 0.3f * b;
	}

	private float getGamma(float a, float noOfDays) 
	{
		return noOfDays * (2 + 0.3f * a);
	}

	private float gradientAscent(float[] leaderHistory, float[] followerHistory, int numberOfDays, float a, float b)
	{
		float[] futurePrices = new float[numberOfDays];
		float[] deltaPrices = new float[numberOfDays];

		for(short i=0; i<numberOfDays; i++)
		{
			futurePrices[i] = m_randomizer.nextFloat() * 3;
		}

		float alpha = getAlpha(b);
		float beta = getBeta(a, b);

		do
		{
			for(short j=0; j<numberOfDays; j++)
			{

				deltaPrices[j] = 2*alpha*futurePrices[j] + beta;
				futurePrices[j] = futurePrices[j] + deltaPrices[j];
				if(futurePrices[j] < 1)
					futurePrices[j] = 1.2f;
			}

		} while(mean(deltaPrices) > 1e-20f);

		if(getProfit(futurePrices[0],b*futurePrices[0]+a) < 0.5)
			return computePrice(leaderHistory,followerHistory);

		return futurePrices[0];
	}

	private float getProfit(float leaderPrice, float followerPrice)
	{
		return (leaderPrice - 1)*(2-leaderPrice+0.3f*followerPrice);
	}

	private float mean(float[] numberArray)
	{
		int sum = 0;
		
		for(short i=0; i<numberArray.length; i++)
		{
			sum += Math.abs(numberArray[i]);
		}

		return sum/numberArray.length;

	}

	private float sumOfSquares(float[] array) throws IndexOutOfBoundsException
	{
		float sum = 0;
		for (short i = 0; i < array.length; i++)
		{
			sum += Math.pow(array[i], 2);
		}

		return sum;
	}

	private float sum(float[] array) throws IndexOutOfBoundsException
	{
		float sum = 0;
		for (short i = 0; i < array.length; i++)
		{
			sum += array[i];
		}

		return sum;
	}

	private float sumOfProducts(float[] leaderPrices, float[] followerPrices)
	{
		float sum = 0;
		for(short i = 0; i < leaderPrices.length; i++)
		{
			sum += leaderPrices[i]*followerPrices[i];
		}

		return sum;
	}

	private float computeDenominator(short noOfDays, float t1, float t4)
	{
		return  noOfDays * t1 - (float)Math.pow(t4, 2);
	}

	private float computePrice(float[] leaderHistory, float[] followerHistory)
	{
		float t1, t2, t3, t4, t5;
		short noOfDays = (short) leaderHistory.length;
		t1 = sumOfSquares(leaderHistory);
		t2 = sumOfProducts(leaderHistory, followerHistory);
		t3 = sum(followerHistory);
		t4 = sum(leaderHistory);
		t5 = computeDenominator(noOfDays, t1, t4);

		float a, b;
		a = (t1 * t3 - t4 * t2) / t5;
		b = (noOfDays * t2 - t3 * t4) / t5;

		float numerator, denominator;
		numerator = 2 + 0.3f * a + 1 - 0.3f * b;
		denominator = 2.0f * (1 - 0.3f * b);

		System.out.println(a + b * numerator / denominator);

		return numerator / denominator;
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
