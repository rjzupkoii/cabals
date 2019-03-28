package edu.mtu.cabals.wup;

import org.junit.Assert;
import org.junit.Test;

public class SpeciesTests {

	private static final double EPSILON = 1e-15;
	
	/**
	 * Verify height to dbh is correct for Acer Rebrum.
	 */
	@Test
	public void AcerRebrumDbh() {
		AcerRebrum species = new AcerRebrum();
		Assert.assertEquals(-1, species.heightToDbh(1), EPSILON);
		Assert.assertEquals(0.74, round(species.heightToDbh(2)), EPSILON);
		Assert.assertEquals(3.53, round(species.heightToDbh(5)), EPSILON);
		Assert.assertEquals(8.31, round(species.heightToDbh(10)), EPSILON);
		Assert.assertEquals(34.56, round(species.heightToDbh(25)), EPSILON);
		Assert.assertEquals(84.97, round(species.heightToDbh(30)), EPSILON);
		Assert.assertEquals(-1, species.heightToDbh(31), EPSILON);
	}
	
	/**
	 * Verify height to dbh is correct for Betula Alleghaniesis
	 */
	@Test
	public void BetulaAlleghaniensisDbh() {
		BetulaAlleghaniensis species = new BetulaAlleghaniensis();
		Assert.assertEquals(-1, species.heightToDbh(1), EPSILON);
		Assert.assertEquals(0.43, round(species.heightToDbh(2)), EPSILON);
		Assert.assertEquals(2.47, round(species.heightToDbh(5)), EPSILON);
		Assert.assertEquals(6.65, round(species.heightToDbh(10)), EPSILON);
		Assert.assertEquals(59.95, round(species.heightToDbh(22)), EPSILON);
		Assert.assertEquals(-1, round(species.heightToDbh(23)), EPSILON);
	}
	
	/**
	 * Verify height to dbh is correct for Pinus Strobus.
	 */
	@Test
	public void PinusStrobusDbh() {
		PinusStrobus species = new PinusStrobus();
		Assert.assertEquals(-1, species.heightToDbh(1), EPSILON);
		Assert.assertEquals(0.81, round(species.heightToDbh(2)), EPSILON);
		Assert.assertEquals(4.80, round(species.heightToDbh(5)), EPSILON);
		Assert.assertEquals(12.09, round(species.heightToDbh(10)), EPSILON);
		Assert.assertEquals(41.06, round(species.heightToDbh(25)), EPSILON);
		Assert.assertEquals(294.50, round(species.heightToDbh(50)), EPSILON);
		Assert.assertEquals(-1, round(species.heightToDbh(51)), EPSILON);
	}
	
	private double round(double value) {
		return (double)Math.round(value * 100d) / 100d;
	}
}
