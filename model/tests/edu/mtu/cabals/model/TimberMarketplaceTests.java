package edu.mtu.cabals.model;

import org.junit.Assert;
import org.junit.Test;

import ec.util.MersenneTwisterFast;
import edu.mtu.cabals.wup.AcerRebrum;
import edu.mtu.cabals.wup.BetulaAlleghaniensis;
import edu.mtu.cabals.wup.PinusStrobus;
import edu.mtu.simulation.ForestSimException;

public class TimberMarketplaceTests {

	/**
	 * Very basic test to ensure that loading is working correctly.
	 */
	@Test
	public void loadTest() throws ForestSimException {
		TimberMarketplace tm = TimberMarketplace.getInstance();
		tm.load("data/prices.csv", new MersenneTwisterFast());
		
		Assert.assertNotEquals(0.0, tm.getPrice(new AcerRebrum(), 27.94));
		Assert.assertNotEquals(0.0, tm.getPrice(new BetulaAlleghaniensis(), 27.94));
		Assert.assertNotEquals(0.0, tm.getPrice(new PinusStrobus(), 22.86));
		Assert.assertNotEquals(0.0, tm.getWoodyBiomassPrice());
	}
}
