package edu.mtu.cabals.model;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import edu.mtu.cabals.wup.AcerRebrum;

public class TimberMarketplaceTests {

	/**
	 * Very basic test to ensure that loading is working correctly.
	 */
	@Test
	public void loadTest() {
		TimberMarketplace tm = TimberMarketplace.getInstance();
		tm.load("data\\prices.csv", new Random());
		
		System.out.println(tm.getPulpwoodPrice(new AcerRebrum()));
		Assert.assertNotEquals(0.0, tm.getPulpwoodPrice(new AcerRebrum()));
		Assert.assertNotEquals(0.0, tm.getSawlogPrice(new AcerRebrum()));
	}
}
