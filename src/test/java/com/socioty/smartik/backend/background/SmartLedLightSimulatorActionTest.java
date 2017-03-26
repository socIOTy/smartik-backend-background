package com.socioty.smartik.backend.background;


import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.CaseFormat;
import com.socioty.smartik.backend.background.simulate.bulb.SmartLedDeviceSimulator.Action;

public class SmartLedLightSimulatorActionTest {

	@Test
	public void testActionFromString() {
		for (final Action action : Action.values()) {
			final String actionString = "set" + CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, action.toString());
			final Action result = Action.parseAction(actionString);
			Assert.assertEquals(action, result);
		}
	}
}
